package com.mcoding.pangolin.client.container;

import com.mcoding.pangolin.client.handler.HeartBeatHandler;
import com.mcoding.pangolin.client.handler.IntranetProxyChannelHandler;
import com.mcoding.pangolin.client.handler.LoginRequestHandler;
import com.mcoding.pangolin.client.handler.TargetServerChannelHandler;
import com.mcoding.pangolin.client.listener.ChannelStatusListener;
import com.mcoding.pangolin.client.model.AddressBridge;
import com.mcoding.pangolin.client.util.ThreadUtils;
import com.mcoding.pangolin.common.LifeCycle;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author wzt on 2019/6/19.
 * @version 1.0
 */
@Slf4j
public class ClientBootstrapContainer implements ChannelStatusListener, LifeCycle {

    private AddressBridge addressBridge;
    private Bootstrap intranetProxyClientBootstrap;
    private Bootstrap targetServerClientBootstrap;

    public ClientBootstrapContainer(AddressBridge addressBridge) {
        this.addressBridge = addressBridge;
        this.init();
    }

    @Override
    public void start() {
        this.connectIntranetProxyServer();
    }

    @Override
    public void close() {
    }

    private void init() {
        targetServerClientBootstrap = new Bootstrap();
        targetServerClientBootstrap.group(new NioEventLoopGroup(1));
        targetServerClientBootstrap.channel(NioSocketChannel.class);
        targetServerClientBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        targetServerClientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        targetServerClientBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        targetServerClientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(TargetServerChannelHandler.INSTANCE);
            }
        });

        intranetProxyClientBootstrap = new Bootstrap();
        intranetProxyClientBootstrap.group(new NioEventLoopGroup(1));
        intranetProxyClientBootstrap.channel(NioSocketChannel.class);
        intranetProxyClientBootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        intranetProxyClientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        intranetProxyClientBootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        intranetProxyClientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(HeartBeatHandler.INSTANCE);
                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                pipeline.addLast(new ProtobufDecoder(PMessageOuterClass.PMessage.getDefaultInstance()));
                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                pipeline.addLast(new ProtobufEncoder());

                pipeline.addLast(new LoginRequestHandler(addressBridge.getPrivateKey()));
                pipeline.addLast(new IntranetProxyChannelHandler(addressBridge, targetServerClientBootstrap, ClientBootstrapContainer.this));
            }
        });

    }

    /**
     * 连接内网代理服务器
     */
    private void connectIntranetProxyServer() {
        String intranetProxyServerHost = addressBridge.getIntranetProxyServerHost();
        int intranetProxyPort = addressBridge.getIntranetProxyServerPort();

        try {
            intranetProxyClientBootstrap
                    .connect(intranetProxyServerHost, intranetProxyPort)
                    .addListener((ChannelFuture future) -> {
                        if (future.isSuccess()) {
                            log.info("EVENT=CONNECT INTRANET PROXY SERVER|HOST={}|PORT={}|CHANNEL={}", intranetProxyServerHost, intranetProxyPort, future.channel());
                        } else {
                            log.error(future.cause().getMessage());
                            ThreadUtils.sleep(10, TimeUnit.SECONDS);
                            connectIntranetProxyServer();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
            ThreadUtils.sleep(10, TimeUnit.SECONDS);
            this.connectIntranetProxyServer();
        }
    }

    @Override
    public void channelInActive(ChannelHandlerContext channelHandlerContext) {
        this.start();
    }
}
