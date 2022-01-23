package com.dtp.common.config.web;

import lombok.Data;

/**
 * TomcatThreadPool related
 *
 * @author yanhom
 */
@Data
public class JettyThreadPool {

    /**
     * Maximum number of threads.
     */
    private int max = 200;

    /**
     * Minimum number of threads.
     */
    private int min = 8;
}
