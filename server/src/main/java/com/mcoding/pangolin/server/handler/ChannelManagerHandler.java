package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.server.manager.config.UrlToFuncConfig;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Function;

/**
 * @author wzt on 2019/7/3.
 * @version 1.0
 */
@Slf4j
public class ChannelManagerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
        String responseContent = "";

        String relativeUrl = httpRequest.uri();
        Function<Void, String> handlerFunc = UrlToFuncConfig.getFunction(relativeUrl);
        if (Objects.isNull(handlerFunc)) {
            responseContent = relativeUrl + "不存在对应的服务";
        } else {
            responseContent = handlerFunc.apply(null);
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

}
