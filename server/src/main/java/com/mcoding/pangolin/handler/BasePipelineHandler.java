package com.mcoding.pangolin.handler;

import com.google.common.eventbus.EventBus;
import com.mcoding.pangolin.Message;
import com.mcoding.pangolin.contants.EndPointUnDirectedPath;
import com.mcoding.pangolin.context.TunnelContext;
import com.mcoding.pangolin.listener.NewProxyPortListener;
import com.mcoding.pangolin.listener.event.CreateNewProxyPortEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.Objects;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class BasePipelineHandler extends ChannelInboundHandlerAdapter {

    private EventBus eventBus = new EventBus("create_new_proxy_port_event_bus");

    public BasePipelineHandler() {
        NewProxyPortListener createNewProxyPortListener = new NewProxyPortListener();
        eventBus.register(createNewProxyPortListener);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        Message message = (Message) msg;
        if (Message.CONNECTING.equals(message.getType())) {
            Integer proxyPort = message.getProxyPort();
            Channel baseChannel = TunnelContext.get(EndPointUnDirectedPath.BASE_SERVER_TO_BASE_CLIENT, proxyPort);
            if (Objects.nonNull(baseChannel)) {
                Message respMsg = new Message();
                respMsg.setData(String.format("端口%d已被占用,请更换端口", proxyPort).getBytes());
                ctx.writeAndFlush(respMsg);
                ctx.channel().close();
                return;
            }

            // 新生成一个代理管道任务
            eventBus.post(new CreateNewProxyPortEvent(message.getProxyPort(), false));

            TunnelContext.put(EndPointUnDirectedPath.BASE_SERVER_TO_BASE_CLIENT, proxyPort, ctx.channel());

        } else {
            Integer proxyPort = message.getProxyPort();

            Channel proxyChannel = TunnelContext.get(EndPointUnDirectedPath.APP_TO_PROXY_SERVER, proxyPort);
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