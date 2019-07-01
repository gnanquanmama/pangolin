package com.mcoding.pangolin.common;

import io.netty.util.AttributeKey;

/**
 * @author wzt on 2019/6/25.
 * @version 1.0
 */
public interface Constants {

    AttributeKey<String> SESSION_ID = AttributeKey.newInstance("session_id");
    AttributeKey<String> PRIVATE_KEY = AttributeKey.newInstance("private_key");
    AttributeKey<Integer> PUBLIC_PORT = AttributeKey.newInstance("public_port");

}
