package com.mcoding.pangolin.server.manager;

import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.common.util.PropertyUtils;
import com.mcoding.pangolin.server.context.SessionIdProducer;
import com.mcoding.pangolin.server.manager.context.ManagerChannelContext;
import com.mcoding.pangolin.server.manager.func.MenuListFunc;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wzt on 2019/10/30.
 * @version 1.0
 */
public class LoginRequestHandler extends SimpleChannelInboundHandler<String> {

    private static SessionIdProducer sessionIdProducer = new SessionIdProducer();

    private static String adminPassword = PropertyUtils.get("telnet_login_password");

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();

        String sessionId = channel.attr(Constants.SESSION_ID).get();
        if (StringUtils.isBlank(sessionId)) {
            if (adminPassword.equals(msg)) {
                channel.write(Constants.LINE_BREAK);
                channel.write("WELCOME TO PANGOLIN CONSOLE... ");
                channel.write(Constants.LINE_BREAK + Constants.LINE_BREAK);
                channel.writeAndFlush(new MenuListFunc().apply(null));

                String newGenerateSessionId = sessionIdProducer.generate();
                channel.attr(Constants.SESSION_ID).set(newGenerateSessionId);

                ManagerChannelContext.markAsLogin(channel);

                // 登录成功，则移除登录校验handler
                channel.pipeline().remove(this);

            } else {
                channel.write("PASSWORD ERROR. BYE BYE BYE...");
                channel.writeAndFlush(Constants.LINE_BREAK + Constants.LINE_BREAK);
                channel.close();
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        channel.write(Constants.LINE_BREAK);
        channel.writeAndFlush("password: ");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String sessionId = channel.attr(Constants.SESSION_ID).get();
        if (StringUtils.isNotBlank(sessionId)) {
            ManagerChannelContext.unLogin(sessionId);
        }
    }
}
