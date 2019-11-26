package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.common.codec.ConnectPacket;
import com.mcoding.pangolin.common.codec.DisconnectPacket;
import com.mcoding.pangolin.common.codec.TransferPacket;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.server.context.*;
import com.mcoding.pangolin.server.traffic.TrafficEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
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
@ChannelHandler.Sharable
public class PublicNetWorkChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    public static final PublicNetWorkChannelHandler INSTANCE = new PublicNetWorkChannelHandler();

    private static SessionIdProducer sessionIdProducer = new SessionIdProducer();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress localSocketAddress = (InetSocketAddress) ctx.channel().localAddress();
        int port = localSocketAddress.getPort();

        String privateKey = PublicNetworkPortTable.getUserToPortMap().inverse().get(port);
        Channel intranetProxyChannel = ChannelHolderContext.getIntranetProxyServerChannel(privateKey);
        if (intranetProxyChannel == null) {
            log.warn("PROXY CLIENT CHANNEL UNESTABLISHED");
            ctx.channel().close();
            return;
        }

        if (!intranetProxyChannel.isActive()) {
            System.out.println();
            log.warn("EVENT=CLOSE INACTIVE PROXY CLIENT CHANNEL{}", ctx.channel());
            ChannelHolderContext.unBindIntranetProxyChannel(privateKey);
            ctx.channel().close();
            return;
        }

        log.info("EVENT=active public network proxy channel -> {}", ctx.channel());
        Channel publicNetworkChannel = ctx.channel();
        publicNetworkChannel.config().setAutoRead(false);

        String sessionId = sessionIdProducer.generate();

        publicNetworkChannel.attr(Constants.SESSION_ID).set(sessionId);
        publicNetworkChannel.attr(Constants.PRIVATE_KEY).set(privateKey);


        ConnectPacket connectPacket = new ConnectPacket();
        connectPacket.setType(MessageType.CONNECT);
        connectPacket.setSessionId(sessionId);
        connectPacket.setPrivateKey(privateKey);

        intranetProxyChannel.writeAndFlush(connectPacket);

        ChannelHolderContext.bindPublicNetworkChannel(sessionId, publicNetworkChannel);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {

        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();
        String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();

        Channel proxyServerChannel = ChannelHolderContext.getIntranetProxyServerChannel(privateKey);
        if (Objects.isNull(proxyServerChannel) || !proxyServerChannel.isActive()) {
            log.warn("EVENT=CLOSE PROXY SERVER CHANNEL{}", ctx.channel());
            ctx.close();
            return;
        }

        byte[] data = ByteBufUtil.getBytes(msg);
        TransferPacket transferPacket = TransferPacket.INSTANCE.clone();
        transferPacket.setType(MessageType.TRANSFER);
        transferPacket.setSessionId(sessionId);
        transferPacket.setData(data);

        proxyServerChannel.writeAndFlush(transferPacket);

        // 记录流入流量字节数量
        ctx.channel().eventLoop().execute(() -> {
            TrafficEvent trafficEvent = TrafficEvent.INSTANCE.clone();
            trafficEvent.setUserPrivateKye(privateKey);
            trafficEvent.setInFlow(data.length);
            trafficEvent.setOutFlow(0);
            TrafficEventBus.getInstance().post(trafficEvent);
        });

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();
        String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();

        ChannelHolderContext.unBindPublicNetworkChannel(sessionId);
        ctx.close();
        log.warn("EVENT=CLOSE PUBLIC NETWORK PROXY CHANNEL{}", ctx.channel());

        Channel proxyChannel = ChannelHolderContext.getIntranetProxyServerChannel(privateKey);


        if (Objects.nonNull(proxyChannel) && proxyChannel.isActive()) {
            DisconnectPacket disconnectPacket = new DisconnectPacket();
            disconnectPacket.setType(MessageType.DISCONNECT);
            disconnectPacket.setSessionId(sessionId);

            proxyChannel.writeAndFlush(disconnectPacket);
        }

        // 清楚请求链路信息
        NetworkChainTraceTable.remove(sessionId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
        ctx.close();
    }

}
