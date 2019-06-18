package com.mcoding.pangolin.handler;

import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.contants.EndPointUnDirectedPath;
import com.mcoding.pangolin.context.TunnelContext;
import com.mcoding.pangolin.task.ConnectRealServerTask;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
@AllArgsConstructor
public class BaseConnectionHandler extends ChannelInboundHandlerAdapter {

    private String key;
    private Integer proxyPort;

    private String realServerHost;
    private Integer realServerPort;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Message message = new Message();
        message.setKey(key);
        message.setProxyPort(proxyPort);
        message.setType(Message.CONNECTING);
        ctx.writeAndFlush(message);

        new Thread(new ConnectRealServerTask(proxyPort, realServerHost, realServerPort)).start();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Message message = (Message) msg;
        System.out.println("server said : " + new String(message.getData()));

        Channel realChannel = TunnelContext.get(EndPointUnDirectedPath.BASE_CLIENT_TO_REAL_SERVER, 0);
        realChannel.writeAndFlush(Unpooled.copiedBuffer(new String(message.getData()), CharsetUtil.UTF_8));


        System.out.println(realChannel.isActive());
        System.out.println(realChannel.isOpen());
        System.out.println(realChannel.isRegistered());
        System.out.println(realChannel.isWritable());
        System.out.println("realchannel : " + realChannel.toString());

        TunnelContext.put(EndPointUnDirectedPath.BASE_SERVER_TO_BASE_CLIENT, 0, ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}