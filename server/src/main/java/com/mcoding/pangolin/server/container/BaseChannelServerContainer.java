package com.mcoding.pangolin.server.container;

import com.mcoding.pangolin.common.LifeCycle;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import com.mcoding.pangolin.server.context.PublicNetworkPortTable;
import com.mcoding.pangolin.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
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
 * 基础通道服务容器
 *
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class BaseChannelServerContainer implements LifeCycle {

    private EventLoopGroup bossGroup = new NioEventLoopGroup(2);
    private EventLoopGroup workerGroup = new NioEventLoopGroup(8);
    private int serverPort;

    public BaseChannelServerContainer(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void start() {
        this.startIntranetProxyServer();
        this.startPublicNetworkChannelServer();
    }

    @Override
    public void close() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    /**
     * 开启用户公网通道服务
     */
    private void startPublicNetworkChannelServer() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
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
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ServerIdleStateHandler());
                        pipeline.addLast(PublicNetWorkChannelHandler.INSTANCE);
                    }
                });

        PublicNetworkPortTable.getUserToPortMap().forEach((userId, proxyPort) -> {
            serverBootstrap.bind(proxyPort)
                    .addListener(__ -> log.info("EVENT=开启公网访问端口[{}]", proxyPort));
        });

    }

    /**
     * 启动代理服务器
     */
    private void startIntranetProxyServer() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 100)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new ServerIdleStateHandler());
                        pipeline.addLast(new ProtobufVarint32FrameDecoder());
                        pipeline.addLast(new ProtobufDecoder(PMessageOuterClass.PMessage.getDefaultInstance()));
                        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                        pipeline.addLast(new ProtobufEncoder());
                        pipeline.addLast(IntranetPacketEncodeHandler.INSTANCE);
                        pipeline.addLast(IntranetPacketDecodeHandler.INSTANCE);
                        pipeline.addLast(IntranetLoginResponseHandler.INSTANCE);
                        pipeline.addLast(IntranetTargetServerConnectedHandler.INSTANCE);
                        pipeline.addLast(IntranetTransferResponseHandler.INSTANCE);
                        pipeline.addLast(IntranetDisConnectResponseHandler.INSTANCE);
                        pipeline.addLast(IntranetHeartBeatResponseHandler.INSTANCE);
                        pipeline.addLast(ChainTracingResponseHandler.INSTANCE);
                    }
                });

        serverBootstrap.bind(serverPort)
                .addListener(__ -> log.info("EVENT=开启内网代理管道服务端口[{}]", serverPort));
    }


}
