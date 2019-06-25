package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.server.context.ChannelContextHolder;
import com.mcoding.pangolin.common.Constants;
import com.mcoding.pangolin.server.user.UserTable;
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

        Message message = new Message();
        message.setType(Message.TRANSFER);
        message.setUserId(userId);

        byte[] data = ByteBufUtil.getBytes(msg);
        message.setData(data);
        proxyChannel.writeAndFlush(message);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().localAddress();
        int port = socketAddress.getPort();

        String userId = UserTable.getUserToPortMap().inverse().get(port);


        Channel proxyChannel = ChannelContextHolder.getProxyChannelByUserId(userId);
        if (proxyChannel == null) {
            System.out.println("代理客户端还未访问，关闭通道");
            ctx.channel().close();
        }

        System.out.println("channel Active channel is " + ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("UserChannelHandler inactive");
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.getCause().printStackTrace();
    }
}
