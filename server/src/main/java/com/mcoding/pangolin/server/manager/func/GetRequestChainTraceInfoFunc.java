package com.mcoding.pangolin.server.manager.func;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.HashBiMap;
import com.mcoding.pangolin.server.util.PublicNetworkPortTable;
import com.mcoding.pangolin.server.util.RequestChainTraceTable;

import java.util.function.Function;

/**
 * @author wzt on 2019/7/16.
 * @version 1.0
 */
public class GetRequestChainTraceInfoFunc implements Function<Void, String> {

    @Override
    public String apply(Void aVoid) {
        return JSON.toJSONString(RequestChainTraceTable.getTable());
    }
}
