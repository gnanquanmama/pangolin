package com.mcoding.pangolin.server.flow;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wzt
 */
public class FlowTable {

    private static ConcurrentHashMap<String, Flow> flowMap = new ConcurrentHashMap<>();

    /**
     * 记录
     * @param userPrivateKey
     * @param inFlow
     * @param outFlow
     */
    static void record(String userPrivateKey, long inFlow, long outFlow) {

        Flow flow = flowMap.get(userPrivateKey);
        if (flow == null) {
            flowMap.put(userPrivateKey, new Flow(inFlow, outFlow));
            return;
        }

        long totalInFlow = flow.inFlow + inFlow;
        long totalOutFlow = flow.outFlow + outFlow;
        flow.setInFlow(totalInFlow);
        flow.setOutFlow(totalOutFlow);
    }

    public static Map<String, Flow> get() {
        return flowMap;
    }

    @Data
    @AllArgsConstructor
    static class Flow {
        long inFlow = 0;
        long outFlow = 0;
    }
}


