package com.mcoding.pangolin.server.dashboard.handler;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.math.IntMath;
import com.mcoding.pangolin.common.util.BeanUtils;
import com.mcoding.pangolin.server.container.PublicNetServerBootstrapContainer;
import com.mcoding.pangolin.server.context.PublicNetworkPortTable;
import com.mcoding.pangolin.server.dashboard.model.BindTempProxyPortModel;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author wzt on 2019/11/27.
 * @version 1.0
 */
public class DashboardServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        String bindTempProxyPortUri = "/dashboard/bindTempProxyPort";

        Map<String, Object> paramMap = this.parseRequestParams(msg);
        if (bindTempProxyPortUri.equalsIgnoreCase(msg.uri())) {
            BindTempProxyPortModel bindTempProxyPortModel = new BindTempProxyPortModel();
            BeanUtils.mapToBean(paramMap, bindTempProxyPortModel);

            String validResult = this.valid(bindTempProxyPortModel);
            if (StringUtils.isNotBlank(validResult)) {
                this.print(ctx, validResult);
                return;
            }

            String privateKey = bindTempProxyPortModel.getPrivateKey();
            String proxyPort = bindTempProxyPortModel.getProxyPort();

            HashBiMap<String, Integer> userToPortMap = PublicNetworkPortTable.getUserToPortMap();
            boolean containsKey = userToPortMap.containsKey(privateKey);
            boolean containsPort = userToPortMap.containsValue(Integer.valueOf(proxyPort));
            if (containsKey) {
                this.print(ctx, String.format("私钥[%s]重复了，请修改", privateKey));
                return;
            }

            if (containsPort) {
                this.print(ctx, String.format("代理端口[%s]重复了，请修改", proxyPort));
                return;
            }

            PublicNetServerBootstrapContainer.INSTANCE.bindTempProxyPort(Integer.valueOf(proxyPort), privateKey);

            PublicNetworkPortTable.getUserToPortMap().put(privateKey, Integer.valueOf(proxyPort));
            this.print(ctx, "增加临时代理端口成功, 温馨提示：server重启，配置会失效，请悉知");
            return;
        }

        this.print(ctx, "404");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().writeAndFlush("bloom bloom bloom 内部异常");
        ctx.channel().close();
        super.exceptionCaught(ctx, cause);
    }

    private String valid(BindTempProxyPortModel bindTempProxyPortModel) {

        String privateKey = bindTempProxyPortModel.getPrivateKey();
        String proxyPort = bindTempProxyPortModel.getProxyPort();
        String authCode = bindTempProxyPortModel.getAuthCode();

        if (StringUtils.isBlank(privateKey)) {
            return "用户私钥不能为空";
        }

        if (StringUtils.isBlank(proxyPort)) {
            return "代理端口不能为空";
        }

        if (StringUtils.isBlank(authCode)) {
            return "认证码不能为空";
        }

        String currentAuthCode = this.getCurrentAuthCode();
        if (!currentAuthCode.equalsIgnoreCase(bindTempProxyPortModel.getAuthCode())) {
            return "认证码错误";
        }
        return null;
    }


    private void print(ChannelHandlerContext ctx, String responseContent) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseContent.getBytes()));

        HttpHeaders heads = response.headers();
        heads.add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN + "; charset=UTF-8");
        heads.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        ctx.channel().writeAndFlush(response);
        ctx.channel().close();
    }


    private Map<String, Object> parseRequestParams(FullHttpRequest fullReq) {
        HttpMethod method = fullReq.method();
        Map<String, Object> paramMap = Maps.newHashMap();

        if (HttpMethod.GET == method) {
            QueryStringDecoder decoder = new QueryStringDecoder(fullReq.uri());
            decoder.parameters().forEach((key, value) -> {
                paramMap.put(key, value.get(0));
            });
        } else if (HttpMethod.POST == method) {
            HttpPostRequestDecoder postDecoder = new HttpPostRequestDecoder(fullReq);
            postDecoder.offer(fullReq);
            List<InterfaceHttpData> paramList = postDecoder.getBodyHttpDatas();
            if (paramList != null) {
                for (InterfaceHttpData param : paramList) {
                    Attribute data = (Attribute) param;
                    try {
                        paramMap.put(data.getName(), data.getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return paramMap;
    }

    private String getCurrentAuthCode() {
        LocalDate localDate = LocalDate.now();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();
        return String.valueOf(year + IntMath.pow(month, 2) + IntMath.pow(day, 3));
    }
}