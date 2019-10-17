package com.mcoding.pangolin.protocol;

/**
 * @author wzt on 2019/7/1.
 * @version 1.0
 */
public interface MessageType {

    byte AUTH = 1;
    byte CONNECT = 2;
    byte TRANSFER = 3;
    byte DISCONNECT = 4;
    byte HEART_BEAT = 5;
    byte CHAIN_TRACE = 6;

}
