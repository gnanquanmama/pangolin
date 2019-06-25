package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.common.ChannelContextHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
public class ProxyChannelHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        switch (msg.getType()) {
            case Message.CONNECTING:
                handleConnection(ctx, msg);
                break;
            case Message.TRANSFER:
                handleTransfer(ctx, msg);
                break;
            default:
                break;
        }

    }

    private void handleConnection(ChannelHandlerContext ctx, Message msg) {
        String userId = msg.getUserId();

        ChannelContextHolder.addProxyChannel(ctx.channel());
    }

    private void handleTransfer(ChannelHandlerContext ctx, Message msg) {
        String userId = msg.getUserId();
        Channel userChannel = ChannelContextHolder.getUserChannelByUserId(userId);
        if (userChannel.isWritable()) {
            System.out.println("receive data : " + new String(msg.getData()));

            userChannel.writeAndFlush(Unpooled.wrappedBuffer(msg.getData()));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("ProxyChannelHandler channelInactive");
        ctx.close();
    }

}
