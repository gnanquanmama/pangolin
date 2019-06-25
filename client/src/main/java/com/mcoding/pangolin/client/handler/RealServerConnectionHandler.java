package com.mcoding.pangolin.client.handler;

import com.mcoding.pangolin.client.util.ChannelContextHolder;
import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.common.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */

@AllArgsConstructor
public class RealServerConnectionHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        byte[] content = ByteBufUtil.getBytes(byteBuf);

        String userId = ctx.channel().attr(Constants.USER_ID).get();
        Message respMsg = new Message();
        respMsg.setType(Message.TRANSFER);
        respMsg.setUserId(userId);
        respMsg.setData(content);

        try {
            System.out.println("real server response content : " + content.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Channel proxyChannel = ChannelContextHolder.getProxyChannel();
        proxyChannel.writeAndFlush(respMsg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ChannelContextHolder.addUserChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("用户连接掉线了,关闭user_channel和proxy_channel");
        ChannelContextHolder.closeAll();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}