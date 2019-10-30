package com.mcoding.pangolin.server.context;

import java.util.concurrent.atomic.AtomicLong;

/**
 * SessionId 生成器
 *
 * @author wzt on 2019/7/2.
 * @version 1.0
 */
public class SessionIdProducer {

    private AtomicLong count = new AtomicLong();

    public String generate() {
        return String.format("pangolin_%07d", count.incrementAndGet());
    }
}
