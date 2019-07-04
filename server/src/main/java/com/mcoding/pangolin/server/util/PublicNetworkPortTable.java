package com.mcoding.pangolin.server.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.HashBiMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.List;

/**
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
@Slf4j
public class PublicNetworkPortTable {

    private static HashBiMap<String, Integer> userToPortMap = HashBiMap.create();

    static {
        String userJson = "";
        try (InputStream inputStream = PublicNetworkPortTable.class.getResourceAsStream("/user.json")){
            userJson = IOUtils.toString(inputStream, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<PublicPortConfig> publicPortConfigList = JSON.parseObject(userJson,
                new TypeReference<List<PublicPortConfig>>() {
                });

        log.info("EVENT=读取公网端口配置|CONTENT={}", JSON.toJSONString(publicPortConfigList));

        for (PublicPortConfig publicPortConfig : publicPortConfigList) {
            userToPortMap.put(publicPortConfig.getPrivateKey(), publicPortConfig.getPublicPort());
        }

    }

    public static HashBiMap<String, Integer> getUserToPortMap() {
        return userToPortMap;
    }

    @Data
    private static class PublicPortConfig {
        private String privateKey;
        private Integer publicPort;
    }
}

