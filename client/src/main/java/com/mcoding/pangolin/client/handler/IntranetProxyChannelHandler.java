package com.mcoding.pangolin.client.handler;

import com.google.protobuf.ByteString;
import com.mcoding.pangolin.client.container.ClientContainer;
import com.mcoding.pangolin.client.entity.ProxyInfo;
import com.mcoding.pangolin.client.util.PangolinChannelContext;
import com.mcoding.pangolin.protocol.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
@Slf4j
public class IntranetProxyChannelHandler extends SimpleChannelInboundHandler<PMessageOuterClass.PMessage> {

    private ProxyInfo proxyInfo;
    private Bootstrap realServerBootstrap;
    private ClientContainer clientContainer;

    public IntranetProxyChannelHandler(ProxyInfo proxyInfo, Bootstrap realServerBootstrap, ClientContainer clientContainer) {
        this.proxyInfo = proxyInfo;
        this.realServerBootstrap = realServerBootstrap;
        this.clientContainer = clientContainer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 发送认证私钥
        PMessageOuterClass.PMessage connectMsg = PMessageOuterClass.PMessage.newBuilder()
                .setPrivateKey(proxyInfo.getPrivateKey())
                .setType(MessageType.AUTH)
                .build();
        ctx.channel().writeAndFlush(connectMsg);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, PMessageOuterClass.PMessage message) {
        switch (message.getType()) {
            case MessageType.AUTH:
                this.handleAuth(ctx, message);
                break;
            case MessageType.DISCONNECT:
                this.handleDisconnect(ctx, message);
                break;
            case MessageType.CONNECT:
                this.handleConnectedMessage(ctx, message);
                break;
            case MessageType.TRANSFER:
                this.handleTransfer(ctx, message);
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

    private void handleAuth(ChannelHandlerContext ctx, PMessageOuterClass.PMessage message) {
        ByteString data = message.getData();
        if (Constants.AUTH_SUCCESS.equalsIgnoreCase(data.toStringUtf8())) {
            ctx.channel().attr(Constants.SESSION_ID).set(message.getSessionId());
            ctx.channel().attr(Constants.PRIVATE_KEY).set(message.getPrivateKey());
            PangolinChannelContext.bindIntranetProxyChannel(ctx.channel());
            log.info("EVENT=认证成功");

        } else {
            log.error("EVENT=认证异常|DESC={}", data.toStringUtf8());
            System.exit(0);
        }
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
        Channel userChannel = PangolinChannelContext.getTargetChannel(sessionId);
        if (Objects.nonNull(userChannel)) {
            log.info("EVENT=连接被代理服务|DESC=通道已连接，不需要重新连接");
            return;
        }

        String realServerHost = proxyInfo.getRealServerHost();
        Integer realServerPort = proxyInfo.getRealServerPort();

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