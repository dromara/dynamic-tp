package com.dtp.test.core.notify.capture;

import com.dtp.common.queue.VariableLinkedBlockingQueue;
import com.dtp.core.notifier.capture.CapturedBlockingQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.BlockingQueue;

/**
 * CapturedBlockingQueueTest related
 *
 * @author ruoan
 * @since 1.1.3
 */
public class CapturedBlockingQueueTest {

    @Test
    public void testBlockingQueueDefaultCapacity() {
        BlockingQueue<Runnable> queue = new VariableLinkedBlockingQueue<>();
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(queue);
        Assertions.assertEquals(0, capturedBlockingQueue.size());
        Assertions.assertEquals(Integer.MAX_VALUE, capturedBlockingQueue.remainingCapacity());
    }

    @Test
    public void testBlockingQueueCapacitySet() {
        final int capacity = 100;
        BlockingQueue<Runnable> queue = new VariableLinkedBlockingQueue<>(capacity);
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(queue);
        Assertions.assertEquals(capacity, capturedBlockingQueue.remainingCapacity());
    }

    @Test
    public void testPut() {
        BlockingQueue<Runnable> queue = new VariableLinkedBlockingQueue<>();
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(queue);
        Assertions.assertThrows(UnsupportedOperationException.class, () ->
                capturedBlockingQueue.put(() -> System.out.println("can't put Runnable to CapturedBlockingQueue")));
    }

    @Test
    public void testTake() {
        BlockingQueue<Runnable> queue = new VariableLinkedBlockingQueue<>();
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(queue);
        Assertions.assertThrows(UnsupportedOperationException.class, capturedBlockingQueue::take);
    }

    @Test
    public void testBlockingQueuePut() throws InterruptedException {
        final int capacity = 100;
        final int firstPutSize = 30;
        final int secondPutSize = 40;

        BlockingQueue<Runnable> queue = new VariableLinkedBlockingQueue<>(capacity);
        putTaskToQueue(firstPutSize, queue);
        CapturedBlockingQueue capturedBlockingQueue = new CapturedBlockingQueue(queue);

        Assertions.assertEquals(capacity - firstPutSize, capturedBlockingQueue.remainingCapacity());
        Assertions.assertEquals(firstPutSize, capturedBlockingQueue.size());

        putTaskToQueue(secondPutSize, queue);

        //The status of VariableLinkedBlockingQueue changes dynamically as tasks are added to it.
        Assertions.assertEquals(capacity - firstPutSize - secondPutSize, queue.remainingCapacity());
        Assertions.assertEquals(firstPutSize + secondPutSize, queue.size());

        //The status of CapturedBlockingQueue remains unchanged after creation.
        Assertions.assertEquals(capacity - firstPutSize, capturedBlockingQueue.remainingCapacity());
        Assertions.assertEquals(firstPutSize, capturedBlockingQueue.size());
    }

    private void putTaskToQueue(int size, BlockingQueue<Runnable> queue) throws InterruptedException {
        for (int i = 0; i < size; i++) {
            queue.put(() -> System.out.println("i am mock task"));
        }
    }
}
