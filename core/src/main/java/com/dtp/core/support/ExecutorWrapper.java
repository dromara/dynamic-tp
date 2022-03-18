package com.dtp.core.support;

import lombok.Data;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Wrap juc ThreadPoolExecutor
 *
 * @author: yanhom
 * @since 1.0.3
 **/
@Data
public class ExecutorWrapper {

    private String threadPoolName;

    private ThreadPoolExecutor executor;
}
