package com.mcoding.pangolin.server.handler;

import com.google.protobuf.ByteString;
import com.mcoding.pangolin.common.codec.Packet;
import com.mcoding.pangolin.protocol.PMessageOuterClass;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.set.UnmodifiableNavigableSet;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 指令数据包解析处理器
 *
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
@Slf4j
@ChannelHandler.Sharable
public class IntranetPacketEncodeHandler extends MessageToMessageEncoder<Packet> {

    public static final IntranetPacketEncodeHandler INSTANCE = new IntranetPacketEncodeHandler();


    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, List<Object> out) {

        PMessageOuterClass.PMessage.Builder builder = PMessageOuterClass.PMessage.newBuilder();
        builder.setType(packet.getType());
        if (StringUtils.isNotBlank(packet.getSessionId())) {
            builder.setSessionId(packet.getSessionId());
        }

        if (StringUtils.isNotBlank(packet.getPrivateKey())) {
            builder.setPrivateKey(packet.getPrivateKey());
        }

        if (packet.getData() != null) {
            builder.setData(ByteString.copyFrom(packet.getData()));
        }

        out.add(builder.build());
    }


}
