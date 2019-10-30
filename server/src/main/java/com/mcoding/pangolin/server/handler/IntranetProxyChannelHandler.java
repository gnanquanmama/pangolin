package com.mcoding.pangolin.server.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.common.entity.AddressInfo;
import com.mcoding.pangolin.common.util.ChannelAddressUtils;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import com.mcoding.pangolin.server.context.TrafficEventBus;
import com.mcoding.pangolin.server.traffic.TrafficEvent;
import com.mcoding.pangolin.server.context.PangolinChannelContext;
import com.mcoding.pangolin.server.context.PublicNetworkPortTable;
import com.mcoding.pangolin.server.context.RequestChainTraceTable;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * 内网代理通道处理器
 *
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class IntranetProxyChannelHandler extends SimpleChannelInboundHandler<PMessageOuterClass.PMessage> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("EVENT=激活内网代理端通道{}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();
        PangolinChannelContext.unBindIntranetProxyChannel(privateKey);
        log.warn("EVENT=关闭内网代理端通道{}", ctx.channel());
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        switch (msg.getType()) {
            case MessageType.AUTH:
                handleAuth(ctx, msg);
                break;
            case MessageType.CONNECT:
                handleConnect(ctx, msg);
                break;
            case MessageType.TRANSFER:
                handleTransfer(ctx, msg);
                break;
            case MessageType.DISCONNECT:
                handleDisconnect(ctx, msg);
                break;
            case MessageType.HEART_BEAT:
                handleHeartBeat(ctx, msg);
                break;
            case MessageType.CHAIN_TRACE:
                handleChainTrace(ctx, msg);
                break;
            default:
                break;
        }
    }

    private void handleChainTrace(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        log.info("EVENT=收取到追踪信息|MSG={}", msg.getData().toStringUtf8());

        String data = msg.getData().toStringUtf8();
        List<AddressInfo> clientAddressList = JSON.parseObject(data, new TypeReference<List<AddressInfo>>(){});

        AddressInfo clientProxyAddress = null;
        AddressInfo clientTargetAddress = null;

        for (AddressInfo addressInfo : clientAddressList) {
            if (StringUtil.isNullOrEmpty(addressInfo.getSessionId())){
                clientProxyAddress = addressInfo;
            } else {
                clientTargetAddress = addressInfo;
            }
        }

        String privateKey = clientProxyAddress.getPrivateKey();
        String sessionId = clientTargetAddress.getSessionId();

        Channel intranetProxyChannel = PangolinChannelContext.getIntranetProxyServerChannel(privateKey);
        Channel publicNetworkChannel = PangolinChannelContext.getPublicNetworkChannel(sessionId);

        AddressInfo serverIntranetProxyChannel = ChannelAddressUtils.buildAddressInfo(intranetProxyChannel);
        AddressInfo serverPublicNetworkChannel = ChannelAddressUtils.buildAddressInfo(publicNetworkChannel);

        List<AddressInfo> requestAddressList = Lists.newArrayList();
        requestAddressList.add(serverPublicNetworkChannel);
        requestAddressList.add(serverIntranetProxyChannel);
        requestAddressList.add(clientProxyAddress);
        requestAddressList.add(clientTargetAddress);

        RequestChainTraceTable.add(sessionId, requestAddressList);
    }

    private void handleHeartBeat(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        log.info("EVENT=收到心跳包|MSG={}", msg.getData().toStringUtf8());
    }

    private void handleDisconnect(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        log.warn("EVENT=断开外网连接通道|DESC=被代理服务器通道已关闭|SESSION_ID={}", msg.getSessionId());
        PangolinChannelContext.unBindPublicNetworkChannel(msg.getSessionId());
    }

    private void handleConnect(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        Channel userChannel = PangolinChannelContext.getPublicNetworkChannel(msg.getSessionId());
        userChannel.config().setAutoRead(true);
    }

    private void handleAuth(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        String privateKey = msg.getPrivateKey();

        PMessageOuterClass.PMessage.Builder authMsgBuilder = PMessageOuterClass.PMessage.newBuilder()
                .setSessionId(msg.getSessionId())
                .setType(MessageType.AUTH)
                .setPrivateKey(privateKey)
                .setData(ByteString.copyFrom(Constants.AUTH_SUCCESS.getBytes()));

        Integer publicPort = PublicNetworkPortTable.getUserToPortMap().get(privateKey);
        if (Objects.isNull(publicPort)) {
            authMsgBuilder.setData(ByteString.copyFrom("私钥不存在，请在服务端配置后，再连接".getBytes()));
            ctx.writeAndFlush(authMsgBuilder.build());
            log.error("EVENT=认证失败，不存在PRIVATE_KEY={}", privateKey);
            return;
        }

        ctx.writeAndFlush(authMsgBuilder.build());
        ctx.channel().attr(Constants.PRIVATE_KEY).set(privateKey);
        PangolinChannelContext.addProxyServerChannel(privateKey, ctx.channel());
        log.info("EVENT=连接认证处理|DESC=认证通过|PRIVATE_KEY={}", privateKey);
    }

    private void handleTransfer(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        Channel userChannel = PangolinChannelContext.getPublicNetworkChannel(msg.getSessionId());
        userChannel.writeAndFlush(Unpooled.wrappedBuffer(msg.getData().toByteArray()));

        // 记录流入流量字节数量
        TrafficEvent trafficEvent = new TrafficEvent();

        String userPrivateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();
        trafficEvent.setUserPrivateKye(userPrivateKey);
        trafficEvent.setInFlow(0);
        trafficEvent.setOutFlow(msg.toByteArray().length);
        TrafficEventBus.getInstance().post(trafficEvent);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.info("event=代理管道可写状态变化" + ctx.channel().isWritable());

        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("EVENT=代理通道异常|CHANNEL={}|ERROR_MSG={}", ctx.channel(), cause.getMessage());
    }
}
