package com.mcoding.pangolin.server.container;

import com.mcoding.pangolin.common.LifeCycle;
import com.mcoding.pangolin.common.util.PropertyUtils;
import com.mcoding.pangolin.server.handler.ChannelManagerHandler;
import com.mcoding.pangolin.server.monitor.handler.TelnetLoginHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 通道管理容器
 *
 * @author wzt on 2019/7/3.
 * @version 1.0
 */
@Slf4j
public class MonitorServerBootstrapContainer implements LifeCycle {

    public static final MonitorServerBootstrapContainer INSTANCE = new MonitorServerBootstrapContainer();

    private MonitorServerBootstrapContainer() {
    }

    private static int telnetPort = PropertyUtils.getInt("telnet_port");

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    @Override
    public void start() {

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 10)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Delimiters.lineDelimiter()));
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new TelnetLoginHandler());
                        pipeline.addLast(new ChannelManagerHandler());
                    }
                });

        bootstrap.bind(telnetPort)
                .addListener(listener -> log.info("EVENT=OPEN TELNET SERVICE|PORT={}", telnetPort));
    }


    @Override
    public void close() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
