package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.common.codec.DisconnectPacket;
import com.mcoding.pangolin.server.context.PangolinChannelContext;
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
public class IntranetDisConnectResponseHandler extends SimpleChannelInboundHandler<DisconnectPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DisconnectPacket packet) {
        log.warn("EVENT=断开外网连接通道|DESC=被代理服务器通道已关闭|SESSION_ID={}", packet.getSessionId());
        PangolinChannelContext.unBindPublicNetworkChannel(packet.getSessionId());
    }

}
