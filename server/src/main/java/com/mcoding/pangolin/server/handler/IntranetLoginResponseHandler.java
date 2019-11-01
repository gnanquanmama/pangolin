package com.mcoding.pangolin.server.handler;

import com.mcoding.pangolin.common.codec.LoginPacket;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.server.context.PangolinChannelContext;
import com.mcoding.pangolin.server.context.PublicNetworkPortTable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 登录处理器
 *
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
@Slf4j
public class IntranetLoginResponseHandler extends SimpleChannelInboundHandler<LoginPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginPacket packet) {
        String privateKey = packet.getPrivateKey();

        LoginPacket loginPacket = new LoginPacket();
        loginPacket.setType(MessageType.LOGIN);
        loginPacket.setPrivateKey(privateKey);
        loginPacket.setData(Constants.LOGIN_SUCCESS.getBytes());


        Integer publicPort = PublicNetworkPortTable.getUserToPortMap().get(privateKey);
        if (Objects.isNull(publicPort)) {
            loginPacket.setData("私钥不存在，请管理员在服务端配置后，再连接".getBytes());
            ctx.writeAndFlush(loginPacket);
            log.error("EVENT=登录失败，不存在PRIVATE_KEY={},请联系管理员", privateKey);
            return;
        }

        ctx.writeAndFlush(loginPacket);

        ctx.channel().attr(Constants.PRIVATE_KEY).set(privateKey);

        PangolinChannelContext.markAsLogin(ctx.channel());
        log.info("EVENT=登录处理|DESC=校验通过|PRIVATE_KEY={}", privateKey);

        ctx.channel().pipeline().remove(this);
    }

}
