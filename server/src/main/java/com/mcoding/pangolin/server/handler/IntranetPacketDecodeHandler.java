package com.mcoding.pangolin.server.handler;

import com.google.common.collect.Maps;
import com.mcoding.pangolin.common.codec.*;
import com.mcoding.pangolin.common.constant.Constants;
import com.mcoding.pangolin.protocol.MessageType;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import com.mcoding.pangolin.server.context.PangolinChannelContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 指令数据包解析处理器
 *
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
@Slf4j
public class IntranetPacketDecodeHandler extends MessageToMessageDecoder<PMessageOuterClass.PMessage> {

    private static Map<Byte, Class<? extends Packet>> msgTypeToPacketClass = Maps.newHashMap();

    static {
        msgTypeToPacketClass.put(MessageType.LOGIN, LoginPacket.class);
        msgTypeToPacketClass.put(MessageType.CONNECT, ConnectPacket.class);
        msgTypeToPacketClass.put(MessageType.TRANSFER, TransferPacket.class);
        msgTypeToPacketClass.put(MessageType.DISCONNECT, DisconnectPacket.class);
        msgTypeToPacketClass.put(MessageType.HEART_BEAT, HeartBeatPacket.class);
        msgTypeToPacketClass.put(MessageType.CHAIN_TRACE, ChainTracePacket.class);
    }

    private int queueSize = 1000;
    private CircularFifoQueue<TransferPacket> transferPacketQueue = new CircularFifoQueue<>(queueSize);

    @Override
    protected void decode(ChannelHandlerContext ctx, PMessageOuterClass.PMessage msg, List<Object> out) throws Exception {
        int messageType = msg.getType();

        Packet packet = null;
        if (messageType == MessageType.TRANSFER) {
            packet = Optional.ofNullable(transferPacketQueue.poll()).orElse(new TransferPacket());
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
        log.info("EVENT=激活内网代理端通道{}", ctx.channel());

        // 缓存 queueSize 个 数据传输对象
        for (int i = 0; i < queueSize; i++) {
            transferPacketQueue.offer(new TransferPacket());
        }

        ctx.channel().eventLoop().schedule(() -> {
            if (!transferPacketQueue.isFull()) {
                int currentSize = transferPacketQueue.size();
                int maxSize = transferPacketQueue.maxSize();
                for (int i = 0; i < maxSize - currentSize; i++) {
                    transferPacketQueue.offer(new TransferPacket());
                }
            }

        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        CircularFifoQueue f;

        String privateKey = ctx.channel().attr(Constants.PRIVATE_KEY).get();
        PangolinChannelContext.unBindIntranetProxyChannel(privateKey);
        log.warn("EVENT=关闭内网代理端通道{}", ctx.channel());
        ctx.close();
    }


    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        log.info("event=代理管道可写状态变化" + ctx.channel().isWritable());

        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("EVENT=代理通道异常|CHANNEL={}|ERROR_MSG={}", ctx.channel(), cause.getMessage());
    }
}
