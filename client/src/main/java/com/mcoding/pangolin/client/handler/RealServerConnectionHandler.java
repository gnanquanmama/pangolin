package com.mcoding.pangolin.client.handler;

import com.google.protobuf.ByteString;
import com.mcoding.pangolin.client.util.PangolinChannelContext;
import com.mcoding.pangolin.protocol.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */

@AllArgsConstructor
@Slf4j
public class RealServerConnectionHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        byte[] content = ByteBufUtil.getBytes(byteBuf);

        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();

        PMessageOuterClass.PMessage respMsg = PMessageOuterClass.PMessage.newBuilder()
                .setType(MessageType.TRANSFER)
                .setData(ByteString.copyFrom(content))
                .setSessionId(sessionId)
                .build();
        Channel proxyChannel = PangolinChannelContext.getProxyChannel();
        proxyChannel.writeAndFlush(respMsg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("EVENT=激活被代理通道");

        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();
        PangolinChannelContext.addUserChannel(sessionId, ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String sessionId = ctx.channel().attr(Constants.SESSION_ID).get();
        Channel userChannel = PangolinChannelContext.getUserChannel(sessionId);
        if (Objects.isNull(userChannel)) {
            log.warn("EVENT=用户通道掉线，关闭用户通道|DESC=已关闭通道|SESSION_ID={}", sessionId);
        } else {
            log.warn("EVENT=用户通道掉线，关闭用户通道{}", PangolinChannelContext.getUserChannel(sessionId));
        }

        PangolinChannelContext.closeUserChannel(sessionId);

        PMessageOuterClass.PMessage disconnectMsg = PMessageOuterClass.PMessage.newBuilder()
                .setSessionId(sessionId)
                .setType(MessageType.DISCONNECT)
                .build();
        PangolinChannelContext.getProxyChannel().writeAndFlush(disconnectMsg);

        log.info("EVENT=关闭公网服务连接通道|SESSION_ID={}", sessionId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("EVENT=用户通道异常|CHANNEL={}|ERROR_DESC={}", ctx.channel(), cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.info("EVENT=管道可写状态变化" + ctx.channel().isWritable());
        super.channelWritabilityChanged(ctx);
    }
}