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

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;

/**
 * TaskExecAware related
 *
 * @author yanhom
 * @since 1.1.4
 **/
@Slf4j
public class TaskExecAware implements ExecutorAware {

    @Override
    public int getOrder() {
        return AwareTypeEnum.TASK_EXEC_AWARE.getOrder();
    }

    @Override
    public String getName() {
        return AwareTypeEnum.TASK_EXEC_AWARE.getName();
    }

    @Override
    public void afterExecute(Executor executor, Runnable r, Throwable t) {
        tryPrintError(r, t);
        clearContext();
    }

    private void clearContext() {
        MDC.remove(TRACE_ID);
    }

    private void tryPrintError(Runnable r, Throwable t) {
        if (Objects.nonNull(t)) {
            log.error("thread {} throw exception {}", Thread.currentThread(), t.getMessage(), t);
            return;
        }
        if (r instanceof FutureTask) {
            try {
                Future<?> future = (Future<?>) r;
                future.get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("thread {} throw exception {}", Thread.currentThread(), e.getMessage(), e);
            }
        }
    }
}
