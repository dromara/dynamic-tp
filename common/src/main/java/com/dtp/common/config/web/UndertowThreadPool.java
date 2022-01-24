package com.dtp.common.config.web;

import lombok.Data;

/**
 * TomcatThreadPool related
 *
 * @author yanhom
 */
@Data
public class UndertowThreadPool {

    /**
     * Number of io threads.
     */
    private int ioThreads = 8;

    /**
     * Number of worker threads.
     */
    private int workerThreads = 8 * ioThreads;
}
