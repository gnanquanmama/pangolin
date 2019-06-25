package com.mcoding.pangolin.client.handler;

import com.mcoding.pangolin.client.container.ClientContainer;
import com.mcoding.pangolin.client.util.ChannelContextHolder;
import com.mcoding.pangolin.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class ProxyClientChannelHandler extends SimpleChannelInboundHandler<Message> {

    private String userId = "1";
    private String realServerHost = "127.0.0.1";
    private Integer realServerPort = 9999;

    private Bootstrap realServerBootstrap;
    private ClientContainer clientContainer;

    public ProxyClientChannelHandler(Bootstrap realServerBootstrap, ClientContainer clientContainer) {
        this.realServerBootstrap = realServerBootstrap;
        this.clientContainer = clientContainer;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Message message) {
        System.out.println("server said : " + new String(message.getData()));

        switch (message.getType()) {
            case Message.TRANSFER:
                this.handleTansfer(ctx, message);
                break;
            default:
                break;
        }
    }

    private void handleTansfer(ChannelHandlerContext ctx, Message message) {
        Channel proxyChannel = ctx.channel();
        Channel userChannel = ChannelContextHolder.getUserChannel();

        userChannel.writeAndFlush(Unpooled.wrappedBuffer(message.getData()));
    }

    private void handleConnectedMessage(ChannelHandlerContext ctx) {
        ChannelFuture futureChannel = this.realServerBootstrap.connect(realServerHost, realServerPort);
        futureChannel.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    System.out.println("连接真实服务器成功");
                } else {
                    System.out.println("连接失败");
                }
            }
        });

    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.handleConnectedMessage(ctx);

        Message connectMsg = new Message();
        connectMsg.setUserId(userId);
        connectMsg.setType(Message.CONNECTING);
        ctx.channel().writeAndFlush(connectMsg);

        ChannelContextHolder.addProxyChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("代理客户端断开连接，关闭user_channel和proxy_channel");
        ChannelContextHolder.closeAll();
        this.clientContainer.channelInActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}