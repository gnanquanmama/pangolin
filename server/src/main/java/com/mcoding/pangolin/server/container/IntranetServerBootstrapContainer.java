package com.mcoding.pangolin.server.container;

import com.mcoding.pangolin.common.LifeCycle;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
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
import lombok.extern.slf4j.Slf4j;

/**
 * 基础通道服务容器
 *
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class IntranetServerBootstrapContainer implements LifeCycle {

    private EventLoopGroup intNetBossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup intNetWorkerGroup = new NioEventLoopGroup();

    private int serverPort;

    public IntranetServerBootstrapContainer(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void start() {
        this.startIntranetProxyServer();
    }

    @Override
    public void close() {
        intNetBossGroup.shutdownGracefully();
        intNetWorkerGroup.shutdownGracefully();
    }

    /**
     * 启动代理服务器
     */
    private void startIntranetProxyServer() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(intNetBossGroup, intNetWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 100)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
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
                        pipeline.addLast(IntranetTransferResponseHandler.INSTANCE);
                        pipeline.addLast(IntranetTargetServerConnectedHandler.INSTANCE);
                        pipeline.addLast(IntranetDisConnectResponseHandler.INSTANCE);
                        pipeline.addLast(IntranetHeartBeatResponseHandler.INSTANCE);
                        pipeline.addLast(ChainTracingResponseHandler.INSTANCE);
                    }
                });

        serverBootstrap.bind(serverPort).addListener(listener -> {
            if (listener.isSuccess()) {
                log.info("EVENT=OPEN INTRANET PROXY SERVER PORT [{}]", serverPort);
            } else {
                log.error("EVENT=OPEN INTRANET PROXY SERVER PORT [{}]|exception {}",
                        serverBootstrap, listener.cause().getMessage());
            }
        });
    }


}
