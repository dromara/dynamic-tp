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

package org.dromara.dynamictp.core.support.task.runnable;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * NamedRunnable related
 *
 * @author yanhom
 * @since 1.0.6
 */
public class NamedRunnable implements Runnable {

    private final Runnable runnable;

    private final String name;

    public NamedRunnable(Runnable runnable, String name) {
        this.runnable = runnable;
        this.name = name;
    }

    @Override
    public void run() {
        this.runnable.run();
    }

    public String getName() {
        return name;
    }

    public static NamedRunnable of(Runnable runnable, String name) {
        if (StringUtils.isBlank(name)) {
            name = runnable.getClass().getSimpleName() + "-" + UUID.randomUUID();
        }
        return new NamedRunnable(runnable, name);
    }
}
