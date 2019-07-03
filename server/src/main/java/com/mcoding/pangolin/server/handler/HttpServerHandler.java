package com.mcoding.pangolin.server.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.mcoding.pangolin.server.util.ChannelContextHolder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wzt on 2019/7/3.
 * @version 1.0
 */
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final static String ONLINE_CHANNEL_URI = "/channel/online/info";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
        String responseJson = "404";
        if (ONLINE_CHANNEL_URI.equalsIgnoreCase(httpRequest.uri())) {
            responseJson = this.buildOnlineChannelInfo();
        } else {
            responseJson = httpRequest.uri() + "不存在对于的服务";
        }

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseJson.getBytes()));

        HttpHeaders heads = response.headers();
        heads.add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN + "; charset=UTF-8");
        heads.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        heads.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        ctx.writeAndFlush(response);
    }

    /**
     * 构建在线通道信息
     *
     * @return
     */
    private String buildOnlineChannelInfo() {
        String allProxyServerChannel = ChannelContextHolder.getAllProxyServerChannel()
                .stream()
                .map(Channel::toString)
                .collect(Collectors.joining(","));

        String allUserServerChannel = ChannelContextHolder.getAllUserServerChannel()
                .stream()
                .map(Channel::toString)
                .collect(Collectors.joining(","));

        Map<String, String> resultMap = Maps.newHashMap();
        resultMap.put("proxyChannel", allProxyServerChannel);
        resultMap.put("userChannel", allUserServerChannel);
        return JSON.toJSONString(resultMap);
    }

}
