package com.mcoding.pangolin.server.container;

import com.mcoding.pangolin.common.LifeCycle;
import com.mcoding.pangolin.server.handler.ProxyChannelHandler;
import com.mcoding.pangolin.server.handler.UserChannelHandler;
import com.mcoding.pangolin.server.user.UserTable;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class ProxyServerContainer implements LifeCycle {

    private EventLoopGroup bossGroup = new NioEventLoopGroup(2);
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private int serverPort;

    public ProxyServerContainer(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void start() {
        this.startProxyServer();
        this.startUserServer();
    }

    @Override
    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    /**
     * 启动代理服务器
     */
    private void startProxyServer() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 100)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024, Integer.MAX_VALUE))
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ObjectEncoder());
                        pipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                        pipeline.addLast(new ProxyChannelHandler());
                    }
                });

        try {
            ChannelFuture f = serverBootstrap.bind(serverPort).sync();
            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    log.info("EVENT=开启基础管道服务端口[{}]", serverPort);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 开启用户外网访问端口服务
     */
    private void startUserServer() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 100)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024, Integer.MAX_VALUE))
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new UserChannelHandler());
                    }
                });

        UserTable.getUserToPortMap().forEach((userId, proxyPort) -> {
            try {
                ChannelFuture f = serverBootstrap.bind(proxyPort).sync();
                f.addListener(future -> log.info("EVENT=开启公网访问端口[{}]", proxyPort));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

}
