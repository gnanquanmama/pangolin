package com.mcoding.pangolin.server.manager.config;

import com.google.common.collect.Maps;
import com.mcoding.pangolin.server.manager.func.*;

import java.util.Map;
import java.util.function.Function;

/**
 * @author wzt on 2019/7/16.
 * @version 1.0
 */
public class UrlToFuncConfig {

    private static Map<String, Function<Void, String>> urlToFunc = Maps.newHashMap();

    static {
        urlToFunc.put("/channel/online/info", new GetOnlineChannelInfoFunc());
        urlToFunc.put("/channel/inactive/close", new CloseInactiveChannelFunc());
        urlToFunc.put("/public/port/conf", new GetPublicNetworkPortConfigFunc());
        urlToFunc.put("/public/trace/info", new GetRequestChainTraceInfoFunc());
        urlToFunc.put("/public/flow/info", new GetUserFlowInfoFunc());
    }

    public static Function<Void, String> getFunction(String relativeUrl) {
        return urlToFunc.get(relativeUrl);
    }

}
