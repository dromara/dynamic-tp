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

package org.dromara.dynamictp.extension.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.core.aware.TaskStatAware;
import org.dromara.dynamictp.core.support.task.runnable.DtpRunnable;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.DTP_EXECUTE_ENHANCED;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.FALSE_STR;

/**
 * deal agent wrapper
 * @author txbao
 */
@Slf4j
@SuppressWarnings("all")
public class AgentAware extends TaskStatAware {

    /**
     * dtpRunnableCache  key -> Runnable  value -> DtpRunnable
     */
    private final Map<Runnable, SoftReference<DtpRunnable>> dtpRunnableCache = new ConcurrentHashMap<>();

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public String getName() {
        return "agent";
    }

    private DtpRunnable determineDtpRunnable(List<Field> conditionalFields, Runnable r, Set<Class> visitedClass) throws IllegalAccessException {
        for (Field field : conditionalFields) {
            if (Objects.isNull(field)) {
                continue;
            }
            field.setAccessible(true);
            Runnable o = (Runnable) field.get(r);
            if (o instanceof DtpRunnable) {
                return (DtpRunnable) o;
            }
            if (Objects.isNull(o) || CollUtil.contains(visitedClass, o.getClass())) {
                return null;
            } else {
                visitedClass.add(o.getClass());
            }
            // 纵向查找
            DtpRunnable dtpRunnable = getDtpRunnable(o.getClass(), o, visitedClass);
            if (dtpRunnable != null) {
                return dtpRunnable;
            }
        }
        return null;
    }

    private DtpRunnable getDtpRunnable(Class<? extends Runnable> rClass, Runnable r, Set<Class> visitedClass) throws IllegalAccessException {
        while (Runnable.class.isAssignableFrom(rClass)) {
            Field[] declaredFields = rClass.getDeclaredFields();
            if (ArrayUtil.isNotEmpty(declaredFields)) {
                List<Field> conditionFields = Arrays.stream(declaredFields)
                        .filter(ele -> Runnable.class.isAssignableFrom(ele.getType()))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(conditionFields)) {
                    DtpRunnable dtpRunnable = determineDtpRunnable(conditionFields, r, visitedClass);
                    if (Objects.nonNull(dtpRunnable)) {
                        return dtpRunnable;
                    }
                }
            }
            if (!Runnable.class.isAssignableFrom(rClass.getSuperclass())) {
                break;
            }
            rClass = (Class<? extends Runnable>) rClass.getSuperclass();
        }
        return null;
    }

    private Runnable getDtpRunnableInstance(Runnable r) {
        if (r instanceof DtpRunnable) {
            return r;
        }
        DtpRunnable dtpRunnable = null;
        Class<? extends Runnable> rClass = r.getClass();
        try {
            dtpRunnable = getDtpRunnable(rClass, r, new HashSet<>());
        } catch (IllegalAccessException e) {
            log.error("getDtpRunnable Error", e);
        }
        if (dtpRunnable == null) {
            if (log.isDebugEnabled()) {
                log.debug("DynamicTp aware [{}], can not find DtpRunnable.", getName());
            }
            return r;
        }
        return dtpRunnable;
    }

    @Override
    public Runnable beforeExecuteWrap(Executor executor, Thread t, Runnable r) {
        Runnable runnableWrap = getDtpRunnableInstance(r);
        if (runnableWrap instanceof DtpRunnable) {
            dtpRunnableCache.put(r, new SoftReference<>((DtpRunnable) runnableWrap));
        } else {
            // 被封装的wrapper没有找到DtpRunnable对象，那么就关闭某些监控指标，防止内存溢出
            System.setProperty(DTP_EXECUTE_ENHANCED, FALSE_STR);
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
}
