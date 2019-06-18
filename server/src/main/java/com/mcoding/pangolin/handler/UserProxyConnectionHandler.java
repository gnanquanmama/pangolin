package com.mcoding.pangolin.handler;

import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.contants.EndPointUnDirectedPath;
import com.mcoding.pangolin.context.TunnelContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;

/**
 *
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
@AllArgsConstructor
public class UserProxyConnectionHandler extends SimpleChannelInboundHandler {

    private Integer proxyPort;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        ByteBuf msgBuf = (ByteBuf) msg;
        byte[] content = new byte[msgBuf.readableBytes()];
        msgBuf.readBytes(content);

        System.out.println(new String(content));

        Channel baseChannel = TunnelContext.get(EndPointUnDirectedPath.BASE_SERVER_TO_BASE_CLIENT, proxyPort);
        Message message = new Message();
        message.setType(Message.TRANSFER);
        message.setProxyPort(proxyPort);
        message.setData(content);
        baseChannel.writeAndFlush(message);

        TunnelContext.put(EndPointUnDirectedPath.APP_TO_PROXY_SERVER, proxyPort, ctx.channel());
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

}