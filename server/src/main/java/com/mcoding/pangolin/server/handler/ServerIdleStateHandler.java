package com.mcoding.pangolin.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 服务空闲检测器
 *
 * @author wzt on 2019/10/16.
 * @version 1.0
 */
@Slf4j
public class ServerIdleStateHandler extends IdleStateHandler {

    private static final int READ_IDLE_TIME = 15;

    public ServerIdleStateHandler() {
        super(READ_IDLE_TIME, 0, 0, TimeUnit.MINUTES);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {

        log.error("EVENT=连接空闲检测|DESC=连接超过一个小时没有读取到数据, 关闭连接");
        ctx.channel().close();
    }

}
