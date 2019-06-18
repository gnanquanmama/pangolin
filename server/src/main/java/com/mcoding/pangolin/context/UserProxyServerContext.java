package com.mcoding.pangolin.context;

import com.mcoding.pangolin.task.UserProxyServerTask;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wzt on 2019/6/18.
 * @version 1.0
 */
public class UserProxyServerContext {

    private static Map<Integer, UserProxyServerTask> channelMap = new HashMap<>();


    public static void put(Integer key, UserProxyServerTask task) {
        channelMap.put(key, task);
    }

    public static UserProxyServerTask get(Integer key) {
        return channelMap.get(key);
    }

}
