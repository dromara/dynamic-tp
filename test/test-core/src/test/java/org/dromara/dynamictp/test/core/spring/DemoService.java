package org.dromara.dynamictp.test.core.spring;

import java.util.concurrent.Executor;

/**
 * @author fei biao team
 * @version $
 * Date: 2023/4/22
 * Time: 14:29
 */
public class DemoService {
    private final Executor asyncExecutor;

    public DemoService(Executor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }
}
