package com.mcoding.pangolin.server.handler;

import com.google.protobuf.ByteString;
import com.mcoding.pangolin.protocol.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import com.mcoding.pangolin.server.util.PublicNetworkPortTable;
import com.mcoding.pangolin.server.util.PangolinChannelContext;
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
public class ProxyChannelHandler extends SimpleChannelInboundHandler<PMessageOuterClass.PMessage> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("EVENT=激活内网代理端通道{}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();
        PangolinChannelContext.closeProxyServerChannel(privateKey);
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
        log.warn("EVENT=断开外网连接通道|DESC=被代理服务器通道已关闭|SESSION_ID={}", msg.getSessionId());
        PangolinChannelContext.closeUserServerChannel(msg.getSessionId());
    }

    private void handleConnect(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        Channel userChannel = PangolinChannelContext.getUserServerChannel(msg.getSessionId());
        userChannel.config().setAutoRead(true);
    }

    private void handleAuth(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg) {
        String privateKey = msg.getPrivateKey();

        PMessageOuterClass.PMessage.Builder authMsgBuilder = PMessageOuterClass.PMessage.newBuilder()
                .setSessionId(msg.getSessionId())
                .setType(MessageType.AUTH)
                .setPrivateKey(privateKey)
                .setData(ByteString.copyFrom(Constants.AUTH_SUCCESS.getBytes()));

        Integer publicPort = PublicNetworkPortTable.getUserToPortMap().get(privateKey);
        if (Objects.isNull(publicPort)) {
            authMsgBuilder.setData(ByteString.copyFrom("私钥不存在，请在服务端配置后，再连接".getBytes()));
            ctx.writeAndFlush(authMsgBuilder.build());
            log.error("EVENT=认证失败，不存在PRIVATE_KEY={}", privateKey);
            return;
        }

        ctx.writeAndFlush(authMsgBuilder.build());
        ctx.channel().attr(Constants.PRIVATE_KEY).set(privateKey);
        PangolinChannelContext.addProxyServerChannel(privateKey, ctx.channel());
        log.info("EVENT=连接认证处理|DESC=认证通过|PRIVATE_KEY={}", privateKey);
    }

    private void handleTransfer(PMessageOuterClass.PMessage msg) {
        Channel userChannel = PangolinChannelContext.getUserServerChannel(msg.getSessionId());
        userChannel.writeAndFlush(Unpooled.wrappedBuffer(msg.getData().toByteArray()));
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
