package com.mcoding.pangolin.client.listener;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author wzt on 2019/6/25.
 * @version 1.0
 */
public interface ChannelStatusListener {

    void channelInActive(ChannelHandlerContext channelHandlerContext);

}
