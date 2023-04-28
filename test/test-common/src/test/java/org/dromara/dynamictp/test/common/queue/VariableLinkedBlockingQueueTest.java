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
