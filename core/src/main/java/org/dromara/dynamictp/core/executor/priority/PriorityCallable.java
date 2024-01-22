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

import lombok.Getter;

import java.util.concurrent.Callable;

/**
 * PriorityCallable related
 *
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 * @since 1.1.7
 */
public class PriorityCallable<V> implements Priority, Callable<V> {

    private final Callable<V> callable;

    @Getter
    private final int priority;

    private PriorityCallable(Callable<V> callable, int priority) {
        this.callable = callable;
        this.priority = priority;
    }

    public static <T> Callable<T> of(Callable<T> task, int i) {
        return new PriorityCallable<>(task, i);
    }

    @Override
    public V call() throws Exception {
        return callable.call();
    }

}
