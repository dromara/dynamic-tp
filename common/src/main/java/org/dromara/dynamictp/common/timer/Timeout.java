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

package org.dromara.dynamictp.common.timer;

/**
 * A handle associated with a {@link TimerTask} that is returned by a{@link Timer}.
 * <p>
 * Copy from dubbo, see <a href="https://github.com/apache/dubbo/blob/3.2/dubbo-common/src/main/java/org/apache/dubbo/common/timer/Timeout.java">here</a> for more details.
 * </p>
 */
public interface Timeout {

    /**
     * Returns the {@link Timer} that created this handle.
     *
     * @return the {@link Timer} that created this handle
     */
    Timer timer();

    /**
     * Returns the {@link TimerTask} which is associated with this handle.
     *
     * @return the {@link TimerTask} which is associated with this handle
     */
    TimerTask task();

    /**
     * Returns {@code true} if and only if the {@link TimerTask} associated
     * with this handle has been expired.
     *
     * @return {@code true} if and only if the {@link TimerTask} associated
     */
    boolean isExpired();

    /**
     * Returns {@code true} if and only if the {@link TimerTask} associated
     * with this handle has been cancelled.
     *
     * @return {@code true} if and only if the {@link TimerTask} associated
     */
    boolean isCancelled();

    /**
     * Attempts to cancel the {@link TimerTask} associated with this handle.
     * If the task has been executed or cancelled already, it will return with
     * no side effect.
     *
     * @return True if the cancellation completed successfully, otherwise false
     */
    boolean cancel();
}
