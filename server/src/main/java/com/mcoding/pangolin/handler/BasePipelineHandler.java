package com.mcoding.pangolin.handler;

import com.alibaba.fastjson.JSON;
import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.context.BaseChannelContext;
import com.mcoding.pangolin.context.ProxyChannelContext;
import com.mcoding.pangolin.task.UserProxyServerTask;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class BasePipelineHandler extends ChannelInboundHandlerAdapter {

    private static  ExecutorService execService = Executors.newCachedThreadPool();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        Message message = (Message) msg;
        if (Message.CONNECTING.equals(message.getType())) {
            // 新生成一个代理管道任务
            UserProxyServerTask proxyServerTask = new UserProxyServerTask(message.getProxyPort(), false);
            BaseChannelContext.put(message.getProxyPort(), ctx.channel());
            execService.submit(proxyServerTask);
        } else {
            Integer proxyPort = message.getProxyPort();
            Channel proxyChannel = ProxyChannelContext.get(proxyPort);

            ByteBuf respBuffer = Unpooled.copiedBuffer(new String(message.getData()), CharsetUtil.UTF_8);
            proxyChannel.writeAndFlush(respBuffer);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}