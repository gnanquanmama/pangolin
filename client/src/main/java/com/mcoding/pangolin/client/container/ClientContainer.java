package com.mcoding.pangolin.client.container;

import com.mcoding.pangolin.client.entity.ProxyInfo;
import com.mcoding.pangolin.client.handler.HeartBeatHandler;
import com.mcoding.pangolin.client.handler.IntranetProxyChannelHandler;
import com.mcoding.pangolin.client.handler.TargetServerChannelHandler;
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
    private Bootstrap intranetProxyClientBootstrap;
    private Bootstrap targetServerClientBootstrap;

    public ClientContainer(ProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
        this.init();
    }

    @Override
    public void start() {
        try {
            this.connectIntranetProxyServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
    }

    private void init() {
        targetServerClientBootstrap = new Bootstrap();
        targetServerClientBootstrap.group(new NioEventLoopGroup());
        targetServerClientBootstrap.channel(NioSocketChannel.class);
        targetServerClientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        targetServerClientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new TargetServerChannelHandler());
            }
        });

        intranetProxyClientBootstrap = new Bootstrap();
        intranetProxyClientBootstrap.group(new NioEventLoopGroup());
        intranetProxyClientBootstrap.channel(NioSocketChannel.class);
        intranetProxyClientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        intranetProxyClientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new HeartBeatHandler(0, 60, 0, TimeUnit.MINUTES));
                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                pipeline.addLast(new ProtobufDecoder(PMessageOuterClass.PMessage.getDefaultInstance()));
                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                pipeline.addLast(new ProtobufEncoder());

                pipeline.addLast(new IntranetProxyChannelHandler(proxyInfo, targetServerClientBootstrap, ClientContainer.this));
            }
        });

    }

    /**
     * 连接内网代理服务器
     *
     * @throws Exception
     */
    private void connectIntranetProxyServer() throws Exception {
        String proxyServerHost = proxyInfo.getProxyServerHost();
        int proxyPort = proxyInfo.getProxyServerPort();

        ChannelFuture channelFuture = intranetProxyClientBootstrap.connect(proxyServerHost, proxyPort).sync();
        channelFuture.addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                log.info("EVENT=连接代理服务器|HOST={}|PORT={}|CHANNEL={}", proxyServerHost, proxyPort, future.channel());
            } else {
                TimeUnit.SECONDS.sleep(5);
                connectIntranetProxyServer();
            }
        });
    }

    @Override
    public void channelInActive(ChannelHandlerContext channelHandlerContext) {
        this.start();
    }
}
