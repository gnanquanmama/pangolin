package com.mcoding.pangolin.handler;

import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.contants.EndPointUnDirectedPath;
import com.mcoding.pangolin.context.TunnelContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */

@AllArgsConstructor
public class RealServerConnectionHandler extends ChannelInboundHandlerAdapter {

    private Integer remoteProxyPort;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("连接真实服务成功");
        TunnelContext.put(EndPointUnDirectedPath.BASE_CLIENT_TO_REAL_SERVER, 0, ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;

        byte[] content = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(content);
        System.out.println("real server response content : " + new String(content));
        byteBuf.release();

        Message respMsg = new Message();
        respMsg.setType(Message.TRANSFER);
        respMsg.setProxyPort(remoteProxyPort);
        respMsg.setData(content);

        Channel baseChannel = TunnelContext.get(EndPointUnDirectedPath.BASE_SERVER_TO_BASE_CLIENT, 0);
        baseChannel.writeAndFlush(respMsg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接掉线了");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}