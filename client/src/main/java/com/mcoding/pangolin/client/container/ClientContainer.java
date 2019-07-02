package com.mcoding.pangolin.client.container;

import com.mcoding.pangolin.client.entity.ProxyInfo;
import com.mcoding.pangolin.client.handler.ProxyClientChannelHandler;
import com.mcoding.pangolin.client.handler.RealServerConnectionHandler;
import com.mcoding.pangolin.client.listener.ChannelStatusListener;
import com.mcoding.pangolin.common.LifeCycle;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import io.netty.bootstrap.Bootstrap;
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
public class ClientContainer implements ChannelStatusListener, LifeCycle {

    private ProxyInfo proxyInfo;
    private Bootstrap proxyServerBootstrap;
    private Bootstrap realServerBootstrap;

    public ClientContainer(ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
        this.init();
    }

    @Override
    public void start() {
        try {
            this.connectProxyServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
    }

    private void init() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        realServerBootstrap = new Bootstrap();
        realServerBootstrap.group(workerGroup);
        realServerBootstrap.channel(NioSocketChannel.class);
        realServerBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        realServerBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new RealServerConnectionHandler());
            }
        });

        proxyServerBootstrap = new Bootstrap();
        proxyServerBootstrap.group(workerGroup);
        proxyServerBootstrap.channel(NioSocketChannel.class);
        proxyServerBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        proxyServerBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                pipeline.addLast(new ProtobufDecoder(PMessageOuterClass.PMessage.getDefaultInstance()));
                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                pipeline.addLast(new ProtobufEncoder());

                pipeline.addLast(new ProxyClientChannelHandler(proxyInfo, realServerBootstrap, ClientContainer.this));
            }
        });

    }


    private void connectProxyServer() throws Exception {
        String proxyServerHost = proxyInfo.getProxyServerHost();
        int proxyPort = proxyInfo.getProxyServerPort();

        ChannelFuture channelFuture = proxyServerBootstrap.connect(proxyServerHost, proxyPort).sync();

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("EVENT=连接代理服务器|HOST={}|PORT={}|CHANNEL={}", proxyServerHost, proxyPort, future.channel());
                } else {
                    connectProxyServer();
                }
            }
        });

        TimeUnit.SECONDS.sleep(5);
    }

    @Override
    public void channelInActive(ChannelHandlerContext channelHandlerContext) {
        this.start();
    }
}
