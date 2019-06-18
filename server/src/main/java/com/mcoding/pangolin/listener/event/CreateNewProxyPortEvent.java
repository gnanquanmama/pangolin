package com.mcoding.pangolin.listener.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author wzt on 2019/6/18.
 * @version 1.0
 */
@Data
@AllArgsConstructor
public class CreateNewProxyPortEvent {

    private Integer proxyPort;
    private boolean ssl = false;

}
