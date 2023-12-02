package org.dromara.dynamictp.example.wrapper;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;

/**
 * CustomTaskWrapper related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
public class CustomTaskWrapper implements TaskWrapper {

    @Override
    public String name() {
        return "custom";
    }

    @Override
    public Runnable wrap(Runnable runnable) {
        return new MyRunnable(runnable);
    }

    public static class MyRunnable implements Runnable {

        private final Runnable runnable;

        public MyRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            log.info("before run");
            runnable.run();
            log.info("after run");
        }
    }
}
