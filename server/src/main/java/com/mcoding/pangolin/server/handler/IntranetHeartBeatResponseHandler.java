package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.common.codec.HeartBeatPacket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 心跳处理器
 *
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class IntranetHeartBeatResponseHandler extends SimpleChannelInboundHandler<HeartBeatPacket> {

    public static final IntranetHeartBeatResponseHandler INSTANCE =new IntranetHeartBeatResponseHandler();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HeartBeatPacket packet) {
        log.info("EVENT=RECEIVE HEARTBEAT PACKET|MSG={}", new String(packet.getData()));
    }

}
