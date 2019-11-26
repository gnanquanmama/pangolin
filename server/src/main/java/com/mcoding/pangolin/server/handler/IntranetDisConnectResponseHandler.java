package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.common.codec.DisconnectPacket;
import com.mcoding.pangolin.server.context.ChannelHolderContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 内网代理通道关闭处理器
 *
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class IntranetDisConnectResponseHandler extends SimpleChannelInboundHandler<DisconnectPacket> {

    public static final IntranetDisConnectResponseHandler INSTANCE = new IntranetDisConnectResponseHandler();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DisconnectPacket packet) {
        log.warn("EVENT=断开外网连接通道|DESC=被代理服务器通道已关闭|SESSION_ID={}", packet.getSessionId());
        ChannelHolderContext.unBindPublicNetworkChannel(packet.getSessionId());
    }

}
