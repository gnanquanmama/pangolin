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
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class UserChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        String userId = ctx.channel().attr(Constants.USER_ID).get();

        Message message = new Message();
        message.setType(Message.TRANSFER);
        message.setUserId(userId);

        byte[] data = ByteBufUtil.getBytes(msg);
        message.setData(data);

        Channel proxyServerChannel = ChannelContextHolder.getProxyServerChannel(userId);

        if (Objects.isNull(proxyServerChannel) || !proxyServerChannel.isActive()) {
            log.warn("EVENT=关闭代理服务代理管道{}", ctx.channel());
            ctx.close();
            return;
        }

        proxyServerChannel.writeAndFlush(message);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress localSocketAddress = (InetSocketAddress) ctx.channel().localAddress();
        int port = localSocketAddress.getPort();

        String userId = UserTable.getUserToPortMap().inverse().get(port);
        ctx.channel().attr(Constants.USER_ID).set(userId);

        Channel proxyChannel = ChannelContextHolder.getProxyServerChannel(userId);
        if (proxyChannel == null) {
            log.warn("代理客户端通道还未建立");
            ctx.channel().close();
            return;
        }

        if (!proxyChannel.isActive()) {
            System.out.println();
            log.warn("EVENT=关闭未激活代理客户端通道{}", ctx.channel());
            ChannelContextHolder.closeProxyServerChannel(userId);
            ctx.channel().close();
            return;
        }

        log.info("EVENT=激活公网代理端通道:{}", ctx.channel());
        Channel currentChannel = ChannelContextHolder.getUserServerChannel(userId);
        if (Objects.nonNull(currentChannel)) {
            log.info("端口{}通道{}已建立，不保存新的通道", ctx.channel(), port);
            ctx.close();
            return;
        }

        ChannelContextHolder.addUserServerChannel(userId, ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String userId = ctx.channel().attr(Constants.USER_ID).get();
        ChannelContextHolder.closeUserServerChannel(userId);
        ctx.close();
        log.warn("EVENT=关闭公网代理端通道{}", ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.getCause().printStackTrace();
    }

}
