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

package org.dromara.dynamictp.test.common.queue;

import org.dromara.dynamictp.common.queue.VariableLinkedBlockingQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * VariableLinkedBlockingQueueTest related
 *
 * @author yanhom
 * @since 1.1.0
 */
class VariableLinkedBlockingQueueTest {

    @Test
    void testCapacitySet() {
        VariableLinkedBlockingQueue<String> queue = new VariableLinkedBlockingQueue<>();
        Assertions.assertEquals(Integer.MAX_VALUE, queue.remainingCapacity() + queue.size());
        queue.setCapacity(1000);
        Assertions.assertEquals(1000, queue.remainingCapacity() + queue.size());
    }
}
