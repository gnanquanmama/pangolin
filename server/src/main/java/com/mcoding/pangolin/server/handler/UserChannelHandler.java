package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.common.Constants;
import com.mcoding.pangolin.server.user.UserTable;
import com.mcoding.pangolin.server.util.ChannelContextHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;

/**
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
public class UserChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        String userId = ctx.channel().attr(Constants.USER_ID).get();

        Message message = new Message();
        message.setType(Message.TRANSFER);
        message.setUserId(userId);

        byte[] data = ByteBufUtil.getBytes(msg);
        message.setData(data);

        ChannelContextHolder.getProxyServerChannel(userId).writeAndFlush(message);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().localAddress();
        int port = socketAddress.getPort();

        String userId = UserTable.getUserToPortMap().inverse().get(port);
        ctx.channel().attr(Constants.USER_ID).set(userId);

        Channel proxyChannel = ChannelContextHolder.getProxyServerChannel(userId);
        if (proxyChannel == null) {
            System.out.println("代理客户端通道还未建立");
            ctx.channel().close();
        } else if (!proxyChannel.isActive()) {
            System.out.println("代理客户端已不活动");
            ChannelContextHolder.closeProxyServerChannel(userId);
            ctx.channel().close();
        } else {
            System.out.println("公网代理端通道开启: " + ctx.channel());
            ChannelContextHolder.addUserServerChannel(userId, ctx.channel());
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String userId = ctx.channel().attr(Constants.USER_ID).get();
        ChannelContextHolder.closeUserServerChannel(userId);
        System.out.println("公网代理端通道关闭: " + ctx.channel());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.getCause().printStackTrace();
    }
}
