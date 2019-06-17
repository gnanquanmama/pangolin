package com.mcoding.pangolin.handler;

import com.alibaba.fastjson.JSON;
import com.mcoding.pangolin.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
@AllArgsConstructor
public class InitConnectionHandler extends ChannelInboundHandlerAdapter {

    private String key;
    private Integer proxyPort;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Message message = new Message();
        message.setKey(key);
        message.setProxyPort(proxyPort);
        message.setType(Message.CONNECTING);
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Message message = (Message) msg;
        System.out.println("server said : " + new String(message.getData()));

        StringBuilder sendString = new StringBuilder();
        sendString.append("HTTP/1.1 200 OK\r\n");
        sendString.append("Content-Type:text/html;charset=UTF-8\r\n");
        sendString.append("\r\n");

        sendString.append("<html><head><title>显示报文</title></head><body>");
        sendString.append("接收到请求报文是：<br/>");
        sendString.append("</body></html>");


        Message respMsg = new Message();
        respMsg.setType(Message.TRANSFER);
        respMsg.setProxyPort(proxyPort);
        respMsg.setData(sendString.toString().getBytes());
        ctx.writeAndFlush(respMsg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}