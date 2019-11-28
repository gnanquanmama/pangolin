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
import lombok.extern.slf4j.Slf4j;

/**
 * 基础通道服务容器
 *
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class PublicNetServerBootstrapContainer implements LifeCycle {

    public final static PublicNetServerBootstrapContainer INSTANCE = new PublicNetServerBootstrapContainer();

    private PublicNetServerBootstrapContainer() {
    }

    private EventLoopGroup pubNetBossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup pubNetWorkerGroup = new NioEventLoopGroup();
    private ServerBootstrap serverBootstrap = new ServerBootstrap();

    @Override
    public void start() {
        this.initServerBootstrap();
        this.startPublicNetworkChannelServer();
    }

    @Override
    public void close() {
        pubNetBossGroup.shutdownGracefully();
        pubNetWorkerGroup.shutdownGracefully();
    }

    private void initServerBootstrap() {
        serverBootstrap.group(pubNetBossGroup, pubNetWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 100)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ServerIdleStateHandler());
                        pipeline.addLast(PublicNetWorkChannelHandler.INSTANCE);
                    }
                });
    }

    /**
     * 开启用户公网服务
     */
    private void startPublicNetworkChannelServer() {
        PublicNetworkPortTable.getUserToPortMap().forEach((userId, proxyPort) -> {
            serverBootstrap.bind(proxyPort).addListener(listener -> {
                if (listener.isSuccess()) {
                    log.info("EVENT=OPEN PUBLIC NETWORK PORT [{}]", proxyPort);
                } else {
                    log.error("EVENT=OPEN PUBLIC NETWORK PORT [{}]|exception {}",
                            proxyPort, listener.cause().getMessage());
                }
            });
        });
    }

    /**
     * 临时绑定端口
     *
     * @param proxyPort
     * @param key
     */
    public synchronized void bindTempProxyPort(int proxyPort, String key) {
        serverBootstrap.bind(proxyPort).addListener(listener -> {
            if (listener.isSuccess()) {
                log.info("EVENT=OPEN PUBLIC NETWORK PORT [{}]", proxyPort);
                PublicNetworkPortTable.getUserToPortMap().put(key, proxyPort);
            } else {
                log.error("EVENT=OPEN PUBLIC NETWORK PORT [{}]|exception {}",
                        proxyPort, listener.cause().getMessage());
            }
        });
    }

}
