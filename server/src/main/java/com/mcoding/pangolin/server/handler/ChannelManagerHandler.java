package com.mcoding.pangolin.server.handler;

import com.google.common.collect.Maps;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.server.monitor.func.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author wzt on 2019/7/3.
 * @version 1.0
 */
@Slf4j
public class ChannelManagerHandler extends SimpleChannelInboundHandler<String> {

    private static final String EXIT = "exit";

    private static Map<String, Function<Void, String>> commandToFunc = Maps.newHashMap();

    static {
        commandToFunc.put("0", new MenuListFunc());
        commandToFunc.put("1", new GetOnlineChannelInfoFunc());
        commandToFunc.put("2", new GetPublicNetworkPortConfigFunc());
        commandToFunc.put("3", new CloseInactiveChannelFunc());
        commandToFunc.put("4", new GetRequestChainTraceInfoFunc());
        commandToFunc.put("5", new GetUserFlowInfoFunc());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        Channel channel = ctx.channel();

        if (StringUtils.isNotBlank(msg) && EXIT.equalsIgnoreCase(msg)) {
            channel.write(Constants.LINE_BREAK + Constants.LINE_BREAK);
            channel.write("BYE BYE BYE ^_^");
            channel.writeAndFlush(Constants.LINE_BREAK + Constants.LINE_BREAK);
            channel.close();
            return;
        }

        Function<Void, String> func = commandToFunc.get(msg);
        if (Objects.nonNull(func)) {
            ctx.channel().writeAndFlush(func.apply(null) + Constants.LINE_BREAK);
        } else {
            ctx.channel().writeAndFlush("请输入正确的数字..." + Constants.LINE_BREAK);
        }
    }


}
