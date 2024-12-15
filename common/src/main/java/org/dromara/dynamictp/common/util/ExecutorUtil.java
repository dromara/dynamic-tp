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

package org.dromara.dynamictp.common.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Objects;
import java.util.concurrent.FutureTask;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;

/**
 * ExecutorUtil related
 *
 * @author yanhom
 * @since 1.1.9
 */
@Slf4j
public final class ExecutorUtil {

    private ExecutorUtil() {
    }

    public static void tryExecAfterExecute(Runnable r, Throwable t) {
        tryPrintError(r, t);
        tryClearContext();
    }

    private static void tryPrintError(Runnable r, Throwable t) {
        if (Objects.nonNull(t)) {
            log.error("DynamicTp execute, thread {} throw exception, traceId {}",
                    Thread.currentThread(), MDC.get(TRACE_ID), t);
            return;
        }
        if (r instanceof FutureTask) {
            try {
                FutureTask<?> future = (FutureTask<?>) r;
                if (future.isDone() && !future.isCancelled()) {
                    future.get();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("DynamicTp execute, thread {} throw exception, traceId {}",
                        Thread.currentThread(), MDC.get(TRACE_ID), e);
            }
        }
    }

    public static void tryClearContext() {
        MDC.remove(TRACE_ID);
    }
}
