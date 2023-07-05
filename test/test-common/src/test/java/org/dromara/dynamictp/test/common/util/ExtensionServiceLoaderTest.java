package org.dromara.dynamictp.test.common.util;

import org.dromara.dynamictp.common.util.ExtensionServiceLoader;
import org.dromara.dynamictp.core.plugin.DtpInterceptor;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.List;

/**
 * ExtensionServiceLoader test case
 */
public class ExtensionServiceLoaderTest {
    
    @Test
    public void test1(){
        List<DtpInterceptor> loader = ExtensionServiceLoader.loader(DtpInterceptor.class);
        Assertions.assertTrue(loader.stream().anyMatch(it->it instanceof TestInterceptorLoader));
    }

}
