package com.mcoding.pangolin.server.container;

import com.mcoding.pangolin.common.LifeCycle;
import com.mcoding.pangolin.server.context.PublicNetworkPortTable;
import com.mcoding.pangolin.server.handler.PublicNetWorkChannelHandler;
import com.mcoding.pangolin.server.handler.ServerIdleStateHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 基础通道服务容器
 *
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class PublicNetServerBootstrapContainer implements LifeCycle {

    private EventLoopGroup pubNetBossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup pubNetWorkerGroup = new NioEventLoopGroup();

    @Override
    public void start() {
        this.startPublicNetworkChannelServer();
    }

    @Override
    public void close() {
        pubNetBossGroup.shutdownGracefully();
        pubNetWorkerGroup.shutdownGracefully();
    }

    /**
     * 开启用户公网通道服务
     */
    private void startPublicNetworkChannelServer() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(pubNetBossGroup, pubNetWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 100)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ServerIdleStateHandler());
                        pipeline.addLast(PublicNetWorkChannelHandler.INSTANCE);
                    }
                });

        PublicNetworkPortTable.getUserToPortMap().forEach((userId, proxyPort) -> {
            serverBootstrap.bind(proxyPort).addListener(listener -> {
                if (listener.isSuccess()) {
                    log.info("EVENT=OPEN PUBLIC NETWORK PORT [{}]", proxyPort);
                } else {
                    log.error("EVENT=OPEN PUBLIC NETWORK PORT [{}]|exception {}",
                            serverBootstrap, listener.cause().getMessage());
                }
            });
        });
    }


}
