package org.dromara.dynamictp.test.core;

import org.dromara.dynamictp.core.DtpRegistry;
import org.dromara.dynamictp.core.support.ExecutorWrapper;
import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.dromara.dynamictp.core.thread.DtpExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * DtpRegistryTest related
 *
 * @author yanhom
 * @since 1.1.0
 */
class DtpRegistryTest {

    @Test
    void testRegisterDtp() {
        DtpExecutor dtpExecutor = ThreadPoolBuilder.newBuilder()
                .threadPoolName("test_dtp")
                .buildDynamic();
        DtpRegistry.registerExecutor(ExecutorWrapper.of(dtpExecutor), "test");
        Assertions.assertEquals("test_dtp", ((DtpExecutor)DtpRegistry.getExecutor("test_dtp")).getThreadPoolName());
    }

}
