package com.mcoding.pangolin.server.monitor.func;

import com.alibaba.fastjson.JSON;
import com.mcoding.pangolin.server.context.NetworkChainTraceTable;

import java.util.function.Function;

/**
 * @author wzt on 2019/7/16.
 * @version 1.0
 */
public class GetRequestChainTraceInfoFunc implements Function<Void, String> {

    @Override
    public String apply(Void aVoid) {
        return JSON.toJSONString(NetworkChainTraceTable.getTable());
    }
}
