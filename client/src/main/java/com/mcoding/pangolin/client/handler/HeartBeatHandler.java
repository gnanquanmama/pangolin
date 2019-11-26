package com.mcoding.pangolin.client.handler;

import com.google.protobuf.ByteString;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 心跳发送处理器
 *
 * @author wzt on 2019/10/16.
 * @version 1.0
 */
@ChannelHandler.Sharable
@Slf4j
public class HeartBeatHandler extends IdleStateHandler {

    public static HeartBeatHandler INSTANCE = new HeartBeatHandler();

    public HeartBeatHandler() {
        super(0, 15, 0, TimeUnit.MINUTES);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        if (IdleStateEvent.WRITER_IDLE_STATE_EVENT == evt) {
            String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();

            PMessageOuterClass.PMessage heartBeatMsg = PMessageOuterClass.PMessage.newBuilder()
                    .setPrivateKey(privateKey)
                    .setType(MessageType.HEART_BEAT)
                    .setData(ByteString.copyFrom("I still living ^_^".getBytes()))
                    .build();

            ctx.channel().writeAndFlush(heartBeatMsg);
            log.info("EVENT=SEND HEARTBEAT PACKET|CHANNEL={}|MSG={}", ctx.channel(), heartBeatMsg.getData().toStringUtf8());
        } else if (IdleStateEvent.READER_IDLE_STATE_EVENT == evt) {
            log.warn("EVENT=CHANNEL READ TIMEOUT CLOSE CHANNEL {}", ctx.channel());
            ctx.channel().close();
        }
    }

}
