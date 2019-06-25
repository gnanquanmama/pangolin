package com.mcoding.pangolin.server.user;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author wzt on 2019/6/20.
 * @version 1.0
 */
public class UserTable {

    private static HashBiMap<String, Integer> userToPortMap = HashBiMap.create();

    static {
        userToPortMap.put("1", 9797);
    }

    public static HashBiMap<String, Integer> getUserToPortMap(){
        return userToPortMap;
    }

}
