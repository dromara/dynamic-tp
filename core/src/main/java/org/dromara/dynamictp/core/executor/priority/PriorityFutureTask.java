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

package org.dromara.dynamictp.core.executor.priority;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * PriorityFutureTask related
 *
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 * @since 1.1.7
 */
public class PriorityFutureTask<V> extends FutureTask<V> implements Priority {

    /**
     * The runnable.
     */
    private final Priority obj;

    private final int priority;

    public PriorityFutureTask(Runnable runnable, V result) {
        super(runnable, result);
        this.obj = (PriorityRunnable) runnable;
        this.priority = this.obj.getPriority();
    }

    public PriorityFutureTask(Callable<V> callable) {
        super(callable);
        this.obj = (PriorityCallable<V>) callable;
        this.priority = this.obj.getPriority();
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

}
