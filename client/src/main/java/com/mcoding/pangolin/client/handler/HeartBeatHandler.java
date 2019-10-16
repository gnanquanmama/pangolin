package com.mcoding.pangolin.client.handler;

import com.google.protobuf.ByteString;
import com.mcoding.pangolin.protocol.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
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
@Slf4j
public class HeartBeatHandler extends IdleStateHandler {

    public HeartBeatHandler(int readerIdleTime, int writerIdleTime, int allIdleTime, TimeUnit timeUnit) {
        super(readerIdleTime, writerIdleTime, allIdleTime, timeUnit);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        if (IdleStateEvent.WRITER_IDLE_STATE_EVENT == evt) {
            String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();

            PMessageOuterClass.PMessage heartBeatMsg = PMessageOuterClass.PMessage.newBuilder()
                    .setPrivateKey(privateKey)
                    .setType(MessageType.HEART_BEAT)
                    .setData(ByteString.copyFrom("I still living ^_^".getBytes()))
                    .build();

            ctx.channel().writeAndFlush(heartBeatMsg);
            log.info("EVENT=发送心跳包|CHANNEL={}|MSG={}",ctx.channel(), heartBeatMsg.getData().toStringUtf8());
        } else if (IdleStateEvent.READER_IDLE_STATE_EVENT == evt) {
            log.warn("event=读超时，关闭管道{}", ctx.channel());
            ctx.channel().close();
        }
    }

}
