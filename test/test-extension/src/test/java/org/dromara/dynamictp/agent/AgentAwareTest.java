/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dromara.dynamictp.agent;

import org.dromara.dynamictp.extension.agent.AgentAware;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AgentAwareTest {

    @Test
    public void testDirectOnlyOneDtpRunnable() throws InvocationTargetException, IllegalAccessException {
        Method getDtpRunnableInstance = ReflectionUtils.findMethod(AgentAware.class, "getDtpRunnableInstance", Runnable.class);
        Assertions.assertNotNull(getDtpRunnableInstance);

        getDtpRunnableInstance.setAccessible(true);

        Runnable runnable = () -> {

        };


        MyAgentWrapper myAgentWrapper = new MyAgentWrapper(runnable, new Object());
        Object result = getDtpRunnableInstance.invoke(new AgentAware(), myAgentWrapper);
        Assertions.assertTrue(result == myAgentWrapper);

        DtpRunnable dtpRunnable = new DtpRunnable(runnable, runnable, "test");
        myAgentWrapper = new MyAgentWrapper(dtpRunnable, new Object());
        result = getDtpRunnableInstance.invoke(new AgentAware(), myAgentWrapper);
        Assertions.assertNotNull(dtpRunnable == result);
    }

    @Test
    public void testDirectTwoRunnable() throws InvocationTargetException, IllegalAccessException {
        Runnable runnable = () -> {

        };
        DtpRunnable dtpRunnable = new DtpRunnable(runnable, runnable, "test");
        MyAgentWrapperTwoRunnable myAgentWrapper = new MyAgentWrapperTwoRunnable(dtpRunnable, runnable, "test");

        Method getDtpRunnableInstance = ReflectionUtils.findMethod(AgentAware.class, "getDtpRunnableInstance", Runnable.class);
        Assertions.assertNotNull(getDtpRunnableInstance);
        getDtpRunnableInstance.setAccessible(true);

        Object result = getDtpRunnableInstance.invoke(new AgentAware(), myAgentWrapper);
        Assertions.assertTrue(result == dtpRunnable);

        myAgentWrapper = new MyAgentWrapperTwoRunnable(runnable, dtpRunnable, "test");
        result = getDtpRunnableInstance.invoke(new AgentAware(), myAgentWrapper);
        Assertions.assertTrue(result == dtpRunnable);
    }

    @Test
    public void testNotDirectRunnable() throws InvocationTargetException, IllegalAccessException {
        Runnable runnable = () -> {

        };
        DtpRunnable dtpRunnable = new DtpRunnable(runnable, runnable, "test");
        MyAgentWrapper myAgentWrapper = new MyAgentWrapper(dtpRunnable, new Object());

        MyAgentTwoPathRunnableWrapper twoPathRunnableWrapper = new MyAgentTwoPathRunnableWrapper(myAgentWrapper, new Object());
        Method getDtpRunnableInstance = ReflectionUtils.findMethod(AgentAware.class, "getDtpRunnableInstance", Runnable.class);
        Assertions.assertNotNull(getDtpRunnableInstance);
        getDtpRunnableInstance.setAccessible(true);

        Object result = getDtpRunnableInstance.invoke(new AgentAware(), twoPathRunnableWrapper);
        Assertions.assertTrue(result == dtpRunnable);
    }

    @Test
    public void testExtendRunnable() throws InvocationTargetException, IllegalAccessException {
        Runnable runnable = () -> {

        };
        DtpRunnable dtpRunnable = new DtpRunnable(runnable, runnable, "test");
        MyAgentWrapper myAgentWrapper = new MyAgentWrapper(dtpRunnable, new Object());
        MyAgentWrapperChild myAgentWrapperChild = new MyAgentWrapperChild(myAgentWrapper, new Object());

        Method getDtpRunnableInstance = ReflectionUtils.findMethod(AgentAware.class, "getDtpRunnableInstance", Runnable.class);
        Assertions.assertNotNull(getDtpRunnableInstance);
        getDtpRunnableInstance.setAccessible(true);

        Object result = getDtpRunnableInstance.invoke(new AgentAware(), myAgentWrapperChild);
        Assertions.assertTrue(result == dtpRunnable);
    }

    @Test
    public void testDeepRunnable() throws InvocationTargetException, IllegalAccessException {
        Runnable runnable = () -> {

        };
        DtpRunnable dtpRunnable = new DtpRunnable(runnable, runnable, "test");
        MyAgentWrapper myAgentWrapper = new MyAgentWrapper(runnable, new Object());
        MyAgentWrapper myAgentWrapperDtpRunnable = new MyAgentWrapper(dtpRunnable, new Object());
        MyAgentWrapperChild myAgentWrapperChild = new MyAgentWrapperChild(myAgentWrapperDtpRunnable, new Object());

        MyAgentTwoPathRunnableChildWrapper myAgentTwoPathRunnableChildWrapper = new MyAgentTwoPathRunnableChildWrapper(myAgentWrapperChild,
                myAgentWrapper, new Object());

        Method getDtpRunnableInstance = ReflectionUtils.findMethod(AgentAware.class, "getDtpRunnableInstance", Runnable.class);
        Assertions.assertNotNull(getDtpRunnableInstance);
        getDtpRunnableInstance.setAccessible(true);
        Object result = getDtpRunnableInstance.invoke(new AgentAware(), myAgentTwoPathRunnableChildWrapper);
        Assertions.assertTrue(result == dtpRunnable);
    }

}
