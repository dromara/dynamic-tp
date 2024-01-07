package org.dromara.dynamictp.test.core.support;

import org.dromara.dynamictp.core.support.ThreadPoolBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
public class ThreadPoolBuilderTest {

    @Test
    void testBuildDynamic() {
        Assertions.assertThrows(IllegalArgumentException.class,() -> ThreadPoolBuilder.newBuilder()
                .threadPoolName("dtpExecutor1")
                .priority()
                .ordered()
                .buildDynamic());
    }

}
