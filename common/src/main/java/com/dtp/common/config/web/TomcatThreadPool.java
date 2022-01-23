package com.dtp.common.config.web;

import lombok.Data;

/**
 * TomcatThreadPool related
 *
 * @author yanhom
 */
@Data
public class TomcatThreadPool {

    /**
     * Maximum amount of worker threads.
     */
    private int max = 200;

    /**
     * Minimum amount of worker threads.
     */
    private int minSpare = 10;
}
