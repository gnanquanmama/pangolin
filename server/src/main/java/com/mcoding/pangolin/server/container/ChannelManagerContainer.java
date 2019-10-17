package com.mcoding.pangolin.server.container;

import com.mcoding.pangolin.common.LifeCycle;
import com.mcoding.pangolin.common.util.PropertyUtils;
import com.mcoding.pangolin.server.handler.ChannelManagerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 通道管理容器
 *
 * @author wzt on 2019/7/3.
 * @version 1.0
 */
@Slf4j
public class ChannelManagerContainer implements LifeCycle {

    private static int webPort = PropertyUtils.getInt("web_port");

    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Override
    public void start() {

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new HttpRequestDecoder(),
                                new HttpResponseEncoder(),
                                new HttpObjectAggregator(1024 * 512),
                                new ChannelManagerHandler());
                    }
                });

        bootstrap.bind(webPort)
                .addListener(listener -> log.info("EVENT=开启HTTP服务|端口={}", webPort));
    }


    @Override
    public void close() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
