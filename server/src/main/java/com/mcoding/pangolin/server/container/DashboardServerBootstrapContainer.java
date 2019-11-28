package com.mcoding.pangolin.server.container;

import com.mcoding.pangolin.common.LifeCycle;
import com.mcoding.pangolin.common.util.PropertyUtils;
import com.mcoding.pangolin.server.dashboard.handler.DashboardServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 控制面板服务容器
 *
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class DashboardServerBootstrapContainer implements LifeCycle {

    private static int dashboardHttpServerPort = PropertyUtils.getInt("dashboard_http_port");

    public static final DashboardServerBootstrapContainer INSTANCE = new DashboardServerBootstrapContainer();

    private EventLoopGroup httpBossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup httpWorkerGroup = new NioEventLoopGroup();

    private DashboardServerBootstrapContainer() {
    }

    @Override
    public void start() {
        this.startMetricsServer();
    }

    @Override
    public void close() {
        httpBossGroup.shutdownGracefully();
        httpWorkerGroup.shutdownGracefully();
    }


    private void startMetricsServer() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(httpBossGroup, httpWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpRequestDecoder());
                        pipeline.addLast(new HttpResponseEncoder());
                        pipeline.addLast(new HttpObjectAggregator(512 * 1024));
                        pipeline.addLast(new DashboardServerHandler());
                    }
                });

        serverBootstrap.bind(dashboardHttpServerPort).addListener(listener -> {
            if (listener.isSuccess()) {
                log.info("EVENT=OPEN DASHBOARD SERVER PORT [{}]", dashboardHttpServerPort);
            } else {
                log.error("EVENT=OPEN DASHBOARD PROXY SERVER PORT [{}]|EXCEPTION {}",
                        dashboardHttpServerPort, listener.cause().getMessage());
            }
        });
    }


}
