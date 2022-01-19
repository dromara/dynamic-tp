package com.dtp.common.em;

import com.dtp.common.VariableLinkedBlockingQueue;
import com.dtp.common.ex.DtpException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.*;

/**
 * QueueTypeEnum related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
@Getter
public enum QueueTypeEnum {

    /**
     * BlockingQueue type.
     */
    ARRAY_BLOCKING_QUEUE(1,"ArrayBlockingQueue"),

    LINKED_BLOCKING_QUEUE(2,"LinkedBlockingQueue"),

    PRIORITY_BLOCKING_QUEUE(3,"PriorityBlockingQueue"),

    DELAY_QUEUE(4,"DelayQueue"),

    SYNCHRONOUS_QUEUE(5,"SynchronousQueue"),

    LINKED_TRANSFER_QUEUE(6,"LinkedTransferQueue"),

    LINKED_BLOCKING_DEQUE(7,"LinkedBlockingDeque"),

    VARIABLE_LINKED_BLOCKING_QUEUE(8,"VariableLinkedBlockingQueue");

    private final Integer code;
    private final String name;

    QueueTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static BlockingQueue<Runnable> buildBlockingQueue(String name, int capacity, boolean fair) {
        BlockingQueue<Runnable> blockingQueue = null;
        if (Objects.equals(name, ARRAY_BLOCKING_QUEUE.getName())) {
            blockingQueue = new ArrayBlockingQueue<>(capacity);
        } else if (Objects.equals(name, LINKED_BLOCKING_QUEUE.getName())) {
            blockingQueue = new LinkedBlockingQueue<>(capacity);
        } else if (Objects.equals(name, PRIORITY_BLOCKING_QUEUE.getName())) {
            blockingQueue = new PriorityBlockingQueue<>(capacity);
        } else if (Objects.equals(name, DELAY_QUEUE.getName())) {
            blockingQueue = new DelayQueue();
        } else if (Objects.equals(name, SYNCHRONOUS_QUEUE.getName())) {
            blockingQueue = new SynchronousQueue<>(fair);
        } else if (Objects.equals(name, LINKED_TRANSFER_QUEUE.getName())) {
            blockingQueue = new LinkedTransferQueue<>();
        } else if (Objects.equals(name, LINKED_BLOCKING_DEQUE.getName())) {
            blockingQueue = new LinkedBlockingDeque<>(capacity);
        } else if (Objects.equals(name, VARIABLE_LINKED_BLOCKING_QUEUE.getName())) {
            blockingQueue = new VariableLinkedBlockingQueue<>(capacity);
        }
        if (blockingQueue != null) {
            return blockingQueue;
        }

        log.error("Cannot find specified BlockingQueue {}", name);
        throw new DtpException("Cannot find specified BlockingQueue " + name);
    }
}
