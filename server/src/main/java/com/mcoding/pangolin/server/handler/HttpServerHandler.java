package com.mcoding.pangolin.server.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mcoding.pangolin.server.util.PangolinChannelContext;
import com.mcoding.pangolin.server.util.PublicNetworkPortTable;
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
    private final static String INACTIVE_CLOSE_URI = "/channel/inactive/close";
    private final static String PUBLIC_PORT_CONF = "/public/port/conf";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
        String responseContent = "";
        if (ONLINE_CHANNEL_URI.equalsIgnoreCase(httpRequest.uri())) {
            responseContent = this.buildOnlineChannelInfo();
        } else if (INACTIVE_CLOSE_URI.equalsIgnoreCase(httpRequest.uri())) {
            this.closeInactiveChannel();
            responseContent = "close success";
        } else if (PUBLIC_PORT_CONF.equalsIgnoreCase(httpRequest.uri())) {
            responseContent = this.buildPublicNetworkPortConfig();
        } else {
            responseContent = httpRequest.uri() + "不存在对于的服务";
        }

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseContent.getBytes()));

        HttpHeaders heads = response.headers();
        heads.add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN + "; charset=UTF-8");
        heads.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        heads.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        ctx.writeAndFlush(response);
    }


    /**
     * 构建公网端口配置
     *
     * @return
     */
    private String buildPublicNetworkPortConfig() {
        HashBiMap<String, Integer> map = PublicNetworkPortTable.getUserToPortMap();
        return JSON.toJSONString(map);
    }

    /**
     * 关闭已不活动的通道
     */
    private void closeInactiveChannel() {
        PangolinChannelContext.getAllUserServerChannel().stream()
                .filter(channel -> !channel.isActive())
                .forEach(Channel::close);

        PangolinChannelContext.getAllProxyServerChannel().stream()
                .filter(channel -> !channel.isActive())
                .forEach(Channel::close);
    }

    /**
     * 构建在线通道信息
     *
     * @return
     */
    private String buildOnlineChannelInfo() {
        String allProxyServerChannel = PangolinChannelContext.getAllProxyServerChannel()
                .stream()
                .map(Channel::toString)
                .collect(Collectors.joining(","));

        String allUserServerChannel = PangolinChannelContext.getAllUserServerChannel()
                .stream()
                .map(Channel::toString)
                .collect(Collectors.joining(","));

        Map<String, String> resultMap = Maps.newHashMap();
        resultMap.put("proxyChannel", allProxyServerChannel);
        resultMap.put("userChannel", allUserServerChannel);
        return JSON.toJSONString(resultMap);
    }

}
