package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.common.Constants;
import com.mcoding.pangolin.server.util.ChannelContextHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Objects;

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
        ctx.channel().attr(Constants.USER_ID).set(userId);
        ChannelContextHolder.addProxyServerChannel(userId, ctx.channel());
    }

    private void handleTransfer(ChannelHandlerContext ctx, Message msg) {
        String userId = ctx.channel().attr(Constants.USER_ID).get();
        Channel userChannel = ChannelContextHolder.getUserServerChannel(userId);
        if (Objects.nonNull(userChannel) && userChannel.isWritable()) {
            userChannel.writeAndFlush(Unpooled.wrappedBuffer(msg.getData()));
            System.out.println("传输数据：" + userChannel);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("内网代理端通道开启：" + ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String userId = ctx.channel().attr(Constants.USER_ID).get();
        ChannelContextHolder.closeUserServerChannel(userId);
        System.out.println("内网代理端通道关闭：" + ctx.channel());
        ctx.close();

    }

}
