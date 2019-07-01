package com.mcoding.pangolin.client.handler;

import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.client.util.ChannelContextHolder;
import com.mcoding.pangolin.common.Constants;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */

@AllArgsConstructor
@Slf4j
public class RealServerConnectionHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        byte[] content = ByteBufUtil.getBytes(byteBuf);

        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();
        Message respMsg = new Message();
        respMsg.setType(Message.TRANSFER);
        respMsg.setSessionId(sessionId);
        respMsg.setData(content);

        log.info("EVENT=被代理服务返回信息|字节长度={}", content.length);

        Channel proxyChannel = ChannelContextHolder.getProxyChannel();
        proxyChannel.writeAndFlush(respMsg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("EVENT=激活被代理通道");

        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();
        ChannelContextHolder.addUserChannel(sessionId, ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();
        log.warn("EVENT=用户通道掉线，关闭所有通道{}", ChannelContextHolder.getUserChannel(sessionId));

        ChannelContextHolder.closeUserChannel(sessionId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}