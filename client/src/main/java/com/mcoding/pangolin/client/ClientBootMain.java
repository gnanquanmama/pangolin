package com.mcoding.pangolin.client;

import com.mcoding.pangolin.client.container.ClientContainer;
import com.mcoding.pangolin.client.entity.AddressBridgeInfo;
import com.mcoding.pangolin.common.PangolinEngine;
import com.mcoding.pangolin.common.util.PropertyUtils;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

/**
 * @author wzt on 2019/6/17.
 * @version 1.0
 */
public class ClientBootMain {


    public static void main(String[] args) throws ParseException {
        AddressBridgeInfo addressBridgeInfo = buildProxyInfo(args);
        PangolinEngine.start(new ClientContainer(addressBridgeInfo));
    }


    /**
     * 根据命令行操作构建代理机器连接信息
     *
     * @param args
     * @return
     * @throws ParseException
     */
    private static AddressBridgeInfo buildProxyInfo(String[] args) throws ParseException {

        // 读取配置文件信息，初始化 管道的桥接信息
        AddressBridgeInfo addressBridgeInfo = new AddressBridgeInfo();
        addressBridgeInfo.setPrivateKey(PropertyUtils.get("private_key"));
        addressBridgeInfo.setTargetServerHost(PropertyUtils.get("target_server_host"));
        addressBridgeInfo.setTargetServerPort(PropertyUtils.getInt("target_Server_port"));
        addressBridgeInfo.setIntranetProxyServerHost(PropertyUtils.get("intranet_proxy_server_host"));
        addressBridgeInfo.setIntranetProxyServerPort(PropertyUtils.getInt("intranet_proxy_server_port"));


        Options argOptions = buildOption();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(argOptions, args);

        String privateKey = cmd.getOptionValue("p_key");
        if (StringUtils.isNoneBlank(privateKey)) {
            addressBridgeInfo.setPrivateKey(privateKey);
        }

        String intranetProxyServerHost = cmd.getOptionValue("i_host");
        if (StringUtils.isNoneBlank(intranetProxyServerHost)) {
            addressBridgeInfo.setIntranetProxyServerHost(intranetProxyServerHost);
        }
        String intranetProxyServerPort = cmd.getOptionValue("i_port");
        if (StringUtils.isNoneBlank(intranetProxyServerPort)) {
            addressBridgeInfo.setIntranetProxyServerPort(Integer.valueOf(intranetProxyServerPort));
        }
        String targetServerHost = cmd.getOptionValue("t_host");
        if (StringUtils.isNoneBlank(targetServerHost)) {
            addressBridgeInfo.setTargetServerHost(targetServerHost);
        }
        String targetServerPort = cmd.getOptionValue("t_port");
        if (StringUtils.isNoneBlank(targetServerPort)) {
            addressBridgeInfo.setTargetServerPort(Integer.valueOf(targetServerPort));
        }

        return addressBridgeInfo;
    }

    private static Options buildOption() {
        Options options = new Options();
        options.addOption("p_key", true, "");
        options.addOption("i_host", true, "");
        options.addOption("i_port", true, "");
        options.addOption("t_host", true, "");
        options.addOption("t_port", true, "");

        return options;
    }
}
