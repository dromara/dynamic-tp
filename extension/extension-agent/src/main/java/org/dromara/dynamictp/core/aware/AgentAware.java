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

package org.dromara.dynamictp.core.aware;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * deal agent wrapper
 * @author txbao
 */
@Slf4j
public class AgentAware extends TaskStatAware {
    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public String getName() {
        return "agent";
    }

    /**
     * dtpRunnableCache  key -> Runnable  value -> DtpRunnable
     */
    private final Map<Runnable, SoftReference<DtpRunnable>> dtpRunnableCache = new ConcurrentHashMap<>();

    private Pair<Field, DtpRunnable> getDtpRunnable(Class<? extends Runnable> rClass, Runnable r) throws IllegalAccessException {
        while (Runnable.class.isAssignableFrom(rClass)) {
            Field[] declaredFields = rClass.getDeclaredFields();
            if (ArrayUtil.isNotEmpty(declaredFields)) {
                Field field = Arrays.stream(declaredFields)
                        .filter(ele -> Runnable.class == ele.getType())
                        .findFirst()
                        .orElse(null);
                if (field != null) {
                    field.setAccessible(true);
                    Runnable o = (Runnable) field.get(r);
                    if (o instanceof DtpRunnable) {
                        return new Pair<>(field, (DtpRunnable) o);
                    }

                    // 纵向查找
                    return getDtpRunnable(o.getClass(), o);
                }
                if (!Runnable.class.isAssignableFrom(rClass.getSuperclass())) {
                    break;
                }
                rClass = (Class<? extends Runnable>) rClass.getSuperclass();
            }
        }
        return null;
    }

    private Runnable getDtpRunnableInstance(Runnable r) {
        if (r instanceof DtpRunnable) {
            return r;
        }

        Pair<Field, DtpRunnable> dtpRunnable = null;
        Class<? extends Runnable> rClass = r.getClass();
        try {
            dtpRunnable = getDtpRunnable(rClass, r);
        } catch (IllegalAccessException e) {
            log.error("getDtpRunnable Error", e);
        }

        if (dtpRunnable == null) {
            if (log.isWarnEnabled()) {
                log.warn("DynamicTp aware [{}], can not find DtpRunnable.", getName());
            }
            return r;
        }

        return dtpRunnable.getValue();
    }

    @Override
    public Runnable beforeExecuteWrap(Executor executor, Thread t, Runnable r) {
        Runnable runnableWrap = getDtpRunnableInstance(r);
        if (runnableWrap instanceof DtpRunnable) {
            dtpRunnableCache.put(r, new SoftReference<>((DtpRunnable) runnableWrap));
        }
        return runnableWrap;
    }

    @Override
    public Runnable afterExecuteWrap(Executor executor, Runnable r, Throwable t) {
        SoftReference<DtpRunnable> remove = dtpRunnableCache.remove(r);
        if (remove != null) {
            return remove.get();
        }
        return getDtpRunnableInstance(r);
    }

    @Override
    public Runnable beforeRejectWrap(Runnable r, Executor executor) {
        SoftReference<DtpRunnable> remove = dtpRunnableCache.remove(r);
        if (remove != null) {
            return remove.get();
        }
        return getDtpRunnableInstance(r);
    }

    @Override
    public Runnable afterRejectWrap(Runnable r, Executor executor) {
        SoftReference<DtpRunnable> remove = dtpRunnableCache.remove(r);
        if (remove != null) {
            return remove.get();
        }
        return getDtpRunnableInstance(r);
    }
}
