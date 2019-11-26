package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.common.codec.ConnectPacket;
import com.mcoding.pangolin.server.context.ChannelHolderContext;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 被代理服务器已连接处理器
 *
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class IntranetTargetServerConnectedHandler extends SimpleChannelInboundHandler<ConnectPacket> {

    public static final IntranetTargetServerConnectedHandler INSTANCE = new IntranetTargetServerConnectedHandler();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ConnectPacket packet) {
        Channel publicNetworkChannel = ChannelHolderContext.getPublicNetworkChannel(packet.getSessionId());
        publicNetworkChannel.config().setAutoRead(true);
    }

}
