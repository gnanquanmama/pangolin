package com.mcoding.pangolin.client.handler;

import com.mcoding.pangolin.client.container.ClientContainer;
import com.mcoding.pangolin.client.context.PangolinChannelContext;
import com.mcoding.pangolin.client.entity.AddressBridgeInfo;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
@Slf4j
public class IntranetProxyChannelHandler extends SimpleChannelInboundHandler<PMessageOuterClass.PMessage> {

    private AddressBridgeInfo addressBridgeInfo;
    private Bootstrap realServerBootstrap;
    private ClientContainer clientContainer;

    public IntranetProxyChannelHandler(AddressBridgeInfo addressBridgeInfo, Bootstrap realServerBootstrap, ClientContainer clientContainer) {
        this.addressBridgeInfo = addressBridgeInfo;
        this.realServerBootstrap = realServerBootstrap;
        this.clientContainer = clientContainer;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, PMessageOuterClass.PMessage message) {
        switch (message.getType()) {
            case MessageType.TRANSFER:
                this.handleTransfer(ctx, message);
                break;
            case MessageType.DISCONNECT:
                this.handleDisconnect(ctx, message);
                break;
            case MessageType.CONNECT:
                this.handleConnectedMessage(ctx, message);
                break;
            default:
                break;
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn("EVENT=代理通道掉线，关闭所有通道{}", PangolinChannelContext.getAllChannelList());

        PangolinChannelContext.unBindAll();
        this.clientContainer.channelInActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void handleDisconnect(ChannelHandlerContext ctx, PMessageOuterClass.PMessage message) {
        String sessionId = message.getSessionId();
        PangolinChannelContext.unBindTargetServerChannel(sessionId);
        log.info("EVENT=公网访问连接断开，关闭被代理服务器通道");
    }

    private void handleTransfer(ChannelHandlerContext ctx, PMessageOuterClass.PMessage message) {
        Channel userChannel = PangolinChannelContext.getTargetChannel(message.getSessionId());
        userChannel.writeAndFlush(Unpooled.wrappedBuffer(message.getData().toByteArray()));
    }

    private void handleConnectedMessage(ChannelHandlerContext ctx, PMessageOuterClass.PMessage message) {
        String sessionId = message.getSessionId();
        Channel targetChannel = PangolinChannelContext.getTargetChannel(sessionId);
        if (Objects.nonNull(targetChannel)) {
            log.info("EVENT=连接被代理服务|DESC=通道已连接，不需要重新连接");
            return;
        }

        String realServerHost = addressBridgeInfo.getTargetServerHost();
        Integer realServerPort = addressBridgeInfo.getTargetServerPort();

        realServerBootstrap
                .connect(realServerHost, realServerPort)
                .addListener((ChannelFuture future) -> {
                    if (!future.isSuccess()) {
                        log.error("EVENT=连接被代理服务器失败");
                        return;
                    }

                    log.info("EVENT=连接被代理服务器成功|HOST={}|PORT={}|CHANNEL={}", realServerHost, realServerPort, future.channel());
                    future.channel().attr(Constants.SESSION_ID).set(sessionId);
                    PangolinChannelContext.bindTargetServerChannel(sessionId, future.channel());

                    PMessageOuterClass.PMessage confirmConnectMsg = PMessageOuterClass.PMessage.newBuilder()
                            .setSessionId(sessionId).setType(MessageType.CONNECT).build();

                    ctx.channel().writeAndFlush(confirmConnectMsg);
                });
    }

}