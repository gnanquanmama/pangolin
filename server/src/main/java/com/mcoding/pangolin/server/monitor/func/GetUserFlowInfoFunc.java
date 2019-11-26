package com.mcoding.pangolin.server.monitor.func;

import com.alibaba.fastjson.JSON;
import com.mcoding.pangolin.server.context.TrafficTable;

import java.util.function.Function;

/**
 * @author wzt on 2019/7/16.
 * @version 1.0
 */
public class GetUserFlowInfoFunc implements Function<Void, String> {

    @Override
    public String apply(Void aVoid) {
        return JSON.toJSONString(TrafficTable.get());
    }
}
