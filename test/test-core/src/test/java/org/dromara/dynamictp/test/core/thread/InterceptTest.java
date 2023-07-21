package org.dromara.dynamictp.test.core.thread;

import cn.hutool.core.collection.CollectionUtil;
import org.dromara.dynamictp.core.plugin.DtpInterceptorRegistry;

/**
 * @author hanli
 * @date 2023年07月19日 3:33 PM
 */
public class InterceptTest {

    public static class TestA {

        public void execute() {
            beforeExecute();
            System.out.println("execute");
            afterExecute();
        }

        public void beforeExecute() {
            System.out.println("beforeExecute");
        }

        public void afterExecute() {
            System.out.println("afterExecute");
        }

    }

    public static void main(String[] args) {
        TestAInterceptor testAInterceptor = new TestAInterceptor();
        DtpInterceptorRegistry.register("TestAInterceptor", testAInterceptor);
        TestA testA = new TestA();
        TestA testA1 = (TestA) DtpInterceptorRegistry.plugin(testA, CollectionUtil.newHashSet("TestAInterceptor"));
        testA1.execute();
    }
}
