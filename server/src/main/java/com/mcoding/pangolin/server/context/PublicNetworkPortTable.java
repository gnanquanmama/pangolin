package com.mcoding.pangolin.server.context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
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
        try (InputStream inputStream = PublicNetworkPortTable.class.getResourceAsStream("/pub_net_conf.json")){
            userJson = IOUtils.toString(inputStream, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<UserPubNetPortConfig> userPubNetPortConfigList = JSON.parseObject(userJson,
                new TypeReference<List<UserPubNetPortConfig>>() {
                });

        log.info("EVENT=READ公网端口配置|CONTENT={}", JSON.toJSONString(userPubNetPortConfigList));

        for (UserPubNetPortConfig userPubNetPortConfig : userPubNetPortConfigList) {
            userToPortMap.put(userPubNetPortConfig.getUserPrivateKey(), userPubNetPortConfig.getPubNetPort());
        }

    }

    public static HashBiMap<String, Integer> getUserToPortMap() {
        return userToPortMap;
    }

    @Data
    private static class UserPubNetPortConfig {

        @JSONField(name = "user_private_key")
        private String userPrivateKey;

        @JSONField(name = "pub_net_port")
        private Integer pubNetPort;
    }
}

