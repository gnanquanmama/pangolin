package com.mcoding.pangolin.server.handler;

import com.google.protobuf.ByteString;
import com.mcoding.pangolin.protocol.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class UserChannelHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static AtomicLong atomicLong = new AtomicLong();

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();
        String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();

        ChannelContextHolder.closeUserServerChannel(sessionId);
        ctx.close();
        log.warn("EVENT=关闭公网代理端通道{}", ctx.channel());

        Channel proxyChannel = ChannelContextHolder.getProxyServerChannel(privateKey);

        PMessageOuterClass.PMessage disconnectMsg = PMessageOuterClass.PMessage.newBuilder()
                .setType(MessageType.DISCONNECT)
                .setSessionId(sessionId)
                .build();

        proxyChannel.writeAndFlush(disconnectMsg);

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();
        String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();
        byte[] data = ByteBufUtil.getBytes(msg);

        PMessageOuterClass.PMessage disconnectMsg = PMessageOuterClass.PMessage.newBuilder()
                .setType(MessageType.TRANSFER)
                .setSessionId(sessionId)
                .setData(ByteString.copyFrom(data))
                .build();

        Channel proxyServerChannel = ChannelContextHolder.getProxyServerChannel(privateKey);
        if (Objects.isNull(proxyServerChannel) || !proxyServerChannel.isActive()) {
            log.warn("EVENT=关闭代理服务代理管道{}", ctx.channel());
            ctx.close();
            return;
        }

        proxyServerChannel.writeAndFlush(disconnectMsg);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        InetSocketAddress localSocketAddress = (InetSocketAddress) ctx.channel().localAddress();
        int port = localSocketAddress.getPort();

        String privateKey = UserTable.getUserToPortMap().inverse().get(port);
        Channel proxyChannel = ChannelContextHolder.getProxyServerChannel(privateKey);
        if (proxyChannel == null) {
            log.warn("代理客户端通道还未建立");
            ctx.channel().close();
            return;
        }

        if (!proxyChannel.isActive()) {
            System.out.println();
            log.warn("EVENT=关闭未激活代理客户端通道{}", ctx.channel());
            ChannelContextHolder.closeProxyServerChannel(privateKey);
            ctx.channel().close();
            return;
        }

        log.info("EVENT=激活公网代理端通道:{}", ctx.channel());
        Channel userChannel = ctx.channel();
        userChannel.config().setAutoRead(false);

        String sessionId = this.generateSessionId();
        userChannel.attr(Constants.SESSION_ID).set(sessionId);
        userChannel.attr(Constants.PRIVATE_KEY).set(privateKey);


        PMessageOuterClass.PMessage connectMsg = PMessageOuterClass.PMessage.newBuilder()
                .setType(MessageType.CONNECT)
                .setSessionId(sessionId)
                .build();

        proxyChannel.writeAndFlush(connectMsg);

        ChannelContextHolder.addUserServerChannel(sessionId, userChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.getCause().printStackTrace();
    }

    private String generateSessionId() {
        return String.valueOf(atomicLong.incrementAndGet());
    }

}
