package com.mcoding.pangolin.server.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wzt on 2019/7/2.
 * @version 1.0
 */
public class SessionIdProducer {

    private static AtomicLong count = new AtomicLong();

    public String generate() {
        return String.format("pangolin_%07d", count.incrementAndGet());
    }
}