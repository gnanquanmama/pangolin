package com.mcoding.pangolin.server.context;

import com.google.common.eventbus.AsyncEventBus;

import java.util.concurrent.Executors;

/**
 * @author wzt
 */
public class TrafficEventBus {


    private final static AsyncEventBus INSTANCE = new AsyncEventBus(Executors.newFixedThreadPool(2));

    public static AsyncEventBus getInstance() {
        return INSTANCE;
    }


}
