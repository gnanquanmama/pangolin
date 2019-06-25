package com.mcoding.pangolin.client.container;

import com.mcoding.pangolin.client.handler.ProxyClientChannelHandler;
import com.mcoding.pangolin.client.handler.RealServerConnectionHandler;
import com.mcoding.pangolin.client.listener.ChannelStatusListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.util.concurrent.TimeUnit;

/**
 * @author wzt on 2019/6/19.
 * @version 1.0
 */
public class ClientContainer implements ChannelStatusListener {

    private String proxyServerHost;
    private Integer proxyPort;

    private Bootstrap proxyServerBootstrap;
    private Bootstrap realServerBootstrap;

    public ClientContainer(String proxyServerHost, Integer proxyPort) {
        this.proxyServerHost = proxyServerHost;
        this.proxyPort = proxyPort;
        this.init();
    }

    private void init() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        realServerBootstrap = new Bootstrap();
        realServerBootstrap.group(workerGroup);
        realServerBootstrap.channel(NioSocketChannel.class);
        realServerBootstrap.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
        realServerBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        realServerBootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, Integer.MAX_VALUE));
        realServerBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new RealServerConnectionHandler());
            }
        });

        proxyServerBootstrap = new Bootstrap();
        proxyServerBootstrap.group(workerGroup);
        proxyServerBootstrap.channel(NioSocketChannel.class);
        proxyServerBootstrap.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT);
        proxyServerBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        proxyServerBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new ObjectEncoder());
                ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                ch.pipeline().addLast(new ProxyClientChannelHandler(realServerBootstrap, ClientContainer.this));
            }
        });

    }


    private void connectProxyServer() throws Exception {
        ChannelFuture channelFuture = proxyServerBootstrap.connect(proxyServerHost, proxyPort).sync();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("连接代理服务器成功~~~");

                } else {
                    TimeUnit.SECONDS.sleep(2);
                    connectProxyServer();
                }
            }
        });
    }

    public void start() {
        try {
            this.connectProxyServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void channelInActive(ChannelHandlerContext channelHandlerContext) {
        this.start();
    }
}
