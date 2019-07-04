package com.mcoding.pangolin.server.container;

import com.mcoding.pangolin.common.LifeCycle;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import com.mcoding.pangolin.server.handler.ProxyChannelHandler;
import com.mcoding.pangolin.server.handler.UserChannelHandler;
import com.mcoding.pangolin.server.util.PublicNetworkPortTable;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
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
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1, 1024 * 1024))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ProtobufVarint32FrameDecoder());
                        pipeline.addLast(new ProtobufDecoder(PMessageOuterClass.PMessage.getDefaultInstance()));
                        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                        pipeline.addLast(new ProtobufEncoder());
                        pipeline.addLast(new ProxyChannelHandler());
                    }
                });

        try {
            ChannelFuture f = serverBootstrap.bind(serverPort).sync();
            f.addListener(channelFutureListener -> log.info("EVENT=开启基础管道服务端口[{}]", serverPort));
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
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new UserChannelHandler());
                    }
                });

        PublicNetworkPortTable.getUserToPortMap().forEach((userId, proxyPort) -> {
            serverBootstrap.bind(proxyPort)
                    .addListener(future -> log.info("EVENT=开启公网访问端口[{}]", proxyPort));
        });

    }

}
