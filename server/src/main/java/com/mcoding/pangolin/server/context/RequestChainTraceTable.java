package com.mcoding.pangolin.server.context;

import com.google.common.collect.Maps;
import com.mcoding.pangolin.common.entity.AddressInfo;

import java.util.List;
import java.util.Map;

/**
 * 请求链路追踪表
 *
 * @author wzt on 2019/10/17.
 * @version 1.0
 */
public class RequestChainTraceTable {

    private static Map<String, List<AddressInfo>> map = Maps.newConcurrentMap();

    public static void add(String sessionId, List<AddressInfo> addressInfoList) {
        map.put(sessionId, addressInfoList);
    }

    public static Map<String, List<AddressInfo>> getTable() {
        return map;
    }

    public static void remove(String sessionId) {
        map.remove(sessionId);
    }

}
