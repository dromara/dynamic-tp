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

package org.dromara.dynamictp.extension.opentelemetry.wrapper;

import io.opentelemetry.api.trace.Span;
import org.dromara.dynamictp.core.support.task.runnable.MdcRunnable;
import org.dromara.dynamictp.core.support.task.wrapper.TaskWrapper;
import io.opentelemetry.context.Context;
import org.slf4j.MDC;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;

/**
 * OpenTelemetryWrapper related
 *
 * @author weishaopeng
 * @since 1.1.3
 **/
public class OpenTelemetryWrapper implements TaskWrapper {

    private static final String NAME = "OTel";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Runnable wrap(Runnable runnable) {
        Context context = Context.current();
        //把Trace信息传入DynamicTP中
         MDC.put(TRACE_ID, Span.current().getSpanContext().getTraceId());
        // 被wrap方法包装后，该Executor执行的所有Runnable都会跑在特定的context中
        return MdcRunnable.get(context.wrap(runnable));
    }
}
