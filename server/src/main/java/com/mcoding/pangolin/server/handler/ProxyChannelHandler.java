package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.common.Constants;
import com.mcoding.pangolin.server.util.ChannelContextHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class ProxyChannelHandler extends SimpleChannelInboundHandler<Message> {


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("EVENT=激活内网代理端通道{}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();
        ChannelContextHolder.closeProxyServerChannel(privateKey);
        log.warn("EVENT=关闭内网代理端通道{}", ctx.channel());
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        switch (msg.getType()) {
            case Message.AUTH:
                handleAuth(ctx, msg);
                break;
            case Message.CONNECT:
                handleConnect(ctx, msg);
                break;
            case Message.TRANSFER:
                handleTransfer(msg);
                break;
            default:
                break;
        }
    }

    private void handleConnect(ChannelHandlerContext ctx, Message msg) {
        Channel userChannel = ChannelContextHolder.getUserServerChannel(msg.getSessionId());
        userChannel.config().setAutoRead(true);
    }

    private void handleAuth(ChannelHandlerContext ctx, Message msg) {
        String privateKey = msg.getPrivateKey();
        ctx.channel().attr(Constants.PRIVATE_KEY).set(privateKey);
        ChannelContextHolder.addProxyServerChannel(privateKey, ctx.channel());
        log.info("EVENT=连接认证处理|DESC=认证通过|PRIVATE_KEY={}", privateKey);
    }

    private void handleTransfer(Message msg) {
        Channel userChannel = ChannelContextHolder.getUserServerChannel(msg.getSessionId());
        if (Objects.nonNull(userChannel) && userChannel.isWritable()) {
            userChannel.writeAndFlush(Unpooled.wrappedBuffer(msg.getData()));
            log.info("EVENT=传输数据监控|CHANNEL={}|LENGTH={}", userChannel, msg.getData().length);
        }
    }

}
