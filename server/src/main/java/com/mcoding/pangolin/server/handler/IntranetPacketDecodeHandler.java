package com.mcoding.pangolin.server.handler;

import com.google.common.collect.Maps;
import com.mcoding.pangolin.common.codec.*;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import com.mcoding.pangolin.server.context.ChannelHolderContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 指令数据包解析处理器
 *
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class IntranetPacketDecodeHandler extends MessageToMessageDecoder<PMessageOuterClass.PMessage> {

    public static final IntranetPacketDecodeHandler INSTANCE = new IntranetPacketDecodeHandler();


    private static Map<Byte, Class<? extends Packet>> msgTypeToPacketClass = Maps.newHashMap();

    static {
        msgTypeToPacketClass.put(MessageType.LOGIN, LoginPacket.class);
        msgTypeToPacketClass.put(MessageType.CONNECT, ConnectPacket.class);
        msgTypeToPacketClass.put(MessageType.TRANSFER, TransferPacket.class);
        msgTypeToPacketClass.put(MessageType.DISCONNECT, DisconnectPacket.class);
        msgTypeToPacketClass.put(MessageType.HEART_BEAT, HeartBeatPacket.class);
        msgTypeToPacketClass.put(MessageType.CHAIN_TRACE, ChainTracePacket.class);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg, List<Object> out) throws Exception {
        int messageType = msg.getType();

        Packet packet = null;
        if (messageType == MessageType.TRANSFER) {
            packet = TransferPacket.INSTANCE.clone();
        } else {
            Class<? extends Packet> packetClass = msgTypeToPacketClass.get((byte) messageType);
            packet = packetClass.newInstance();
        }

        packet.setPrivateKey(msg.getPrivateKey());
        packet.setSessionId(msg.getSessionId());
        packet.setType((byte) msg.getType());
        packet.setData(msg.getData().toByteArray());

        out.add(packet);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("EVENT=ACTIVE INTRANET PROXY CHANNEL{}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();
        ChannelHolderContext.unBindIntranetProxyChannel(privateKey);
        log.warn("EVENT=CLOSE INTRANET PROXY CHANNEL{}", ctx.channel());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("EVENT=PROXY CHANNEL EXCEPTION|CHANNEL={}|ERROR_MSG={}", ctx.channel(), cause.getMessage());
    }
}
