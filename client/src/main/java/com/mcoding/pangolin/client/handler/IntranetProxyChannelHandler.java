package com.mcoding.pangolin.client.handler;

import com.mcoding.pangolin.client.container.ClientBootstrapContainer;
import com.mcoding.pangolin.client.context.ChannelHolderContext;
import com.mcoding.pangolin.client.model.AddressBridge;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
@Slf4j
public class IntranetProxyChannelHandler extends SimpleChannelInboundHandler<PMessageOuterClass.PMessage> {

    private AddressBridge addressBridge;
    private Bootstrap realServerBootstrap;
    private ClientBootstrapContainer clientBootstrapContainer;

    public IntranetProxyChannelHandler(AddressBridge addressBridge, Bootstrap realServerBootstrap, ClientBootstrapContainer clientBootstrapContainer) {
        this.addressBridge = addressBridge;
        this.realServerBootstrap = realServerBootstrap;
        this.clientBootstrapContainer = clientBootstrapContainer;
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
        log.warn("EVENT=内网代理通道掉线，关闭所有通道{}", ChannelHolderContext.getAllChannelList());

        ChannelHolderContext.unBindAll();
        this.clientBootstrapContainer.channelInActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void handleDisconnect(ChannelHandlerContext ctx, PMessageOuterClass.PMessage message) {
        String sessionId = message.getSessionId();
        ChannelHolderContext.unBindTargetServerChannel(sessionId);
        log.info("EVENT=公网访问连接断开，关闭被代理服务器通道");
    }

    private void handleTransfer(ChannelHandlerContext ctx, PMessageOuterClass.PMessage message) {
        Channel targetChannel = ChannelHolderContext.getTargetChannel(message.getSessionId());
        ByteBuf byteBuf = ctx.alloc().ioBuffer().writeBytes((message.getData().toByteArray()));
        targetChannel.writeAndFlush(byteBuf);
    }

    private void handleConnectedMessage(ChannelHandlerContext ctx, PMessageOuterClass.PMessage message) {
        String sessionId = message.getSessionId();
        Channel targetChannel = ChannelHolderContext.getTargetChannel(sessionId);
        if (Objects.nonNull(targetChannel)) {
            log.info("EVENT=连接被代理服务|DESC=通道已连接，不需要重新连接");
            return;
        }

        String realServerHost = addressBridge.getTargetServerHost();
        Integer realServerPort = addressBridge.getTargetServerPort();

        try {
            realServerBootstrap
                    .connect(realServerHost, realServerPort)
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) {
                            if (!future.isSuccess()) {
                                log.error("EVENT=连接被代理服务器失败|DESC={}", future.cause().getMessage());
                                PMessageOuterClass.PMessage disConnectMsg = PMessageOuterClass.PMessage.newBuilder()
                                        .setSessionId(sessionId).setType(MessageType.DISCONNECT).build();
                                ctx.channel().writeAndFlush(disConnectMsg);

                                return;
                            }

                            log.info("EVENT=连接被代理服务器成功|HOST={}|PORT={}|CHANNEL={}", realServerHost, realServerPort, future.channel());
                            future.channel().attr(Constants.SESSION_ID).set(sessionId);
                            ChannelHolderContext.bindTargetServerChannel(sessionId, future.channel());

                            PMessageOuterClass.PMessage confirmConnectMsg = PMessageOuterClass.PMessage.newBuilder()
                                    .setSessionId(sessionId).setType(MessageType.CONNECT).build();

                            ctx.channel().writeAndFlush(confirmConnectMsg);
                        }
                    }).await();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}