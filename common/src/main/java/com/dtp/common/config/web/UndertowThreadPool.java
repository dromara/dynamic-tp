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
     * Number of core worker threads, internal default the coreThreads = maxThreads
     */
    private int coreWorkerThreads = 8 * ioThreads;

    /**
     * Number of max worker threads
     */
    private int maxWorkerThreads = 8 * ioThreads;

    /**
     * Worker thread keep alive, unit s
     */
    private int workerKeepAlive;
}
