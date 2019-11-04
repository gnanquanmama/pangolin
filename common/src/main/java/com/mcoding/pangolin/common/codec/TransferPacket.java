package com.mcoding.pangolin.common.codec;

/**
 * @author wzt on 2019/10/31.
 * @version 1.0
 */
public class TransferPacket extends Packet {

    public static final TransferPacket INSTANCE = new TransferPacket();

    @Override
    public TransferPacket clone() {
        try {
            return (TransferPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return new TransferPacket();
        }
    }

}
