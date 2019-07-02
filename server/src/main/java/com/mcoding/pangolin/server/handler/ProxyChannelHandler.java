package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.common.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import com.mcoding.pangolin.server.util.ChannelContextHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class ProxyChannelHandler extends SimpleChannelInboundHandler<PMessageOuterClass.PMessage> {

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
    protected void channelRead0(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        switch (msg.getType()) {
            case MessageType.AUTH:
                handleAuth(ctx, msg);
                break;
            case MessageType.CONNECT:
                handleConnect(ctx, msg);
                break;
            case MessageType.TRANSFER:
                handleTransfer(msg);
                break;
            case MessageType.DISCONNECT:
                handleDisconnect(ctx, msg);
                break;
            default:
                break;
        }
    }

    private void handleDisconnect(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        log.warn("event=断开外网连接通道|DESC=被代理服务器通道已关闭|SESSION_ID={}", msg.getSessionId());
        ChannelContextHolder.closeUserServerChannel(msg.getSessionId());
    }

    private void handleConnect(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        Channel userChannel = ChannelContextHolder.getUserServerChannel(msg.getSessionId());
        userChannel.config().setAutoRead(true);
    }

    private void handleAuth(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        String privateKey = msg.getPrivateKey();
        ctx.channel().attr(Constants.PRIVATE_KEY).set(privateKey);
        ChannelContextHolder.addProxyServerChannel(privateKey, ctx.channel());
        log.info("EVENT=连接认证处理|DESC=认证通过|PRIVATE_KEY={}", privateKey);
    }

    private void handleTransfer(PMessageOuterClass.PMessage msg) {
        Channel userChannel = ChannelContextHolder.getUserServerChannel(msg.getSessionId());
        userChannel.writeAndFlush(Unpooled.wrappedBuffer(msg.getData().toByteArray()));
        log.info("EVENT=传输数据监控|CHANNEL={}|LENGTH={}", userChannel, msg.getData().toByteArray().length);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.info("event=代理管道可写状态变化" + ctx.channel().isWritable());

        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("EVENT=代理通道异常|CHANNEL={}|ERROR_MSG={}", ctx.channel(), cause.getMessage());
    }
}
