package com.mcoding.pangolin.server.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author wzt on 2019/7/3.
 * @version 1.0
 */
public class PropertyUtils {

    private static Properties property = new Properties();

    static {
        try (InputStream in = PropertyUtils.class.getResourceAsStream("/conf.properties")) {
            property.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String get(String key) {
        return property.getProperty(key);
    }

    public static Integer getInt(String key) {
        return Integer.valueOf(property.getProperty(key));
    }

}
