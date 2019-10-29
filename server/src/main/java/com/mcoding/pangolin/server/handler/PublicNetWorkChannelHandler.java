package com.mcoding.pangolin.server.handler;

import com.google.protobuf.ByteString;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import com.mcoding.pangolin.server.context.FlowEventBusSingleton;
import com.mcoding.pangolin.server.traffic.TrafficEvent;
import com.mcoding.pangolin.server.context.PangolinChannelContext;
import com.mcoding.pangolin.server.context.PublicNetworkPortTable;
import com.mcoding.pangolin.server.context.RequestChainTraceTable;
import com.mcoding.pangolin.server.context.SessionIdProducer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * 公网通道处理器
 *
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class PublicNetWorkChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static SessionIdProducer sessionIdProducer = new SessionIdProducer();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress localSocketAddress = (InetSocketAddress) ctx.channel().localAddress();
        int port = localSocketAddress.getPort();

        String privateKey = PublicNetworkPortTable.getUserToPortMap().inverse().get(port);
        Channel intranetProxyChannel = PangolinChannelContext.getIntranetProxyServerChannel(privateKey);
        if (intranetProxyChannel == null) {
            log.warn("代理客户端通道还未建立");
            ctx.channel().close();
            return;
        }

        if (!intranetProxyChannel.isActive()) {
            System.out.println();
            log.warn("EVENT=关闭未激活代理客户端通道{}", ctx.channel());
            PangolinChannelContext.unBindIntranetProxyChannel(privateKey);
            ctx.channel().close();
            return;
        }

        log.info("EVENT=激活公网代理端通道:{}", ctx.channel());
        Channel publicNetworkChannel = ctx.channel();
        publicNetworkChannel.config().setAutoRead(false);

        String sessionId = sessionIdProducer.generate();

        publicNetworkChannel.attr(Constants.SESSION_ID).set(sessionId);
        publicNetworkChannel.attr(Constants.PRIVATE_KEY).set(privateKey);


        PMessageOuterClass.PMessage connectMsg = PMessageOuterClass.PMessage.newBuilder()
                .setType(MessageType.CONNECT)
                .setSessionId(sessionId)
                .build();

        intranetProxyChannel.writeAndFlush(connectMsg);

        PangolinChannelContext.bindPublicNetworkChannel(sessionId, publicNetworkChannel);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {

        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();
        String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();

        Channel proxyServerChannel = PangolinChannelContext.getIntranetProxyServerChannel(privateKey);
        if (Objects.isNull(proxyServerChannel) || !proxyServerChannel.isActive()) {
            log.warn("EVENT=关闭代理服务代理管道{}", ctx.channel());
            ctx.close();
            return;
        }

        byte[] data = ByteBufUtil.getBytes(msg);
        PMessageOuterClass.PMessage disconnectMsg = PMessageOuterClass.PMessage.newBuilder()
                .setType(MessageType.TRANSFER)
                .setSessionId(sessionId)
                .setData(ByteString.copyFrom(data))
                .build();

        proxyServerChannel.writeAndFlush(disconnectMsg);

        // 记录流入流量字节数量
        TrafficEvent trafficEvent = new TrafficEvent();
        trafficEvent.setUserPrivateKye(privateKey);
        trafficEvent.setInFlow(data.length);
        trafficEvent.setOutFlow(0);
        FlowEventBusSingleton.getInstance().post(trafficEvent);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();
        String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();

        PangolinChannelContext.unBindPublicNetworkChannel(sessionId);
        ctx.close();
        log.warn("EVENT=关闭公网代理端通道{}", ctx.channel());

        Channel proxyChannel = PangolinChannelContext.getIntranetProxyServerChannel(privateKey);


        if (Objects.nonNull(proxyChannel) && proxyChannel.isActive()) {
            PMessageOuterClass.PMessage disconnectMsg = PMessageOuterClass.PMessage.newBuilder()
                    .setType(MessageType.DISCONNECT)
                    .setSessionId(sessionId)
                    .build();
            proxyChannel.writeAndFlush(disconnectMsg);
        }

        // 清楚请求链路信息
        RequestChainTraceTable.remove(sessionId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
        ctx.close();
    }

}
