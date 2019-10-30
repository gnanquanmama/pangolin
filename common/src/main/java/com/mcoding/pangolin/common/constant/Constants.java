package com.mcoding.pangolin.common.constant;

import io.netty.util.AttributeKey;

/**
 * @author wzt on 2019/6/25.
 * @version 1.0
 */
public interface Constants {

    AttributeKey<String> SESSION_ID = AttributeKey.newInstance("session_id");
    AttributeKey<String> PRIVATE_KEY = AttributeKey.newInstance("private_key");

    String AUTH_SUCCESS = "ok";
    String AUTH_FAIL = "fail";

    String LINE_BREAK = "\r\n";

}
