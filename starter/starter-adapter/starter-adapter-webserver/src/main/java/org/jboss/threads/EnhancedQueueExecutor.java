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

/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.threads;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Thread.currentThread;
import static java.security.AccessController.doPrivileged;
import static java.security.AccessController.getContext;
import static java.util.concurrent.locks.LockSupport.parkNanos;
import static java.util.concurrent.locks.LockSupport.unpark;
import static org.jboss.threads.JBossExecutors.unsafe;

import java.lang.management.ManagementFactory;
import java.security.AccessControlContext;
import java.security.PrivilegedAction;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jboss.threads.management.ManageableThreadPoolExecutorService;
import org.jboss.threads.management.StandardThreadPoolMXBean;
import org.wildfly.common.Assert;
import org.wildfly.common.cpu.ProcessorInfo;

/**
 * A task-or-thread queue backed thread pool executor service.  Tasks are added in a FIFO manner, and consumers in a LIFO manner.
 * Threads are only ever created in the event that there are no idle threads available to service a task, which, when
 * combined with the LIFO-based consumer behavior, means that the thread pool will generally trend towards the minimum
 * necessary size.  In addition, the optional {@linkplain #setGrowthResistance(float) growth resistance feature} can
 * be used to further govern the thread pool size.
 * <p>
 * Additionally, this thread pool implementation supports scheduling of tasks.
 * The scheduled tasks will execute on the main pool.
 * <p>
 * New instances of this thread pool are created by constructing and configuring a {@link Builder} instance, and calling
 * its {@link Builder#build() build()} method.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
public class EnhancedQueueExecutor extends EnhancedQueueExecutorBase6 implements ManageableThreadPoolExecutorService, ScheduledExecutorService {
    private static final Thread[] NO_THREADS = new Thread[0];

    static {
        Version.getVersionString();
        MBeanUnregisterAction.forceInit();
    }

    /*
    ┌──────────────────────────┐
    │ Explanation of operation │
    └──────────────────────────┘

    The primary mechanism of this executor is the special linked list/stack.  This list has the following properties:
      • Multiple node types:
        ◦ Task nodes (TaskNode), which have the following properties:
          ▪ Strongly connected (no task is ever removed from the middle of the list; tail can always be found by following .next pointers)
          ▪ FIFO (insertion at tail.next, "removal" at head.next)
          ▪ Head and tail always refer to TaskNodes; head.next/tail.next are the "true" action points for all node types
          ▪ At least one dead task node is always in the list, thus the task is cleared after execution to avoid leaks
        ◦ Waiting consumer thread nodes (PoolThreadNode), which have the following properties:
          ▪ LIFO/FILO (insertion and removal at tail.next)
          ▪ Carrier for task handoff between producer and waiting consumer
        ◦ Waiting termination (awaitTermination) thread nodes (TerminateWaiterNode), which have the following properties:
          ▪ Append-only (insertion at tail.next.next...next)
          ▪ All removed at once when termination is complete
          ▪ If a thread stops waiting, the node remains (but its thread field is cleared to prevent (best-effort) useless unparks)
          ▪ Once cleared, the thread field can never be reinitialized
      • Concurrently accessed (multiple threads may read the list and update the head and tail pointers)
      • TaskNode.next may be any node type or null
      • PoolThreadNode.next may only be another PoolThreadNode or null
      • TerminateWaiterNode.next may only be another TerminateWaiterNode or null

    The secondary mechanism is the thread status atomic variable.  It is logically a structure with the following fields:
      • Current thread count (currentSizeOf(), withCurrentSize())
      • Core pool size (coreSizeOf(), withCoreSize())
      • Max pool size (maxSizeOf(), withMaxSize())
      • Shutdown-requested flag (isShutdownRequested(), withShutdownRequested())
      • Shutdown-with-interrupt requested flag (isShutdownInterrupt(), withShutdownInterrupt())
      • Shutdown-completed flag (isShutdownComplete(), withShutdownComplete())
      • The decision to create a new thread is affected by whether the number of active threads is less than the core size;
        if not, then the growth resistance factor is applied to decide whether the task should be enqueued or a new thread begun.
        Note: the default growth resistance factor is 0% resistance.

    The final mechanism is the queue status atomic variable.  It is logically a structure with the following fields:
      • Current queue size (currentQueueSizeOf(), withCurrentQueueSize())
      • Queue size limit (maxQueueSizeOf(), withMaxQueueSize())

     */

    // =======================================================
    // Optimization control flags
    // =======================================================

    /**
     * A global hint which establishes whether it is recommended to disable uses of {@code EnhancedQueueExecutor}.
     * This hint defaults to {@code false} but can be changed to {@code true} by setting the {@code jboss.threads.eqe.disable}
     * property to {@code true} before this class is initialized.
     */
    public static final boolean DISABLE_HINT = readBooleanPropertyPrefixed("disable", false);

    /**
     * Update the summary statistics.
     */
    static final boolean UPDATE_STATISTICS = readBooleanPropertyPrefixed("statistics", false);
    /**
     * Maintain an estimate of the number of threads which are currently doing work on behalf of the thread pool.
     */
    static final boolean UPDATE_ACTIVE_COUNT =
            UPDATE_STATISTICS || readBooleanPropertyPrefixed("statistics.active-count", false);
    /**
     * Suppress queue limit and size tracking for performance.
     */
    static final boolean NO_QUEUE_LIMIT = readBooleanPropertyPrefixed("unlimited-queue", false);
    /**
     * Set the default value for whether an mbean is to be auto-registered for the thread pool.
     */
    static final boolean REGISTER_MBEAN = readBooleanPropertyPrefixed("register-mbean", true);
    /**
     * Set to enable or disable MBean registration.
     */
    static final boolean DISABLE_MBEAN = readBooleanPropertyPrefixed("disable-mbean", readProperty("org.graalvm.nativeimage.imagecode", null) != null);
    /**
     * The number of times a thread should spin/yield before actually parking.
     */
    static final int PARK_SPINS = readIntPropertyPrefixed("park-spins", ProcessorInfo.availableProcessors() == 1 ? 0 : 1 << 7);
    /**
     * The yield ratio expressed as the number of spins that should yield.
     */
    static final int YIELD_FACTOR = max(min(readIntPropertyPrefixed("park-yields", 1), PARK_SPINS), 0);

    // =======================================================
    // Constants
    // =======================================================

    static final Executor DEFAULT_HANDLER = JBossExecutors.rejectingExecutor();

    // =======================================================
    // Immutable configuration fields
    // =======================================================

    /**
     * The thread factory.
     */
    private final ThreadFactory threadFactory;
    /**
     * The approximate set of pooled threads.
     */
    private final Set<Thread> runningThreads = Collections.newSetFromMap(new ConcurrentHashMap<>());
    /**
     * The management bean instance.
     */
    private final MXBeanImpl mxBean;
    /**
     * The MBean registration handle (if any).
     */
    private final Object handle;
    /**
     * The access control context of the creating thread.
     * Will be set to null when the MBean is not registered.
     */
    private volatile AccessControlContext acc;
    /**
     * The context handler for the user-defined context.
     */
    private final ContextHandler<?> contextHandler;
    /**
     * The task for scheduled execution.
     */
    private final SchedulerTask schedulerTask = new SchedulerTask();
    /**
     * The scheduler thread.
     */
    private final Thread schedulerThread;

    // =======================================================
    // Current state fields
    // =======================================================

    /**
     * The linked list of threads waiting for termination of this thread pool.
     */
    @SuppressWarnings("unused") // used by field updater
    volatile Waiter terminationWaiters;

    /**
     * Queue size:
     * <ul>
     *     <li>Bit 00..1F: current queue length</li>
     *     <li>Bit 20..3F: queue limit</li>
     * </ul>
     */
    @SuppressWarnings("unused") // used by field updater
    volatile long queueSize;

    /**
     * The thread keep-alive timeout value.
     */
    volatile long timeoutNanos;

    /**
     * A resistance factor applied after the core pool is full; values applied here will cause that fraction
     * of submissions to create new threads when no idle thread is available.   A value of {@code 0.0f} implies that
     * threads beyond the core size should be created as aggressively as threads within it; a value of {@code 1.0f}
     * implies that threads beyond the core size should never be created.
     */
    volatile float growthResistance;

    /**
     * The handler for tasks which cannot be accepted by the executor.
     */
    volatile Executor handoffExecutor;

    /**
     * The handler for uncaught exceptions which occur during user tasks.
     */
    volatile Thread.UncaughtExceptionHandler exceptionHandler;

    /**
     * The termination task to execute when the thread pool exits.
     */
    volatile Runnable terminationTask;

    // =======================================================
    // Statistics fields and counters
    // =======================================================

    /**
     * The peak number of threads ever created by this pool.
     */
    @SuppressWarnings("unused") // used by field updater
    volatile int peakThreadCount;
    /**
     * The approximate peak queue size.
     */
    @SuppressWarnings("unused") // used by field updater
    volatile int peakQueueSize;

    private final LongAdder submittedTaskCounter = new LongAdder();
    private final LongAdder completedTaskCounter = new LongAdder();
    private final LongAdder rejectedTaskCounter = new LongAdder();
    private final LongAdder spinMisses = new LongAdder();

    /**
     * The current active number of threads.
     */
    @SuppressWarnings("unused")
    volatile int activeCount;

    // =======================================================
    // Updaters
    // =======================================================

    private static final long terminationWaitersOffset;

    private static final long queueSizeOffset;

    private static final long peakThreadCountOffset;
    private static final long activeCountOffset;
    private static final long peakQueueSizeOffset;

    static {
        try {
            terminationWaitersOffset = unsafe.objectFieldOffset(EnhancedQueueExecutor.class.getDeclaredField("terminationWaiters"));

            queueSizeOffset = unsafe.objectFieldOffset(EnhancedQueueExecutor.class.getDeclaredField("queueSize"));

            peakThreadCountOffset = unsafe.objectFieldOffset(EnhancedQueueExecutor.class.getDeclaredField("peakThreadCount"));
            activeCountOffset = unsafe.objectFieldOffset(EnhancedQueueExecutor.class.getDeclaredField("activeCount"));
            peakQueueSizeOffset = unsafe.objectFieldOffset(EnhancedQueueExecutor.class.getDeclaredField("peakQueueSize"));
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldError(e.getMessage());
        }
    }

    // =======================================================
    // Thread state field constants
    // =======================================================

    private static final long TS_THREAD_CNT_MASK = 0b1111_1111_1111_1111_1111L; // 20 bits, can be shifted as needed

    // shift amounts
    private static final long TS_CURRENT_SHIFT   = 0;
    private static final long TS_CORE_SHIFT      = 20;
    private static final long TS_MAX_SHIFT       = 40;

    private static final long TS_ALLOW_CORE_TIMEOUT = 1L << 60;
    private static final long TS_SHUTDOWN_REQUESTED = 1L << 61;
    private static final long TS_SHUTDOWN_INTERRUPT = 1L << 62;
    @SuppressWarnings("NumericOverflow")
    private static final long TS_SHUTDOWN_COMPLETE = 1L << 63;

    private static final int EXE_OK = 0;
    private static final int EXE_REJECT_QUEUE_FULL = 1;
    private static final int EXE_REJECT_SHUTDOWN = 2;
    private static final int EXE_CREATE_THREAD = 3;

    private static final int AT_YES = 0;
    private static final int AT_NO = 1;
    private static final int AT_SHUTDOWN = 2;

    // =======================================================
    // Marker objects
    // =======================================================

    static final QNode TERMINATE_REQUESTED = new TerminateWaiterNode();
    static final QNode TERMINATE_COMPLETE = new TerminateWaiterNode();

    static final Waiter TERMINATE_COMPLETE_WAITER = new Waiter(null);

    static final Runnable WAITING = new NullRunnable();
    static final Runnable GAVE_UP = new NullRunnable();
    static final Runnable ACCEPTED = new NullRunnable();
    static final Runnable EXIT = new NullRunnable();

    // =======================================================
    // Constructor
    // =======================================================

    static final AtomicInteger sequence = new AtomicInteger(1);

    private final String mBeanName;

    public EnhancedQueueExecutor(final Builder builder) {
        super();
        int maxSize = builder.getMaximumPoolSize();
        int coreSize = min(builder.getCorePoolSize(), maxSize);
        this.handoffExecutor = builder.getHandoffExecutor();
        this.exceptionHandler = builder.getExceptionHandler();
        this.threadFactory = builder.getThreadFactory();
        this.schedulerThread = threadFactory.newThread(schedulerTask);
        String schedulerName = this.schedulerThread.getName();
        this.schedulerThread.setName(schedulerName + " (scheduler)");
        this.terminationTask = builder.getTerminationTask();
        this.growthResistance = builder.getGrowthResistance();
        this.contextHandler = builder.getContextHandler();
        final Duration keepAliveTime = builder.getKeepAliveTime();
        // initial dead node
        // thread stat
        threadStatus = withCoreSize(withMaxSize(withAllowCoreTimeout(0L, builder.allowsCoreThreadTimeOut()), maxSize), coreSize);
        timeoutNanos = TimeUtil.clampedPositiveNanos(keepAliveTime);
        queueSize = withMaxQueueSize(withCurrentQueueSize(0L, 0), builder.getMaximumQueueSize());
        mxBean = new MXBeanImpl();
        mBeanName = builder.getMBeanName();
        if (! DISABLE_MBEAN && builder.isRegisterMBean()) {
            this.acc = getContext();
            final String configuredName = builder.getMBeanName();
            final String finalName = configuredName != null ? configuredName : "threadpool-" + sequence.getAndIncrement();
            handle = doPrivileged(new MBeanRegisterAction(finalName, mxBean), acc);
        } else {
            handle = null;
            this.acc = null;
        }
    }

    static final class MBeanRegisterAction implements PrivilegedAction<ObjectInstance> {
        private final String finalName;
        private final MXBeanImpl mxBean;

        MBeanRegisterAction(final String finalName, final MXBeanImpl mxBean) {
            this.finalName = finalName;
            this.mxBean = mxBean;
        }

        public ObjectInstance run() {
            try {
                final Hashtable<String, String> table = new Hashtable<>();
                table.put("name", ObjectName.quote(finalName));
                table.put("type", "thread-pool");
                return ManagementFactory.getPlatformMBeanServer().registerMBean(mxBean, new ObjectName("jboss.threads", table));
            } catch (Throwable ignored) {
            }
            return null;
        }
    }

    // =======================================================
    // Builder
    // =======================================================

    /**
     * The builder class for an {@code EnhancedQueueExecutor}.  All the fields are initialized to sensible defaults for
     * a small thread pool.
     */
    public static final class Builder {
        private ThreadFactory threadFactory = Executors.defaultThreadFactory();
        private Runnable terminationTask = NullRunnable.getInstance();
        private Executor handoffExecutor = DEFAULT_HANDLER;
        private Thread.UncaughtExceptionHandler exceptionHandler = JBossExecutors.loggingExceptionHandler();
        private int coreSize = 16;
        private int maxSize = 64;
        private Duration keepAliveTime = Duration.ofSeconds(30);
        private float growthResistance;
        private boolean allowCoreTimeOut;
        private int maxQueueSize = Integer.MAX_VALUE;
        private boolean registerMBean = REGISTER_MBEAN;
        private String mBeanName;
        private ContextHandler<?> contextHandler = ContextHandler.NONE;

        /**
         * Construct a new instance.
         */
        public Builder() {}

        /**
         * Get the configured thread factory.
         *
         * @return the configured thread factory (not {@code null})
         */
        public ThreadFactory getThreadFactory() {
            return threadFactory;
        }

        /**
         * Set the configured thread factory.
         *
         * @param threadFactory the configured thread factory (must not be {@code null})
         * @return this builder
         */
        public Builder setThreadFactory(final ThreadFactory threadFactory) {
            Assert.checkNotNullParam("threadFactory", threadFactory);
            this.threadFactory = threadFactory;
            return this;
        }

        /**
         * Get the termination task.  By default, an empty {@code Runnable} is used.
         *
         * @return the termination task (not {@code null})
         */
        public Runnable getTerminationTask() {
            return terminationTask;
        }

        /**
         * Set the termination task.
         *
         * @param terminationTask the termination task (must not be {@code null})
         * @return this builder
         */
        public Builder setTerminationTask(final Runnable terminationTask) {
            Assert.checkNotNullParam("terminationTask", terminationTask);
            this.terminationTask = terminationTask;
            return this;
        }

        /**
         * Get the core pool size.  This is the size below which new threads will always be created if no idle threads
         * are available.  If the pool size reaches the core size but has not yet reached the maximum size, a resistance
         * factor will be applied to each task submission which determines whether the task should be queued or a new
         * thread started.
         *
         * @return the core pool size
         * @see EnhancedQueueExecutor#getCorePoolSize()
         */
        public int getCorePoolSize() {
            return coreSize;
        }

        /**
         * Set the core pool size.  If the configured maximum pool size is less than the configured core size, the
         * core size will be reduced to match the maximum size when the thread pool is constructed.
         *
         * @param coreSize the core pool size (must be greater than or equal to 0, and less than 2^20)
         * @return this builder
         * @see EnhancedQueueExecutor#setCorePoolSize(int)
         */
        public Builder setCorePoolSize(final int coreSize) {
            Assert.checkMinimumParameter("coreSize", 0, coreSize);
            Assert.checkMaximumParameter("coreSize", TS_THREAD_CNT_MASK, coreSize);
            this.coreSize = coreSize;
            return this;
        }

        /**
         * Get the maximum pool size.  This is the absolute upper limit to the size of the thread pool.
         *
         * @return the maximum pool size
         * @see EnhancedQueueExecutor#getMaximumPoolSize()
         */
        public int getMaximumPoolSize() {
            return maxSize;
        }

        /**
         * Set the maximum pool size.  If the configured maximum pool size is less than the configured core size, the
         * core size will be reduced to match the maximum size when the thread pool is constructed.
         *
         * @param maxSize the maximum pool size (must be greater than or equal to 0, and less than 2^20)
         * @return this builder
         * @see EnhancedQueueExecutor#setMaximumPoolSize(int)
         */
        public Builder setMaximumPoolSize(final int maxSize) {
            Assert.checkMinimumParameter("maxSize", 0, maxSize);
            Assert.checkMaximumParameter("maxSize", TS_THREAD_CNT_MASK, maxSize);
            this.maxSize = maxSize;
            return this;
        }

        /**
         * Get the thread keep-alive time.  This is the amount of time (in the configured time unit) that idle threads
         * will wait for a task before exiting.
         *
         * @return the thread keep-alive time duration
         */
        public Duration getKeepAliveTime() {
            return keepAliveTime;
        }

        /**
         * Get the thread keep-alive time.  This is the amount of time (in the configured time unit) that idle threads
         * will wait for a task before exiting.
         *
         * @param keepAliveUnits the time keepAliveUnits of the keep-alive time (must not be {@code null})
         * @return the thread keep-alive time
         * @see EnhancedQueueExecutor#getKeepAliveTime(TimeUnit)
         * @deprecated Use {@link #getKeepAliveTime()} instead.
         */
        @Deprecated
        public long getKeepAliveTime(TimeUnit keepAliveUnits) {
            Assert.checkNotNullParam("keepAliveUnits", keepAliveUnits);
            final long secondsPart = keepAliveUnits.convert(keepAliveTime.getSeconds(), TimeUnit.SECONDS);
            final long nanoPart = keepAliveUnits.convert(keepAliveTime.getNano(), TimeUnit.NANOSECONDS);
            final long sum = secondsPart + nanoPart;
            return sum < 0 ? Long.MAX_VALUE : sum;
        }

        /**
         * Set the thread keep-alive time.
         *
         * @param keepAliveTime the thread keep-alive time (must not be {@code null})
         */
        public Builder setKeepAliveTime(final Duration keepAliveTime) {
            Assert.checkNotNullParam("keepAliveTime", keepAliveTime);
            this.keepAliveTime = keepAliveTime;
            return this;
        }

        /**
         * Set the thread keep-alive time.
         *
         * @param keepAliveTime the thread keep-alive time (must be greater than 0)
         * @param keepAliveUnits the time keepAliveUnits of the keep-alive time (must not be {@code null})
         * @return this builder
         * @see EnhancedQueueExecutor#setKeepAliveTime(long, TimeUnit)
         * @deprecated Use {@link #setKeepAliveTime(Duration)} instead.
         */
        @Deprecated
        public Builder setKeepAliveTime(final long keepAliveTime, final TimeUnit keepAliveUnits) {
            Assert.checkMinimumParameter("keepAliveTime", 1L, keepAliveTime);
            Assert.checkNotNullParam("keepAliveUnits", keepAliveUnits);
            this.keepAliveTime = Duration.of(keepAliveTime, JDKSpecific.timeToTemporal(keepAliveUnits));
            return this;
        }

        /**
         * Get the thread pool growth resistance.  This is the average fraction of submitted tasks that will be enqueued (instead
         * of causing a new thread to start) when there are no idle threads and the pool size is equal to or greater than
         * the core size (but still less than the maximum size).  A value of {@code 0.0} indicates that tasks should
         * not be enqueued until the pool is completely full; a value of {@code 1.0} indicates that tasks should always
         * be enqueued until the queue is completely full.
         *
         * @return the thread pool growth resistance
         * @see EnhancedQueueExecutor#getGrowthResistance()
         */
        public float getGrowthResistance() {
            return growthResistance;
        }

        /**
         * Set the thread pool growth resistance.
         *
         * @param growthResistance the thread pool growth resistance (must be in the range {@code 0.0f ≤ n ≤ 1.0f})
         * @return this builder
         * @see #getGrowthResistance()
         * @see EnhancedQueueExecutor#setGrowthResistance(float)
         */
        public Builder setGrowthResistance(final float growthResistance) {
            Assert.checkMinimumParameter("growthResistance", 0.0f, growthResistance);
            Assert.checkMaximumParameter("growthResistance", 1.0f, growthResistance);
            this.growthResistance = growthResistance;
            return this;
        }

        /**
         * Determine whether core threads are allowed to time out.  A "core thread" is defined as any thread in the pool
         * when the pool size is below the pool's {@linkplain #getCorePoolSize() core pool size}.
         *
         * @return {@code true} if core threads are allowed to time out, {@code false} otherwise
         * @see EnhancedQueueExecutor#allowsCoreThreadTimeOut()
         */
        public boolean allowsCoreThreadTimeOut() {
            return allowCoreTimeOut;
        }

        /**
         * Establish whether core threads are allowed to time out.  A "core thread" is defined as any thread in the pool
         * when the pool size is below the pool's {@linkplain #getCorePoolSize() core pool size}.
         *
         * @param allowCoreTimeOut {@code true} if core threads are allowed to time out, {@code false} otherwise
         * @return this builder
         * @see EnhancedQueueExecutor#allowCoreThreadTimeOut(boolean)
         */
        public Builder allowCoreThreadTimeOut(final boolean allowCoreTimeOut) {
            this.allowCoreTimeOut = allowCoreTimeOut;
            return this;
        }

        /**
         * Get the maximum queue size.  If the queue is full and a task cannot be immediately accepted, rejection will result.
         *
         * @return the maximum queue size
         * @see EnhancedQueueExecutor#getMaximumQueueSize()
         */
        public int getMaximumQueueSize() {
            return maxQueueSize;
        }

        /**
         * Set the maximum queue size.
         * This has no impact when {@code jboss.threads.eqe.unlimited-queue} is set.
         *
         * @param maxQueueSize the maximum queue size (must be ≥ 0)
         * @return this builder
         * @see EnhancedQueueExecutor#setMaximumQueueSize(int)
         */
        public Builder setMaximumQueueSize(final int maxQueueSize) {
            Assert.checkMinimumParameter("maxQueueSize", 0, maxQueueSize);
            Assert.checkMaximumParameter("maxQueueSize", Integer.MAX_VALUE, maxQueueSize);
            this.maxQueueSize = maxQueueSize;
            return this;
        }

        /**
         * Get the handoff executor.
         *
         * @return the handoff executor (not {@code null})
         */
        public Executor getHandoffExecutor() {
            return handoffExecutor;
        }

        /**
         * Set the handoff executor.
         *
         * @param handoffExecutor the handoff executor (must not be {@code null})
         * @return this builder
         */
        public Builder setHandoffExecutor(final Executor handoffExecutor) {
            Assert.checkNotNullParam("handoffExecutor", handoffExecutor);
            this.handoffExecutor = handoffExecutor;
            return this;
        }

        /**
         * Get the uncaught exception handler.
         *
         * @return the uncaught exception handler (not {@code null})
         */
        public Thread.UncaughtExceptionHandler getExceptionHandler() {
            return exceptionHandler;
        }

        /**
         * Set the uncaught exception handler.
         *
         * @param exceptionHandler the uncaught exception handler (must not be {@code null})
         * @return this builder
         */
        public Builder setExceptionHandler(final Thread.UncaughtExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        /**
         * Construct the executor from the configured parameters.
         *
         * @return the executor, which will be active and ready to accept tasks (not {@code null})
         */
        public EnhancedQueueExecutor build() {
            return new EnhancedQueueExecutor(this);
        }

        /**
         * Determine whether an MBean should automatically be registered for this pool.
         *
         * @return {@code true} if an MBean is to be auto-registered; {@code false} otherwise
         */
        public boolean isRegisterMBean() {
            return registerMBean;
        }

        /**
         * Establish whether an MBean should automatically be registered for this pool.
         *
         * @param registerMBean {@code true} if an MBean is to be auto-registered; {@code false} otherwise
         * @return this builder
         */
        public Builder setRegisterMBean(final boolean registerMBean) {
            this.registerMBean = registerMBean;
            return this;
        }

        /**
         * Get the overridden MBean name.
         *
         * @return the overridden MBean name, or {@code null} if a default name should be generated
         */
        public String getMBeanName() {
            return mBeanName;
        }

        /**
         * Set the overridden MBean name.
         *
         * @param mBeanName the overridden MBean name, or {@code null} if a default name should be generated
         * @return this builder
         */
        public Builder setMBeanName(final String mBeanName) {
            this.mBeanName = mBeanName;
            return this;
        }

        /**
         * Get the context handler for the user-defined context.
         *
         * @return the context handler for the user-defined context (not {@code null})
         */
        public ContextHandler<?> getContextHandler() {
            return contextHandler;
        }

        /**
         * Set the context handler for the user-defined context.
         *
         * @param contextHandler the context handler for the user-defined context
         * @return this builder
         */
        public Builder setContextHandler(final ContextHandler<?> contextHandler) {
            Assert.checkNotNullParam("contextHandler", contextHandler);
            this.contextHandler = contextHandler;
            return this;
        }
    }

    // =======================================================
    // ExecutorService
    // =======================================================

    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    public MXBeanImpl getMxBean() {
        return mxBean;
    }

    public Runnable getTerminationTask() {
        return terminationTask;
    }

    public String getMBeanName() {
        return mBeanName;
    }

    /**
     * Execute a task.
     *
     * @param runnable the task to execute (must not be {@code null})
     */
    public void execute(Runnable runnable) {
        Assert.checkNotNullParam("runnable", runnable);
        final Task realRunnable = new Task(runnable, contextHandler.captureContext());
        int result;
        result = tryExecute(realRunnable);
        boolean ok = false;
        if (result == EXE_OK) {
            // last check to ensure that there is at least one existent thread to avoid rare thread timeout race condition
            if (currentSizeOf(threadStatus) == 0 && tryAllocateThread(0.0f) == AT_YES && ! doStartThread(null)) {
                deallocateThread();
            }
            if (UPDATE_STATISTICS) submittedTaskCounter.increment();
            return;
        } else if (result == EXE_CREATE_THREAD) try {
            ok = doStartThread(realRunnable);
        } finally {
            if (! ok) deallocateThread();
        } else {
            if (UPDATE_STATISTICS) rejectedTaskCounter.increment();
            if (result == EXE_REJECT_SHUTDOWN) {
                rejectShutdown(realRunnable);
            } else {
                assert result == EXE_REJECT_QUEUE_FULL;
                rejectQueueFull(realRunnable);
            }
        }
    }

    /**
     * Request that shutdown be initiated for this thread pool.  This is equivalent to calling
     * {@link #shutdown(boolean) shutdown(false)}; see that method for more information.
     */
    public void shutdown() {
        shutdown(false);
    }

    /**
     * Attempt to stop the thread pool immediately by interrupting all running threads and de-queueing all pending
     * tasks.  The thread pool might not be fully stopped when this method returns, if a currently running task
     * does not respect interruption.
     *
     * @return a list of pending tasks (not {@code null})
     */
    public List<Runnable> shutdownNow() {
        shutdown(true);
        final ArrayList<Runnable> list = new ArrayList<>();
        TaskNode head = this.head;
        QNode headNext;
        for (;;) {
            headNext = head.getNext();
            if (headNext == head) {
                // a racing consumer has already consumed it (and moved head)
                head = this.head;
                continue;
            }
            if (headNext instanceof TaskNode) {
                TaskNode taskNode = (TaskNode) headNext;
                if (compareAndSetHead(head, taskNode)) {
                    // save from GC nepotism
                    head.setNextOrdered(head);
                    if (! NO_QUEUE_LIMIT) decreaseQueueSize();
                    head = taskNode;
                    list.add(taskNode.task.handoff());
                }
                // retry
            } else {
                // no more tasks;
                return list;
            }
        }
    }

    /**
     * Determine whether shutdown was requested on this thread pool.
     *
     * @return {@code true} if shutdown was requested, {@code false} otherwise
     */
    public boolean isShutdown() {
        return isShutdownRequested(threadStatus);
    }

    /**
     * Determine whether shutdown has completed on this thread pool.
     *
     * @return {@code true} if shutdown has completed, {@code false} otherwise
     */
    public boolean isTerminated() {
        return isShutdownComplete(threadStatus);
    }

    /**
     * Wait for the thread pool to complete termination.  If the timeout expires before the thread pool is terminated,
     * {@code false} is returned.  If the calling thread is interrupted before the thread pool is terminated, then
     * an {@link InterruptedException} is thrown.
     *
     * @param timeout the amount of time to wait (must be ≥ 0)
     * @param unit the unit of time to use for waiting (must not be {@code null})
     * @return {@code true} if the thread pool terminated within the given timeout, {@code false} otherwise
     * @throws InterruptedException if the calling thread was interrupted before either the time period elapsed or the pool terminated successfully
     */
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        Assert.checkMinimumParameter("timeout", 0, timeout);
        Assert.checkNotNullParam("unit", unit);
        if (timeout > 0) {
            final Thread thread = Thread.currentThread();
            if (runningThreads.contains(thread) || thread == schedulerThread) {
                throw Messages.msg.cannotAwaitWithin();
            }
            Waiter waiters = this.terminationWaiters;
            if (waiters == TERMINATE_COMPLETE_WAITER) {
                return true;
            }
            final Waiter waiter = new Waiter(waiters);
            waiter.setThread(currentThread());
            while (! compareAndSetTerminationWaiters(waiters, waiter)) {
                waiters = this.terminationWaiters;
                if (waiters == TERMINATE_COMPLETE_WAITER) {
                    return true;
                }
                waiter.setNext(waiters);
            }
            try {
                parkNanos(this, unit.toNanos(timeout));
            } finally {
                // prevent future spurious unparks without sabotaging the queue's integrity
                waiter.setThread(null);
            }
        }
        if (Thread.interrupted()) throw new InterruptedException();
        return isTerminated();
    }

    // =======================================================
    // ScheduledExecutorService
    // =======================================================

    public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        startScheduleThread();
        return schedulerTask.schedule(new RunnableScheduledFuture(command, delay, unit));
    }

    public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
        startScheduleThread();
        return schedulerTask.schedule(new CallableScheduledFuture<V>(callable, delay, unit));
    }

    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        startScheduleThread();
        return schedulerTask.schedule(new FixedRateRunnableScheduledFuture(command, initialDelay, period, unit));
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        startScheduleThread();
        return schedulerTask.schedule(new FixedDelayRunnableScheduledFuture(command, initialDelay, delay, unit));
    }

    private void startScheduleThread() {
        // this should be fairly quick...
        if (schedulerThread.getState() == Thread.State.NEW) try {
            schedulerThread.start();
        } catch (IllegalThreadStateException ignored) {
            // make sure it's race-proof
        }
    }

    // =======================================================
    // Management
    // =======================================================

    public StandardThreadPoolMXBean getThreadPoolMXBean() {
        return mxBean;
    }

    // =======================================================
    // Other public API
    // =======================================================

    /**
     * Initiate shutdown of this thread pool.  After this method is called, no new tasks will be accepted.  Once
     * the last task is complete, the thread pool will be terminated and its
     * {@linkplain Builder#setTerminationTask(Runnable) termination task}
     * will be called.  Calling this method more than once has no additional effect, unless all previous calls
     * had the {@code interrupt} parameter set to {@code false} and the subsequent call sets it to {@code true}, in
     * which case all threads in the pool will be interrupted.
     *
     * @param interrupt {@code true} to request that currently-running tasks be interrupted; {@code false} otherwise
     */
    public void shutdown(boolean interrupt) {
        long oldStatus, newStatus;
        // state change sh1:
        //   threadStatus ← threadStatus | shutdown
        // succeeds: -
        // preconditions:
        //   none (thread status may be in any state)
        // postconditions (succeed):
        //   (always) ShutdownRequested set in threadStatus
        //   (maybe) ShutdownInterrupted set in threadStatus (depends on parameter)
        //   (maybe) ShutdownComplete set in threadStatus (if currentSizeOf() == 0)
        //   (maybe) exit if no change
        // postconditions (fail): -
        // post-actions (fail):
        //   repeat state change until success or return
        do {
            oldStatus = threadStatus;
            newStatus = withShutdownRequested(oldStatus);
            if (interrupt) newStatus = withShutdownInterrupt(newStatus);
            if (currentSizeOf(oldStatus) == 0) newStatus = withShutdownComplete(newStatus);
            if (newStatus == oldStatus) return;
        } while (! compareAndSetThreadStatus(oldStatus, newStatus));
        assert oldStatus != newStatus;
        if (isShutdownRequested(newStatus) != isShutdownRequested(oldStatus)) {
            assert ! isShutdownRequested(oldStatus); // because it can only ever be set, not cleared
            // we initiated shutdown
            // terminate the scheduler
            schedulerTask.shutdown();
            // clear out all consumers and append a dummy waiter node
            TaskNode tail = this.tail;
            QNode tailNext;
            // a marker to indicate that termination was requested
            for (;;) {
                tailNext = tail.getNext();
                if (tailNext instanceof TaskNode) {
                    tail = (TaskNode) tailNext;
                } else if (tailNext instanceof PoolThreadNode || tailNext == null) {
                    // remove the entire chain from this point
                    PoolThreadNode node = (PoolThreadNode) tailNext;
                    // state change sh2:
                    //   tail.next ← terminateNode(null)
                    // succeeds: sh1
                    // preconditions:
                    //   threadStatus contains shutdown
                    //   tail(snapshot) is a task node
                    //   tail(snapshot).next is a (list of) pool thread node(s) or null
                    // postconditions (succeed):
                    //   tail(snapshot).next is TERMINATE_REQUESTED
                    if (tail.compareAndSetNext(node, TERMINATE_REQUESTED)) {
                        // got it!
                        // state change sh3:
                        //   node.task ← EXIT
                        // preconditions:
                        //   none (node task may be in any state)
                        // postconditions (succeed):
                        //   task is EXIT
                        // postconditions (fail):
                        //   no change (repeat loop)
                        while (node != null) {
                            node.compareAndSetTask(WAITING, EXIT);
                            node.unpark();
                            node = node.getNext();
                        }
                        // success; exit loop
                        break;
                    }
                    // repeat loop (failed CAS)
                } else if (tailNext instanceof TerminateWaiterNode) {
                    // theoretically impossible, but it means we have nothing else to do
                    break;
                } else {
                    throw Assert.unreachableCode();
                }
            }
        }
        if (isShutdownInterrupt(newStatus) != isShutdownInterrupt(oldStatus)) {
            assert ! isShutdownInterrupt(oldStatus); // because it can only ever be set, not cleared
            // we initiated interrupt, so interrupt all currently active threads
            for (Thread thread : runningThreads) {
                thread.interrupt();
            }
        }
        if (isShutdownComplete(newStatus) != isShutdownComplete(oldStatus)) {
            assert ! isShutdownComplete(oldStatus);  // because it can only ever be set, not cleared
            completeTermination();
        }
    }

    /**
     * Determine if this thread pool is in the process of terminating but has not yet completed.
     *
     * @return {@code true} if the thread pool is terminating, or {@code false} if the thread pool is not terminating or has completed termination
     */
    public boolean isTerminating() {
        final long threadStatus = this.threadStatus;
        return isShutdownRequested(threadStatus) && ! isShutdownComplete(threadStatus);
    }

    /**
     * Start an idle core thread.  Normally core threads are only begun when a task was submitted to the executor
     * but no thread is immediately available to handle the task.
     *
     * @return {@code true} if a core thread was started, or {@code false} if all core threads were already running or
     *   if the thread failed to be created for some other reason
     */
    public boolean prestartCoreThread() {
        if (tryAllocateThread(1.0f) != AT_YES) return false;
        if (doStartThread(null)) return true;
        deallocateThread();
        return false;
    }

    /**
     * Start all core threads.  Calls {@link #prestartCoreThread()} in a loop until it returns {@code false}.
     *
     * @return the number of core threads created
     */
    public int prestartAllCoreThreads() {
        int cnt = 0;
        while (prestartCoreThread()) cnt ++;
        return cnt;
    }

    // =======================================================
    // Tuning & configuration parameters (run time)
    // =======================================================

    /**
     * Get the thread pool growth resistance.  This is the average fraction of submitted tasks that will be enqueued (instead
     * of causing a new thread to start) when there are no idle threads and the pool size is equal to or greater than
     * the core size (but still less than the maximum size).  A value of {@code 0.0} indicates that tasks should
     * not be enqueued until the pool is completely full; a value of {@code 1.0} indicates that tasks should always
     * be enqueued until the queue is completely full.
     *
     * @return the configured growth resistance factor
     * @see Builder#getGrowthResistance() Builder.getGrowthResistance()
     */
    public float getGrowthResistance() {
        return growthResistance;
    }

    /**
     * Set the growth resistance factor.
     *
     * @param growthResistance the thread pool growth resistance (must be in the range {@code 0.0f ≤ n ≤ 1.0f})
     * @see Builder#setGrowthResistance(float) Builder.setGrowthResistance()
     */
    public void setGrowthResistance(final float growthResistance) {
        Assert.checkMinimumParameter("growthResistance", 0.0f, growthResistance);
        Assert.checkMaximumParameter("growthResistance", 1.0f, growthResistance);
        this.growthResistance = growthResistance;
    }

    /**
     * Get the core pool size.  This is the size below which new threads will always be created if no idle threads
     * are available.  If the pool size reaches the core size but has not yet reached the maximum size, a resistance
     * factor will be applied to each task submission which determines whether the task should be queued or a new
     * thread started.
     *
     * @return the core pool size
     * @see Builder#getCorePoolSize() Builder.getCorePoolSize()
     */
    public int getCorePoolSize() {
        return coreSizeOf(threadStatus);
    }

    /**
     * Set the core pool size.  If the configured maximum pool size is less than the configured core size, the
     * core size will be reduced to match the maximum size when the thread pool is constructed.
     *
     * @param corePoolSize the core pool size (must be greater than or equal to 0, and less than 2^20)
     * @see Builder#setCorePoolSize(int) Builder.setCorePoolSize()
     */
    public void setCorePoolSize(final int corePoolSize) {
        Assert.checkMinimumParameter("corePoolSize", 0, corePoolSize);
        Assert.checkMaximumParameter("corePoolSize", TS_THREAD_CNT_MASK, corePoolSize);
        long oldVal, newVal;
        do {
            oldVal = threadStatus;
            if (corePoolSize > maxSizeOf(oldVal)) {
                // automatically bump up max size to match
                newVal = withCoreSize(withMaxSize(oldVal, corePoolSize), corePoolSize);
            } else {
                newVal = withCoreSize(oldVal, corePoolSize);
            }
        } while (! compareAndSetThreadStatus(oldVal, newVal));
        if (maxSizeOf(newVal) < maxSizeOf(oldVal) || coreSizeOf(newVal) < coreSizeOf(oldVal)) {
            // poke all the threads to try to terminate any excess eagerly
            for (Thread activeThread : runningThreads) {
                unpark(activeThread);
            }
        }
    }

    /**
     * Get the maximum pool size.  This is the absolute upper limit to the size of the thread pool.
     *
     * @return the maximum pool size
     * @see Builder#getMaximumPoolSize() Builder.getMaximumPoolSize()
     */
    public int getMaximumPoolSize() {
        return maxSizeOf(threadStatus);
    }

    /**
     * Set the maximum pool size.  If the configured maximum pool size is less than the configured core size, the
     * core size will be reduced to match the maximum size when the thread pool is constructed.
     *
     * @param maxPoolSize the maximum pool size (must be greater than or equal to 0, and less than 2^20)
     * @see Builder#setMaximumPoolSize(int) Builder.setMaximumPoolSize()
     */
    public void setMaximumPoolSize(final int maxPoolSize) {
        Assert.checkMinimumParameter("maxPoolSize", 0, maxPoolSize);
        Assert.checkMaximumParameter("maxPoolSize", TS_THREAD_CNT_MASK, maxPoolSize);
        long oldVal, newVal;
        do {
            oldVal = threadStatus;
            if (maxPoolSize < coreSizeOf(oldVal)) {
                // automatically bump down core size to match
                newVal = withCoreSize(withMaxSize(oldVal, maxPoolSize), maxPoolSize);
            } else {
                newVal = withMaxSize(oldVal, maxPoolSize);
            }
        } while (! compareAndSetThreadStatus(oldVal, newVal));
        if (maxSizeOf(newVal) < maxSizeOf(oldVal) || coreSizeOf(newVal) < coreSizeOf(oldVal)) {
            // poke all the threads to try to terminate any excess eagerly
            for (Thread activeThread : runningThreads) {
                unpark(activeThread);
            }
        }
    }

    /**
     * Determine whether core threads are allowed to time out.  A "core thread" is defined as any thread in the pool
     * when the pool size is below the pool's {@linkplain #getCorePoolSize() core pool size}.
     *
     * @return {@code true} if core threads are allowed to time out, {@code false} otherwise
     * @see Builder#allowsCoreThreadTimeOut() Builder.allowsCoreThreadTimeOut()
     */
    public boolean allowsCoreThreadTimeOut() {
        return isAllowCoreTimeout(threadStatus);
    }

    /**
     * Establish whether core threads are allowed to time out.  A "core thread" is defined as any thread in the pool
     * when the pool size is below the pool's {@linkplain #getCorePoolSize() core pool size}.
     *
     * @param value {@code true} if core threads are allowed to time out, {@code false} otherwise
     * @see Builder#allowCoreThreadTimeOut(boolean) Builder.allowCoreThreadTimeOut()
     */
    public void allowCoreThreadTimeOut(boolean value) {
        long oldVal, newVal;
        do {
            oldVal = threadStatus;
            newVal = withAllowCoreTimeout(oldVal, value);
            if (oldVal == newVal) return;
        } while (! compareAndSetThreadStatus(oldVal, newVal));
        if (value) {
            // poke all the threads to try to terminate any excess eagerly
            for (Thread activeThread : runningThreads) {
                unpark(activeThread);
            }
        }
    }

    /**
     * Get the thread keep-alive time.  This is the minimum length of time that idle threads should remain until they exit.
     * Unless core threads are allowed to time out, threads will only exit if the current thread count exceeds the core
     * limit.
     *
     * @param keepAliveUnits the unit in which the result should be expressed (must not be {@code null})
     * @return the amount of time (will be greater than zero)
     * @see Builder#getKeepAliveTime(TimeUnit) Builder.getKeepAliveTime()
     * @deprecated Use {@link #getKeepAliveTime()} instead.
     */
    @Deprecated
    public long getKeepAliveTime(TimeUnit keepAliveUnits) {
        Assert.checkNotNullParam("keepAliveUnits", keepAliveUnits);
        return keepAliveUnits.convert(timeoutNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Get the thread keep-alive time.  This is the minimum length of time that idle threads should remain until they exit.
     * Unless core threads are allowed to time out, threads will only exit if the current thread count exceeds the core
     * limit.
     *
     * @return the amount of time (will be greater than zero)
     * @see Builder#getKeepAliveTime() Builder.getKeepAliveTime()
     */
    public Duration getKeepAliveTime() {
        return Duration.of(timeoutNanos, ChronoUnit.NANOS);
    }

    /**
     * Set the thread keep-alive time.  This is the minimum length of time that idle threads should remain until they exit.
     * Unless core threads are allowed to time out, threads will only exit if the current thread count exceeds the core
     * limit.
     *
     * @param keepAliveTime the thread keep-alive time (must be &gt; 0)
     * @param keepAliveUnits the unit in which the value is expressed (must not be {@code null})
     * @see Builder#setKeepAliveTime(long, TimeUnit) Builder.setKeepAliveTime()
     * @deprecated Use {@link #setKeepAliveTime(Duration)} instead.
     */
    @Deprecated
    public void setKeepAliveTime(final long keepAliveTime, final TimeUnit keepAliveUnits) {
        Assert.checkMinimumParameter("keepAliveTime", 1L, keepAliveTime);
        Assert.checkNotNullParam("keepAliveUnits", keepAliveUnits);
        timeoutNanos = max(1L, keepAliveUnits.toNanos(keepAliveTime));
    }

    /**
     * Set the thread keep-alive time.  This is the minimum length of time that idle threads should remain until they exit.
     * Unless core threads are allowed to time out, threads will only exit if the current thread count exceeds the core
     * limit.
     *
     * @param keepAliveTime the thread keep-alive time (must not be {@code null})
     * @see Builder#setKeepAliveTime(Duration) Builder.setKeepAliveTime()
     */
    public void setKeepAliveTime(final Duration keepAliveTime) {
        Assert.checkNotNullParam("keepAliveTime", keepAliveTime);
        timeoutNanos = TimeUtil.clampedPositiveNanos(keepAliveTime);
    }

    /**
     * Get the maximum queue size.  If the queue is full and a task cannot be immediately accepted, rejection will result.
     *
     * @return the maximum queue size
     * @see Builder#getMaximumQueueSize() Builder.getMaximumQueueSize()
     */
    public int getMaximumQueueSize() {
        return maxQueueSizeOf(queueSize);
    }

    /**
     * Set the maximum queue size.  If the new maximum queue size is smaller than the current queue size, there is no
     * effect other than preventing tasks from being enqueued until the size decreases below the maximum again.
     *
     * @param maxQueueSize the maximum queue size (must be ≥ 0)
     * @see Builder#setMaximumQueueSize(int) Builder.setMaximumQueueSize()
     */
    public void setMaximumQueueSize(final int maxQueueSize) {
        Assert.checkMinimumParameter("maxQueueSize", 0, maxQueueSize);
        Assert.checkMaximumParameter("maxQueueSize", Integer.MAX_VALUE, maxQueueSize);
        if (NO_QUEUE_LIMIT) return;
        long oldVal;
        do {
            oldVal = queueSize;
        } while (! compareAndSetQueueSize(oldVal, withMaxQueueSize(oldVal, maxQueueSize)));
    }

    /**
     * Get the executor to delegate to in the event of task rejection.
     *
     * @return the executor to delegate to in the event of task rejection (not {@code null})
     */
    public Executor getHandoffExecutor() {
        return handoffExecutor;
    }

    /**
     * Set the executor to delegate to in the event of task rejection.
     *
     * @param handoffExecutor the executor to delegate to in the event of task rejection (must not be {@code null})
     */
    public void setHandoffExecutor(final Executor handoffExecutor) {
        Assert.checkNotNullParam("handoffExecutor", handoffExecutor);
        this.handoffExecutor = handoffExecutor;
    }

    /**
     * Get the exception handler to use for uncaught exceptions.
     *
     * @return the exception handler to use for uncaught exceptions (not {@code null})
     */
    public Thread.UncaughtExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Set the exception handler to use for uncaught exceptions.
     *
     * @param exceptionHandler the exception handler to use for uncaught exceptions (must not be {@code null})
     */
    public void setExceptionHandler(final Thread.UncaughtExceptionHandler exceptionHandler) {
        Assert.checkNotNullParam("exceptionHandler", exceptionHandler);
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Set the termination task, overwriting any previous setting.
     *
     * @param terminationTask the termination task, or {@code null} to perform no termination task
     */
    public void setTerminationTask(final Runnable terminationTask) {
        this.terminationTask = terminationTask;
    }

    // =======================================================
    // Statistics & metrics API
    // =======================================================

    /**
     * Get an estimate of the current queue size.
     *
     * @return an estimate of the current queue size or -1 when {@code jboss.threads.eqe.unlimited-queue} is enabled
     */
    public int getQueueSize() {
        return NO_QUEUE_LIMIT ? -1 : currentQueueSizeOf(queueSize);
    }

    /**
     * Get an estimate of the peak number of threads that the pool has ever held.
     *
     * @return an estimate of the peak number of threads that the pool has ever held
     */
    public int getLargestPoolSize() {
        return UPDATE_STATISTICS ? peakThreadCount : -1;
    }

    /**
     * Get an estimate of the number of threads which are currently doing work on behalf of the thread pool.
     *
     * @return the active count estimate or -1 when {@code jboss.threads.eqe.statistics.active-count} is disabled
     */
    public int getActiveCount() {
        return UPDATE_ACTIVE_COUNT ? activeCount : -1;
    }

    /**
     * Get an estimate of the peak size of the queue.
     *
     * return an estimate of the peak size of the queue or -1 when {@code jboss.threads.eqe.statistics}
     * is disabled or {@code jboss.threads.eqe.unlimited-queue} is enabled
     */
    public int getLargestQueueSize() {
        return UPDATE_STATISTICS && !NO_QUEUE_LIMIT ? peakQueueSize : -1;
    }

    /**
     * Get an estimate of the total number of tasks ever submitted to this thread pool.
     *
     * @return an estimate of the total number of tasks ever submitted to this thread pool
     * or -1 when {@code jboss.threads.eqe.statistics} is disabled
     */
    public long getSubmittedTaskCount() {
        return UPDATE_STATISTICS ? submittedTaskCounter.longValue() : -1;
    }

    /**
     * Get an estimate of the total number of tasks ever rejected by this thread pool for any reason.
     *
     * @return an estimate of the total number of tasks ever rejected by this thread pool
     * or -1 when {@code jboss.threads.eqe.statistics} is disabled
     */
    public long getRejectedTaskCount() {
        return UPDATE_STATISTICS ? rejectedTaskCounter.longValue() : -1;
    }

    /**
     * Get an estimate of the number of tasks completed by this thread pool.
     *
     * @return an estimate of the number of tasks completed by this thread pool
     * or -1 when {@code jboss.threads.eqe.statistics} is disabled
     */
    public long getCompletedTaskCount() {
        return UPDATE_STATISTICS ? completedTaskCounter.longValue() : -1;
    }

    /**
     * Get an estimate of the current number of active threads in the pool.
     *
     * @return an estimate of the current number of active threads in the pool
     */
    public int getPoolSize() {
        return currentSizeOf(threadStatus);
    }

    /**
     * Get an array containing an approximate snapshot of the currently running threads in
     * this executor.
     *
     * @return an array of running (unterminated) threads (not {@code null})
     */
    public Thread[] getRunningThreads() {
        return runningThreads.toArray(NO_THREADS);
    }

    static class MBeanUnregisterAction implements PrivilegedAction<Void> {
        static void forceInit() {
        }

        private final Object handle;

        MBeanUnregisterAction(final Object handle) {
            this.handle = handle;
        }

        public Void run() {
            try {
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(((ObjectInstance) handle).getObjectName());
            } catch (Throwable ignored) {
            }
            return null;
        }
    }

    // =======================================================
    // Pooled thread body
    // =======================================================

    final class ThreadBody implements Runnable {
        private Task initialTask;

        ThreadBody(final Task initialTask) {
            this.initialTask = initialTask;
        }

        /**
         * Execute the body of the thread.  On entry the thread is added to the {@link #runningThreads} set, and on
         * exit it is removed.
         */
        public void run() {
            final Thread currentThread = Thread.currentThread();
            final LongAdder spinMisses = EnhancedQueueExecutor.this.spinMisses;
            runningThreads.add(currentThread);

            // run the initial task
            nullToNop(getAndClearInitialTask()).run();

            // Eagerly allocate a PoolThreadNode for the next time it's needed
            PoolThreadNode nextPoolThreadNode = new PoolThreadNode(currentThread);
            // main loop
            QNode node;
            processingQueue: for (;;) {
                node = getOrAddNode(nextPoolThreadNode);
                if (node instanceof TaskNode) {
                    // task node was removed
                    ((TaskNode) node).getAndClearTask().run();
                    continue;
                } else if (node == nextPoolThreadNode) {
                    // pool thread node was added
                    final PoolThreadNode newNode = nextPoolThreadNode;
                    // nextPoolThreadNode has been added to the queue, a new node is required for next time.
                    nextPoolThreadNode = new PoolThreadNode(currentThread);
                    // at this point, we are registered into the queue
                    long start = System.nanoTime();
                    long elapsed = 0L;
                    waitingForTask: for (;;) {
                        Runnable task = newNode.getTask();
                        assert task != ACCEPTED && task != GAVE_UP && task != null;
                        if (task != WAITING && task != EXIT) {
                            if (newNode.compareAndSetTask(task, ACCEPTED)) {
                                // we have a task to run, so run it and then abandon the node
                                task.run();
                                // rerun outer
                                continue processingQueue;
                            }
                            // we had a task to run, but we failed to CAS it for some reason, so retry
                            if (UPDATE_STATISTICS) spinMisses.increment();
                            continue waitingForTask;
                        } else {
                            final long timeoutNanos = EnhancedQueueExecutor.this.timeoutNanos;
                            long oldVal = threadStatus;
                            if (elapsed >= timeoutNanos || task == EXIT || currentSizeOf(oldVal) > maxSizeOf(oldVal)) {
                                // try to exit this thread, if we are allowed
                                if (task == EXIT ||
                                        isShutdownRequested(oldVal) ||
                                        isAllowCoreTimeout(oldVal) ||
                                        currentSizeOf(oldVal) > coreSizeOf(oldVal)
                                ) {
                                    if (newNode.compareAndSetTask(task, GAVE_UP)) {
                                        for (;;) {
                                            if (tryDeallocateThread(oldVal)) {
                                                // clear to exit.
                                                runningThreads.remove(currentThread);
                                                return;
                                            }
                                            if (UPDATE_STATISTICS) spinMisses.increment();
                                            oldVal = threadStatus;
                                        }
                                        //throw Assert.unreachableCode();
                                    }
                                    continue waitingForTask;
                                } else {
                                    if (elapsed >= timeoutNanos) {
                                        newNode.park(EnhancedQueueExecutor.this);
                                    } else {
                                        newNode.park(EnhancedQueueExecutor.this, timeoutNanos - elapsed);
                                    }
                                    Thread.interrupted();
                                    elapsed = System.nanoTime() - start;
                                    // retry inner
                                    continue waitingForTask;
                                }
                                //throw Assert.unreachableCode();
                            } else {
                                assert task == WAITING;
                                newNode.park(EnhancedQueueExecutor.this, timeoutNanos - elapsed);
                                Thread.interrupted();
                                elapsed = System.nanoTime() - start;
                                // retry inner
                                continue waitingForTask;
                            }
                            //throw Assert.unreachableCode();
                        }
                        //throw Assert.unreachableCode();
                    } // :waitingForTask
                    //throw Assert.unreachableCode();
                } else {
                    assert node instanceof TerminateWaiterNode;
                    // we're shutting down!
                    runningThreads.remove(currentThread);
                    deallocateThread();
                    return;
                }
                //throw Assert.unreachableCode();
            } // :processingQueue
            //throw Assert.unreachableCode();
        }

        private QNode getOrAddNode(PoolThreadNode nextPoolThreadNode) {
            TaskNode head;
            QNode headNext;
            for (;;) {
                head = EnhancedQueueExecutor.this.head;
                headNext = head.getNext();
                // headNext == head can happen if another consumer has already consumed head:
                // retry with a fresh head
                if (headNext != head) {
                    if (headNext instanceof TaskNode) {
                        TaskNode taskNode = (TaskNode) headNext;
                        if (compareAndSetHead(head, taskNode)) {
                            // save from GC Nepotism: generational GCs don't like
                            // cross-generational references, so better to "clean-up" head::next
                            // to save dragging head::next into the old generation.
                            // Clean-up cannot just null out next
                            head.setNextOrdered(head);
                            if (!NO_QUEUE_LIMIT) decreaseQueueSize();
                            return taskNode;
                        }
                    } else if (headNext instanceof PoolThreadNode || headNext == null) {
                        nextPoolThreadNode.setNextRelaxed(headNext);
                        if (head.compareAndSetNext(headNext, nextPoolThreadNode)) {
                            return nextPoolThreadNode;
                        } else if (headNext != null) {
                            // GC Nepotism:
                            // save dragging headNext into old generation
                            // (although being a PoolThreadNode it won't make a big difference)
                            nextPoolThreadNode.setNextRelaxed(null);
                        }
                    } else {
                        assert headNext instanceof TerminateWaiterNode;
                        return headNext;
                    }
                }
                if (UPDATE_STATISTICS) spinMisses.increment();
            }
        }

        private Runnable getAndClearInitialTask() {
            Runnable initial = initialTask;
            this.initialTask = null;
            return initial;
        }
    }

    // =======================================================
    // Thread starting
    // =======================================================

    /**
     * Allocate a new thread.
     *
     * @param growthResistance the growth resistance to apply
     * @return {@code AT_YES} if a thread is allocated; {@code AT_NO} if a thread was not allocated; {@code AT_SHUTDOWN} if the pool is being shut down
     */
    int tryAllocateThread(final float growthResistance) {
        int oldSize;
        long oldStat;
        for (;;) {
            oldStat = threadStatus;
            if (isShutdownRequested(oldStat)) {
                return AT_SHUTDOWN;
            }
            oldSize = currentSizeOf(oldStat);
            if (oldSize >= maxSizeOf(oldStat)) {
                // max threads already reached
                return AT_NO;
            }
            if (oldSize >= coreSizeOf(oldStat) && oldSize > 0) {
                // core threads are reached; check resistance factor (if no threads are running, then always start a thread)
                if (growthResistance != 0.0f && (growthResistance == 1.0f || ThreadLocalRandom.current().nextFloat() < growthResistance)) {
                    // do not create a thread this time
                    return AT_NO;
                }
            }
            // try to increase
            final int newSize = oldSize + 1;
            // state change ex3:
            //   threadStatus.size ← threadStatus(snapshot).size + 1
            // cannot succeed: sh1
            // succeeds: -
            // preconditions:
            //   ! threadStatus(snapshot).shutdownRequested
            //   threadStatus(snapshot).size < threadStatus(snapshot).maxSize
            //   threadStatus(snapshot).size < threadStatus(snapshot).coreSize || random < growthResistance
            // post-actions (fail):
            //   retry whole loop
            if (compareAndSetThreadStatus(oldStat, withCurrentSize(oldStat, newSize))) {
                // increment peak thread count
                if (UPDATE_STATISTICS) {
                    int oldVal;
                    do {
                        oldVal = peakThreadCount;
                        if (oldVal >= newSize) break;
                    } while (! compareAndSetPeakThreadCount(oldVal, newSize));
                }
                return AT_YES;
            }
            if (UPDATE_STATISTICS) spinMisses.increment();
        }
    }

    /**
     * Roll back a thread allocation, possibly terminating the pool.  Only call after {@link #tryAllocateThread(float)} returns {@link #AT_YES}.
     */
    void deallocateThread() {
        long oldStat;
        do {
            oldStat = threadStatus;
        } while (! tryDeallocateThread(oldStat));
    }

    /**
     * Try to roll back a thread allocation, possibly running the termination task if the pool would be terminated
     * by last thread exit.
     *
     * @param oldStat the {@code threadStatus} to CAS
     * @return {@code true} if the thread was deallocated, or {@code false} to retry with a new {@code oldStat}
     */
    boolean tryDeallocateThread(long oldStat) {
        // roll back our thread allocation attempt
        // state change ex4:
        //   threadStatus.size ← threadStatus.size - 1
        // succeeds: ex3
        // preconditions:
        //   threadStatus.size > 0
        long newStat = withCurrentSize(oldStat, currentSizeOf(oldStat) - 1);
        if (currentSizeOf(newStat) == 0 && isShutdownRequested(oldStat)) {
            newStat = withShutdownComplete(newStat);
        }
        if (! compareAndSetThreadStatus(oldStat, newStat)) return false;
        if (isShutdownComplete(newStat)) {
            completeTermination();
        }
        return true;
    }

    /**
     * Start an allocated thread.
     *
     * @param runnable the task or {@code null}
     * @return {@code true} if the thread was started, {@code false} otherwise
     * @throws RejectedExecutionException if {@code runnable} is not {@code null} and the thread could not be created or started
     */
    boolean doStartThread(Task runnable) throws RejectedExecutionException {
        Thread thread;
        try {
            thread = threadFactory.newThread(new ThreadBody(runnable));
        } catch (Throwable t) {
            if (runnable != null) {
                if (UPDATE_STATISTICS) rejectedTaskCounter.increment();
                rejectException(runnable, t);
            }
            return false;
        }
        if (thread == null) {
            if (runnable != null) {
                if (UPDATE_STATISTICS) rejectedTaskCounter.increment();
                rejectNoThread(runnable);
            }
            return false;
        }
        try {
            thread.start();
        } catch (Throwable t) {
            if (runnable != null) {
                if (UPDATE_STATISTICS) rejectedTaskCounter.increment();
                rejectException(runnable, t);
            }
            return false;
        }
        return true;
    }

    // =======================================================
    // Task submission
    // =======================================================

    private int tryExecute(final Task runnable) {
        QNode tailNext;
        TaskNode tail = this.tail;
        TaskNode node = null;
        for (;;) {
            tailNext = tail.getNext();
            if (tailNext == tail) {
                // tail is already consumed, retry with new tail(snapshot)
            } else if (tailNext == null) {
                // no consumers available; maybe we can start one
                int tr = tryAllocateThread(growthResistance);
                if (tr == AT_YES) {
                    return EXE_CREATE_THREAD;
                }
                if (tr == AT_SHUTDOWN) {
                    return EXE_REJECT_SHUTDOWN;
                }
                assert tr == AT_NO;
                // no; try to enqueue
                if (!NO_QUEUE_LIMIT && !increaseQueueSize()) {
                    // queue is full
                    // OK last effort to create a thread, disregarding growth limit
                    tr = tryAllocateThread(0.0f);
                    if (tr == AT_YES) {
                        return EXE_CREATE_THREAD;
                    }
                    if (tr == AT_SHUTDOWN) {
                        return EXE_REJECT_SHUTDOWN;
                    }
                    assert tr == AT_NO;
                    return EXE_REJECT_QUEUE_FULL;
                }
                // queue size increased successfully; we can add to the list
                if (node == null) {
                    // avoid re-allocating TaskNode instances on subsequent iterations
                    node = new TaskNode(runnable);
                }
                // state change ex5:
                //   tail(snapshot).next ← new task node
                // cannot succeed: sh2
                // preconditions:
                //   tail(snapshot).next = null
                //   ex3 failed precondition
                //   queue size increased to accommodate node
                // postconditions (success):
                //   tail(snapshot).next = new task node
                if (tail.compareAndSetNext(null, node)) {
                    // try to update tail to the new node; if this CAS fails then tail already points at past the node
                    // this is because tail can only ever move forward, and the task list is always strongly connected
                    compareAndSetTail(tail, node);
                    return EXE_OK;
                }
                // we failed; we have to drop the queue size back down again to compensate before we can retry
                if (!NO_QUEUE_LIMIT) decreaseQueueSize();
            } else if (tailNext instanceof PoolThreadNode) {
                final QNode tailNextNext = tailNext.getNext();
                // state change ex1:
                //   tail(snapshot).next ← tail(snapshot).next(snapshot).next(snapshot)
                // succeeds: -
                // cannot succeed: sh2
                // preconditions:
                //   tail(snapshot) is a dead TaskNode
                //   tail(snapshot).next is PoolThreadNode
                //   tail(snapshot).next.next* is PoolThreadNode or null
                // additional success postconditions: -
                // failure postconditions: -
                // post-actions (succeed):
                //   run state change ex2
                // post-actions (fail):
                //   retry with new tail(snapshot)
                if (tail.compareAndSetNext(tailNext, tailNextNext)) {
                    assert tail instanceof TaskNode;
                    PoolThreadNode consumerNode = (PoolThreadNode) tailNext;
                    // state change ex2:
                    //   tail(snapshot).next(snapshot).task ← runnable
                    // succeeds: ex1
                    // preconditions:
                    //   tail(snapshot).next(snapshot).task = WAITING
                    // post-actions (succeed):
                    //   unpark thread and return
                    // post-actions (fail):
                    //   retry outer with new tail(snapshot)
                    if (consumerNode.compareAndSetTask(WAITING, runnable)) {
                        // GC Nepotism:
                        // We can save consumerNode::next from being dragged into
                        // old generation, if possible
                        consumerNode.compareAndSetNext(tailNextNext, null);
                        consumerNode.unpark();
                        return EXE_OK;
                    }
                    // otherwise the consumer gave up or was exited already, so fall out and...
                }
            } else if (tailNext instanceof TaskNode) {
                TaskNode tailNextTaskNode = (TaskNode) tailNext;
                // Opportunistically update tail to the next node. If this operation has been handled by
                // another thread we fall back to the loop and try again instead of duplicating effort.
                if (compareAndSetTail(tail, tailNextTaskNode)) {
                    tail = tailNextTaskNode;
                    // bypass the on-spin-miss path because we've updated the next task node to tailNextTaskNode.
                    continue;
                }
            } else {
                // no consumers are waiting and the tail(snapshot).next node is non-null and not a task node, therefore it must be a...
                assert tailNext instanceof TerminateWaiterNode;
                // shutting down
                return EXE_REJECT_SHUTDOWN;
            }
            // retry with new tail(snapshot)
            if (UPDATE_STATISTICS) spinMisses.increment();
            tail = this.tail;
        }
        // not reached
    }

    // =======================================================
    // Termination task
    // =======================================================

    void completeTermination() {
        // be kind and un-interrupt the thread for the termination task
        boolean intr = Thread.interrupted();
        try {
            final Runnable terminationTask = JBossExecutors.classLoaderPreservingTask(this.terminationTask);
            this.terminationTask = null;
            try {
                terminationTask.run();
            } catch (Throwable t) {
                try {
                    exceptionHandler.uncaughtException(Thread.currentThread(), t);
                } catch (Throwable ignored) {
                    // nothing else we can safely do here
                }
            }
            // notify all waiters
            Waiter waiters = getAndSetTerminationWaiters(TERMINATE_COMPLETE_WAITER);
            while (waiters != null) {
                unpark(waiters.getThread());
                waiters = waiters.getNext();
            }
            tail.setNext(TERMINATE_COMPLETE);
            if (!DISABLE_MBEAN) {
                //The check for DISABLE_MBEAN is redundant as acc would be null,
                //but GraalVM needs the hint so to not make JMX reachable.
                if (this.acc != null) {
                    final Object handle = this.handle;
                    if (handle != null) {
                        intr = intr || Thread.interrupted();
                        doPrivileged(new MBeanUnregisterAction(handle), acc);
                    }
                    this.acc = null;
                }
            }
        } finally {
            if (intr) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // =======================================================
    // Compare-and-set operations
    // =======================================================

    void incrementActiveCount() {
        unsafe.getAndAddInt(this, activeCountOffset, 1);
    }

    void decrementActiveCount() {
        unsafe.getAndAddInt(this, activeCountOffset, -1);
    }

    boolean compareAndSetPeakThreadCount(final int expect, final int update) {
        return unsafe.compareAndSwapInt(this, peakThreadCountOffset, expect, update);
    }

    boolean compareAndSetPeakQueueSize(final int expect, final int update) {
        return unsafe.compareAndSwapInt(this, peakQueueSizeOffset, expect, update);
    }

    boolean compareAndSetQueueSize(final long expect, final long update) {
        return unsafe.compareAndSwapLong(this, queueSizeOffset, expect, update);
    }

    boolean compareAndSetTerminationWaiters(final Waiter expect, final Waiter update) {
        return unsafe.compareAndSwapObject(this, terminationWaitersOffset, expect, update);
    }

    Waiter getAndSetTerminationWaiters(final Waiter update) {
        return (Waiter) unsafe.getAndSetObject(this, terminationWaitersOffset, update);
    }

    // =======================================================
    // Queue size operations
    // =======================================================

    boolean increaseQueueSize() {
        long oldVal = queueSize;
        int oldSize = currentQueueSizeOf(oldVal);
        if (oldSize >= maxQueueSizeOf(oldVal)) {
            // reject
            return false;
        }
        int newSize = oldSize + 1;
        while (! compareAndSetQueueSize(oldVal, withCurrentQueueSize(oldVal, newSize))) {
            if (UPDATE_STATISTICS) spinMisses.increment();
            oldVal = queueSize;
            oldSize = currentQueueSizeOf(oldVal);
            if (oldSize >= maxQueueSizeOf(oldVal)) {
                // reject
                return false;
            }
            newSize = oldSize + 1;
        }
        if (UPDATE_STATISTICS) {
            do {
                // oldSize now represents the old peak size
                oldSize = peakQueueSize;
                if (newSize <= oldSize) break;
            } while (! compareAndSetPeakQueueSize(oldSize, newSize));
        }
        return true;
    }

    void decreaseQueueSize() {
        long oldVal;
        oldVal = queueSize;
        assert currentQueueSizeOf(oldVal) > 0;
        while (! compareAndSetQueueSize(oldVal, withCurrentQueueSize(oldVal, currentQueueSizeOf(oldVal) - 1))) {
            if (UPDATE_STATISTICS) spinMisses.increment();
            oldVal = queueSize;
            assert currentQueueSizeOf(oldVal) > 0;
        }
    }

    // =======================================================
    // Inline Functions
    // =======================================================

    static int currentQueueSizeOf(long queueSize) {
        return (int) (queueSize & 0x7fff_ffff);
    }

    static long withCurrentQueueSize(long queueSize, int current) {
        assert current >= 0;
        return queueSize & 0xffff_ffff_0000_0000L | current;
    }

    static int maxQueueSizeOf(long queueSize) {
        return (int) (queueSize >>> 32 & 0x7fff_ffff);
    }

    static long withMaxQueueSize(long queueSize, int max) {
        assert max >= 0;
        return queueSize & 0xffff_ffffL | (long)max << 32;
    }

    static int coreSizeOf(long status) {
        return (int) (status >>> TS_CORE_SHIFT & TS_THREAD_CNT_MASK);
    }

    static int maxSizeOf(long status) {
        return (int) (status >>> TS_MAX_SHIFT & TS_THREAD_CNT_MASK);
    }

    static int currentSizeOf(long status) {
        return (int) (status >>> TS_CURRENT_SHIFT & TS_THREAD_CNT_MASK);
    }

    static long withCoreSize(long status, int newCoreSize) {
        assert 0 <= newCoreSize && newCoreSize <= TS_THREAD_CNT_MASK;
        return status & ~(TS_THREAD_CNT_MASK << TS_CORE_SHIFT) | (long)newCoreSize << TS_CORE_SHIFT;
    }

    static long withCurrentSize(long status, int newCurrentSize) {
        assert 0 <= newCurrentSize && newCurrentSize <= TS_THREAD_CNT_MASK;
        return status & ~(TS_THREAD_CNT_MASK << TS_CURRENT_SHIFT) | (long)newCurrentSize << TS_CURRENT_SHIFT;
    }

    static long withMaxSize(long status, int newMaxSize) {
        assert 0 <= newMaxSize && newMaxSize <= TS_THREAD_CNT_MASK;
        return status & ~(TS_THREAD_CNT_MASK << TS_MAX_SHIFT) | (long)newMaxSize << TS_MAX_SHIFT;
    }

    static long withShutdownRequested(final long status) {
        return status | TS_SHUTDOWN_REQUESTED;
    }

    static long withShutdownComplete(final long status) {
        return status | TS_SHUTDOWN_COMPLETE;
    }

    static long withShutdownInterrupt(final long status) {
        return status | TS_SHUTDOWN_INTERRUPT;
    }

    static long withAllowCoreTimeout(final long status, final boolean allowed) {
        return allowed ? status | TS_ALLOW_CORE_TIMEOUT : status & ~TS_ALLOW_CORE_TIMEOUT;
    }

    static boolean isShutdownRequested(final long status) {
        return (status & TS_SHUTDOWN_REQUESTED) != 0;
    }

    static boolean isShutdownComplete(final long status) {
        return (status & TS_SHUTDOWN_COMPLETE) != 0;
    }

    static boolean isShutdownInterrupt(final long threadStatus) {
        return (threadStatus & TS_SHUTDOWN_INTERRUPT) != 0;
    }

    static boolean isAllowCoreTimeout(final long oldVal) {
        return (oldVal & TS_ALLOW_CORE_TIMEOUT) != 0;
    }

    // =======================================================
    // Static configuration
    // =======================================================

    // =======================================================
    // Utilities
    // =======================================================

    void rejectException(final Task task, final Throwable cause) {
        try {
            handoffExecutor.execute(task.handoff());
        } catch (Throwable t) {
            t.addSuppressed(cause);
            throw t;
        }
    }

    void rejectNoThread(final Task task) {
        try {
            handoffExecutor.execute(task.handoff());
        } catch (Throwable t) {
            t.addSuppressed(new RejectedExecutionException("No threads available"));
            throw t;
        }
    }

    void rejectQueueFull(final Task task) {
        try {
            handoffExecutor.execute(task.handoff());
        } catch (Throwable t) {
            t.addSuppressed(new RejectedExecutionException("Queue is full"));
            throw t;
        }
    }

    void rejectShutdown(final Task task) {
        try {
            handoffExecutor.execute(task.handoff());
        } catch (Throwable t) {
            t.addSuppressed(new RejectedExecutionException("Executor is being shut down"));
            throw t;
        }
    }

    static Runnable nullToNop(final Runnable task) {
        return task == null ? NullRunnable.getInstance() : task;
    }

    // =======================================================
    // Node classes
    // =======================================================

    abstract static class QNode {
        private static final long nextOffset;

        static {
            try {
                nextOffset = unsafe.objectFieldOffset(QNode.class.getDeclaredField("next"));
            } catch (NoSuchFieldException e) {
                throw new NoSuchFieldError(e.getMessage());
            }
        }

        @SuppressWarnings("unused")
        private volatile QNode next;

        boolean compareAndSetNext(QNode expect, QNode update) {
            return unsafe.compareAndSwapObject(this, nextOffset, expect, update);
        }

        QNode getNext() {
            return next;
        }

        void setNext(final QNode node) {
            next = node;
        }

        void setNextRelaxed(final QNode node) {
            unsafe.putObject(this, nextOffset, node);
        }

        void setNextOrdered(final QNode node) {
            unsafe.putOrderedObject(this, nextOffset, node);
        }
    }

    /** Padding between PoolThreadNode task and parked fields and QNode.next */
    static abstract class PoolThreadNodeBase extends QNode {
        /**
         * Padding fields.
         */
        @SuppressWarnings("unused")
        int p00, p01, p02, p03,
                p04, p05, p06, p07,
                p08, p09, p0A, p0B,
                p0C, p0D, p0E, p0F;

        PoolThreadNodeBase() {}
    }

    static final class PoolThreadNode extends PoolThreadNodeBase {

        /**
         * Thread is running normally.
         */
        private static final int STATE_NORMAL = 0;

        /**
         * Thread is parked (or about to park), and unpark call is necessary to wake the thread
         */
        private static final int STATE_PARKED = 1;

        /**
         * The thread has been unparked, any thread that is spinning or about to park will return,
         * if not thread is currently spinning the next thread that attempts to spin will immediately return
         */
        private static final int STATE_UNPARKED = 2;


        private static final long taskOffset;
        private static final long parkedOffset;

        static {
            try {
                taskOffset = unsafe.objectFieldOffset(PoolThreadNode.class.getDeclaredField("task"));
                parkedOffset = unsafe.objectFieldOffset(PoolThreadNode.class.getDeclaredField("parked"));
            } catch (NoSuchFieldException e) {
                throw new NoSuchFieldError(e.getMessage());
            }
        }

        private final Thread thread;

        @SuppressWarnings("unused")
        private volatile Runnable task;

        /**
         * The park state, see the STATE_ constants for the meanings of each value
         */
        @SuppressWarnings("unused")
        private volatile int parked;

        PoolThreadNode(final Thread thread) {
            this.thread = thread;
            task = WAITING;
        }

        boolean compareAndSetTask(final Runnable expect, final Runnable update) {
            return task == expect && unsafe.compareAndSwapObject(this, taskOffset, expect, update);
        }

        Runnable getTask() {
            return task;
        }

        PoolThreadNode getNext() {
            return (PoolThreadNode) super.getNext();
        }

        void park(EnhancedQueueExecutor enhancedQueueExecutor) {
            int spins = PARK_SPINS;
            if (parked == STATE_UNPARKED && unsafe.compareAndSwapInt(this, parkedOffset, STATE_UNPARKED, STATE_NORMAL)) {
                return;
            }
            while (spins > 0) {
                if (spins < YIELD_FACTOR) {
                    Thread.yield();
                } else {
                    JDKSpecific.onSpinWait();
                }
                spins--;
                if (parked == STATE_UNPARKED && unsafe.compareAndSwapInt(this, parkedOffset, STATE_UNPARKED, STATE_NORMAL)) {
                    return;
                }
            }
            if (parked == STATE_NORMAL && unsafe.compareAndSwapInt(this, parkedOffset, STATE_NORMAL, STATE_PARKED)) try {
                LockSupport.park(enhancedQueueExecutor);
            } finally {
                // parked can be STATE_PARKED or STATE_UNPARKED, cannot possibly be STATE_NORMAL.
                // if it's STATE_PARKED, we'd go back to NORMAL since we're not parking anymore.
                // if it's STATE_UNPARKED, we can still go back to NORMAL because all of our preconditions will be rechecked anyway.
                parked = STATE_NORMAL;
            }
        }

        void park(EnhancedQueueExecutor enhancedQueueExecutor, long nanos) {
            long remaining;
            int spins = PARK_SPINS;
            if (spins > 0) {
                long start = System.nanoTime();
                //note that we don't check the nanotime while spinning
                //as spin time is short and for our use cases it does not matter if the time
                //overruns a bit (as the nano time is for thread timeout) we just spin then check
                //to keep performance consistent between the two versions.
                if (parked == STATE_UNPARKED && unsafe.compareAndSwapInt(this, parkedOffset, STATE_UNPARKED, STATE_NORMAL)) {
                    return;
                }
                while (spins > 0) {
                    if (spins < YIELD_FACTOR) {
                        Thread.yield();
                    } else {
                        JDKSpecific.onSpinWait();
                    }
                    if (parked == STATE_UNPARKED && unsafe.compareAndSwapInt(this, parkedOffset, STATE_UNPARKED, STATE_NORMAL)) {
                        return;
                    }
                    spins--;
                }
                remaining = nanos - (System.nanoTime() - start);
                if (remaining < 0) {
                    return;
                }
            } else {
                remaining = nanos;
            }
            if (parked == STATE_NORMAL && unsafe.compareAndSwapInt(this, parkedOffset, STATE_NORMAL, STATE_PARKED)) try {
                LockSupport.parkNanos(enhancedQueueExecutor, remaining);
            } finally {
                // parked can be STATE_PARKED or STATE_UNPARKED, cannot possibly be STATE_NORMAL.
                // if it's STATE_PARKED, we'd go back to NORMAL since we're not parking anymore.
                // if it's STATE_UNPARKED, we can still go back to NORMAL because all of our preconditions will be rechecked anyway.
                parked = STATE_NORMAL;
            }
        }

        void unpark() {
            if (parked == STATE_NORMAL && unsafe.compareAndSwapInt(this, parkedOffset, STATE_NORMAL, STATE_UNPARKED)) {
                return;
            }
            LockSupport.unpark(thread);
        }
    }

    static final class TerminateWaiterNode extends QNode {
    }

    static final class TaskNode extends QNode {
        Task task;

        TaskNode(final Task task) {
            // we always start task nodes with a {@code null} next
            this.task = task;
        }

        Runnable getAndClearTask() {
            Runnable result = task;
            task = null;
            return result;
        }
    }

    // =======================================================
    // Management bean implementation
    // =======================================================

    final class MXBeanImpl implements StandardThreadPoolMXBean {
        MXBeanImpl() {
        }

        public float getGrowthResistance() {
            return EnhancedQueueExecutor.this.getGrowthResistance();
        }

        public void setGrowthResistance(final float value) {
            EnhancedQueueExecutor.this.setGrowthResistance(value);
        }

        public boolean isGrowthResistanceSupported() {
            return true;
        }

        public int getCorePoolSize() {
            return EnhancedQueueExecutor.this.getCorePoolSize();
        }

        public void setCorePoolSize(final int corePoolSize) {
            EnhancedQueueExecutor.this.setCorePoolSize(corePoolSize);
        }

        public boolean isCorePoolSizeSupported() {
            return true;
        }

        public boolean prestartCoreThread() {
            return EnhancedQueueExecutor.this.prestartCoreThread();
        }

        public int prestartAllCoreThreads() {
            return EnhancedQueueExecutor.this.prestartAllCoreThreads();
        }

        public boolean isCoreThreadPrestartSupported() {
            return true;
        }

        public int getMaximumPoolSize() {
            return EnhancedQueueExecutor.this.getMaximumPoolSize();
        }

        public void setMaximumPoolSize(final int maxPoolSize) {
            EnhancedQueueExecutor.this.setMaximumPoolSize(maxPoolSize);
        }

        public int getPoolSize() {
            return EnhancedQueueExecutor.this.getPoolSize();
        }

        public int getLargestPoolSize() {
            return EnhancedQueueExecutor.this.getLargestPoolSize();
        }

        public int getActiveCount() {
            return EnhancedQueueExecutor.this.getActiveCount();
        }

        public boolean isAllowCoreThreadTimeOut() {
            return EnhancedQueueExecutor.this.allowsCoreThreadTimeOut();
        }

        public void setAllowCoreThreadTimeOut(final boolean value) {
            EnhancedQueueExecutor.this.allowCoreThreadTimeOut(value);
        }

        public long getKeepAliveTimeSeconds() {
            return EnhancedQueueExecutor.this.getKeepAliveTime().getSeconds();
        }

        public void setKeepAliveTimeSeconds(final long seconds) {
            EnhancedQueueExecutor.this.setKeepAliveTime(Duration.of(seconds, ChronoUnit.SECONDS));
        }

        public int getMaximumQueueSize() {
            return EnhancedQueueExecutor.this.getMaximumQueueSize();
        }

        public void setMaximumQueueSize(final int size) {
            EnhancedQueueExecutor.this.setMaximumQueueSize(size);
        }

        public int getQueueSize() {
            return EnhancedQueueExecutor.this.getQueueSize();
        }

        public int getLargestQueueSize() {
            return EnhancedQueueExecutor.this.getLargestQueueSize();
        }

        public boolean isQueueBounded() {
            return ! NO_QUEUE_LIMIT;
        }

        public boolean isQueueSizeModifiable() {
            return ! NO_QUEUE_LIMIT;
        }

        public boolean isShutdown() {
            return EnhancedQueueExecutor.this.isShutdown();
        }

        public boolean isTerminating() {
            return EnhancedQueueExecutor.this.isTerminating();
        }

        public boolean isTerminated() {
            return EnhancedQueueExecutor.this.isTerminated();
        }

        public long getSubmittedTaskCount() {
            return EnhancedQueueExecutor.this.getSubmittedTaskCount();
        }

        public long getRejectedTaskCount() {
            return EnhancedQueueExecutor.this.getRejectedTaskCount();
        }

        public long getCompletedTaskCount() {
            return EnhancedQueueExecutor.this.getCompletedTaskCount();
        }

        public long getSpinMissCount() {
            return EnhancedQueueExecutor.this.spinMisses.longValue();
        }
    }

    // =======================================================
    // Basic task wrapper
    // =======================================================

    final class Task implements Runnable {

        private final Runnable delegate;
        private final ClassLoader contextClassLoader;
        private final Object context;

        Task(Runnable delegate, final Object context) {
            Assert.checkNotNullParam("delegate", delegate);
            this.delegate = delegate;
            this.context = context;
            this.contextClassLoader = JBossExecutors.getContextClassLoader(Thread.currentThread());
        }

        @Override
        @SuppressWarnings("rawtypes")
        public void run() {
            if (isShutdownInterrupt(threadStatus)) {
                Thread.currentThread().interrupt();
            } else {
                Thread.interrupted();
            }
            if (UPDATE_ACTIVE_COUNT) incrementActiveCount();
            final Thread currentThread = Thread.currentThread();
            final ClassLoader old = JBossExecutors.getAndSetContextClassLoader(currentThread, contextClassLoader);
            try {
                ((ContextHandler)contextHandler).runWith(delegate, context);
            } catch (Throwable t) {
                try {
                    exceptionHandler.uncaughtException(Thread.currentThread(), t);
                } catch (Throwable ignored) {
                    // nothing else we can safely do here
                }
            } finally {
                JBossExecutors.setContextClassLoader(currentThread, old);
            }
            Thread.interrupted();
            if (UPDATE_ACTIVE_COUNT) {
                decrementActiveCount();
                if (UPDATE_STATISTICS) {
                    completedTaskCounter.increment();
                }
            }
        }

        /**
         * Extracts the original runnable without EQE-specific state updating. This runnable does retain the original
         * context classloader from the submitting thread.
         */
        Runnable handoff() {
            return new ContextClassLoaderSavingRunnable(contextClassLoader, delegate);
        }

        @Override
        public String toString() {
            return "Task{delegate=" + delegate + ", contextClassLoader=" + contextClassLoader + '}';
        }
    }

    // =======================================================
    // Scheduled future tasks
    // =======================================================

    static final int ASF_ST_WAITING = 0;
    static final int ASF_ST_CANCELLED = 1;
    static final int ASF_ST_SUBMITTED = 2;
    static final int ASF_ST_RUNNING = 3;
    static final int ASF_ST_FINISHED = 4;
    static final int ASF_ST_FAILED = 5;
    static final int ASF_ST_REJECTED = 6;

    static final AbstractScheduledFuture<?>[] NO_FUTURES = new AbstractScheduledFuture<?>[0];

    static final AtomicLong SCHEDULED_TASK_SEQ = new AtomicLong();

    /**
     * An implementation of {@link ScheduledFuture} which is wrapped by {@link Task}.
     *
     * @param <V> the result type
     */
    abstract class AbstractScheduledFuture<V> implements ScheduledFuture<V>, Runnable {
        final long seq = SCHEDULED_TASK_SEQ.getAndIncrement();
        /**
         * The task which is wrapping this one.
         */
        final Task wrappingTask;
        /**
         * The scheduled time for this task, in nanoseconds since the scheduler thread was born.
         * Can be mutated in subclasses if the task is recurring, but only under lock.
         */
        volatile long when;
        /**
         * The state of this task; one of {@code ASF_ST_*}.
         */
        volatile int state = ASF_ST_WAITING;
        /**
         * The actual result; only valid in {@code ASF_ST_FINISHED} (where it is of type {@code V}),
         * or in {@code ASF_ST_FAILED} (where it is of type {@code Throwable}),
         * or in {@code ASF_ST_REJECTED} (where it is of type {@code RejectedExecutionException}).
         */
        volatile Object result;
        /**
         * The thread which is currently live for this task.
         */
        Thread liveThread;

        AbstractScheduledFuture(long delay, TimeUnit unit) {
            when = Math.addExact(schedulerTask.age(), unit.toNanos(delay));
            wrappingTask = new Task(this, contextHandler.captureContext());
        }

        public int compareTo(final Delayed o) {
            return o instanceof AbstractScheduledFuture<?> ? compareTo((AbstractScheduledFuture<?>) o) : wrongType();
        }

        public int compareTo(final AbstractScheduledFuture<?> other) {
            int cmp = Long.compare(when, other.when);
            if (cmp == 0) cmp = Long.compare(seq, other.seq);
            return cmp;
        }

        public long getDelay(final TimeUnit unit) {
            return unit.convert(Math.max(0, when - schedulerTask.age()), TimeUnit.NANOSECONDS);
        }

        public boolean isCancelled() {
            return state == ASF_ST_CANCELLED;
        }

        public boolean isDone() {
            int state = this.state;
            return state == ASF_ST_FINISHED || state == ASF_ST_FAILED || state == ASF_ST_CANCELLED || state == ASF_ST_REJECTED;
        }

        public boolean cancel(final boolean mayInterruptIfRunning) {
            int state;
            synchronized (this) {
                state = this.state;
                switch (state) {
                    case ASF_ST_WAITING:
                    case ASF_ST_SUBMITTED: {
                        this.state = ASF_ST_CANCELLED;
                        return true;
                    }
                    case ASF_ST_RUNNING: {
                        if (mayInterruptIfRunning) {
                            liveThread.interrupt();
                        }
                        return false;
                    }
                    case ASF_ST_CANCELLED: {
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
            }
        }

        public V get() throws InterruptedException, ExecutionException {
            int state;
            synchronized (this) {
                for (;;) {
                    state = this.state;
                    switch (state) {
                        case ASF_ST_WAITING:
                        case ASF_ST_SUBMITTED:
                        case ASF_ST_RUNNING: {
                            wait();
                            break;
                        }
                        case ASF_ST_CANCELLED: {
                            throw new CancellationException("Task was cancelled");
                        }
                        case ASF_ST_REJECTED: {
                            throw new ExecutionException("Task failed due to rejection", (RejectedExecutionException) result);
                        }
                        case ASF_ST_FAILED: {
                            throw new ExecutionException((Throwable) result);
                        }
                        case ASF_ST_FINISHED: {
                            //noinspection unchecked
                            return (V) result;
                        }
                    }
                }
            }
        }

        public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            long remaining = unit.toNanos(timeout);
            long start = System.nanoTime();
            int state;
            synchronized (this) {
                for (;;) {
                    state = this.state;
                    switch (state) {
                        case ASF_ST_WAITING:
                        case ASF_ST_SUBMITTED:
                        case ASF_ST_RUNNING: {
                            if (remaining <= 0) {
                                throw new TimeoutException();
                            }
                            wait(remaining / 1_000_000L, (int) (remaining % 1_000_000));
                            break;
                        }
                        case ASF_ST_CANCELLED: {
                            throw new CancellationException("Task was cancelled");
                        }
                        case ASF_ST_REJECTED: {
                            throw new ExecutionException("Task failed due to rejection", (RejectedExecutionException) result);
                        }
                        case ASF_ST_FAILED: {
                            throw new ExecutionException((Throwable) result);
                        }
                        case ASF_ST_FINISHED: {
                            //noinspection unchecked
                            return (V) result;
                        }
                    }
                    long newStart = System.nanoTime();
                    long elapsed = newStart - start;
                    remaining -= elapsed;
                    start = newStart;
                }
            }
        }

        public void run() {
            stateTest: synchronized (this) {
                switch (state) {
                    case ASF_ST_SUBMITTED: {
                        this.state = ASF_ST_RUNNING;
                        liveThread = currentThread();
                        break stateTest;
                    }
                    case ASF_ST_CANCELLED: {
                        // cancelled after submit but before it was run
                        return;
                    }
                    case ASF_ST_FAILED:
                    case ASF_ST_REJECTED: {
                        // a recurring task terminated abruptly, but was still found in the schedule
                        return;
                    }
                    default: {
                        // invalid state
                        fail(badState());
                        return;
                    }
                }
            }
            try {
                finish(performTask());
            } catch (Throwable t) {
                fail(t);
            }
        }

        void submit() {
            synchronized (this) {
                stateTest: switch (state) {
                    case ASF_ST_WAITING: {
                        this.state = ASF_ST_SUBMITTED;
                        //noinspection UnnecessaryLabelOnBreakStatement
                        break stateTest;
                    }
                    case ASF_ST_CANCELLED: {
                        // do not actually submit
                        return;
                    }
                    case ASF_ST_FAILED:
                    case ASF_ST_REJECTED: {
                        // a recurring task terminated abruptly, but was still found in the schedule
                        return;
                    }
                    default: {
                        // invalid state
                        fail(badState());
                        return;
                    }
                }
            }
            try {
                /* copied from {@link #execute(Runnable)} */
                int result = tryExecute(wrappingTask);
                boolean ok = false;
                if (result == EXE_OK) {
                    // last check to ensure that there is at least one existent thread to avoid rare thread timeout race condition
                    if (currentSizeOf(threadStatus) == 0 && tryAllocateThread(0.0f) == AT_YES && ! doStartThread(null)) {
                        deallocateThread();
                    }
                    if (UPDATE_STATISTICS) submittedTaskCounter.increment();
                    return;
                } else if (result == EXE_CREATE_THREAD) try {
                    ok = doStartThread(wrappingTask);
                } finally {
                    if (! ok) deallocateThread();
                } else {
                    if (UPDATE_STATISTICS) rejectedTaskCounter.increment();
                    if (result == EXE_REJECT_SHUTDOWN) {
                        rejectShutdown(wrappingTask);
                    } else {
                        assert result == EXE_REJECT_QUEUE_FULL;
                        rejectQueueFull(wrappingTask);
                    }
                }
            } catch (RejectedExecutionException e) {
                reject(e);
            } catch (Throwable t) {
                reject(new RejectedExecutionException("Task submission failed", t));
            }
        }

        IllegalStateException badState() {
            return new IllegalStateException("Task was not in expected state");
        }

        void reject(RejectedExecutionException e) {
            synchronized (this) {
                switch (state) {
                    case ASF_ST_SUBMITTED: {
                        result = e;
                        this.state = ASF_ST_REJECTED;
                        liveThread = null;
                        notifyAll();
                        return;
                    }
                    default: {
                        // invalid state
                        fail(badState());
                        return;
                    }
                }
            }
        }

        void fail(Throwable t) {
            synchronized (this) {
                switch (state) {
                    case ASF_ST_WAITING:
                    case ASF_ST_SUBMITTED:
                    case ASF_ST_RUNNING: {
                        result = t;
                        this.state = ASF_ST_FAILED;
                        liveThread = null;
                        notifyAll();
                        return;
                    }
                    case ASF_ST_CANCELLED:
                    case ASF_ST_FINISHED:
                    case ASF_ST_FAILED:
                    case ASF_ST_REJECTED: {
                        // ignore the failure, though we're likely in an invalid state
                        return;
                    }
                }
            }
        }

        void finish(V result) {
            // overridden in subclasses where the task repeats
            synchronized (this) {
                switch (state) {
                    case ASF_ST_RUNNING: {
                        this.result = result;
                        this.state = ASF_ST_FINISHED;
                        liveThread = null;
                        notifyAll();
                        return;
                    }
                    default: {
                        // invalid state
                        fail(badState());
                        return;
                    }
                }
            }
        }

        abstract V performTask() throws Exception;

        public String toString() {
            return toString(new StringBuilder()).toString();
        }

        StringBuilder toString(StringBuilder b) {
            return b.append("future result of ");
        }
    }

    static int wrongType() throws ClassCastException {
        throw new ClassCastException("Wrong task type for comparison");
    }

    final class RunnableScheduledFuture extends AbstractScheduledFuture<Void> {
        final Runnable runnable;

        RunnableScheduledFuture(final Runnable runnable, final long delay, final TimeUnit unit) {
            super(delay, unit);
            this.runnable = runnable;
        }

        Void performTask() {
            runnable.run();
            return null;
        }

        StringBuilder toString(final StringBuilder b) {
            return super.toString(b).append(runnable);
        }
    }

    final class CallableScheduledFuture<V> extends AbstractScheduledFuture<V> {
        final Callable<V> callable;

        CallableScheduledFuture(final Callable<V> callable, final long delay, final TimeUnit unit) {
            super(delay, unit);
            this.callable = callable;
        }

        V performTask() throws Exception {
            return callable.call();
        }

        StringBuilder toString(final StringBuilder b) {
            return super.toString(b).append(callable);
        }
    }

    abstract class RepeatingScheduledFuture<V> extends AbstractScheduledFuture<V> {
        final long period;

        RepeatingScheduledFuture(final long delay, final long period, final TimeUnit unit) {
            super(delay, unit);
            this.period = unit.toNanos(period);
        }

        /**
         * Adjust the time of this future for resubmission, after the task has run successfully.
         */
        abstract void adjustTime();

        public void run() {
            super.run();
            // if an exception is thrown, we will have failed already anyway
            adjustTime();
            synchronized (this) {
                switch (state) {
                    case ASF_ST_RUNNING: {
                        state = ASF_ST_WAITING;
                        schedulerTask.schedule(this);
                        return;
                    }
                    default: {
                        // in all other cases, we failed so the task should not be rescheduled
                        return;
                    }
                }
            }
        }

        void finish(final V result) {
            // repeating tasks never actually finish
        }

        StringBuilder toString(final StringBuilder b) {
            return super.toString(b.append("repeating "));
        }
    }

    final class FixedRateRunnableScheduledFuture extends RepeatingScheduledFuture<Void> {
        final Runnable runnable;

        FixedRateRunnableScheduledFuture(final Runnable runnable, final long delay, final long period, final TimeUnit unit) {
            super(delay, period, unit);
            this.runnable = runnable;
        }

        void adjustTime() {
            // if this results in a time in the past, the next run will happen immediately
            this.when += period;
        }

        Void performTask() {
            runnable.run();
            return null;
        }

        StringBuilder toString(final StringBuilder b) {
            return super.toString(b).append(runnable);
        }
    }

    final class FixedDelayRunnableScheduledFuture extends RepeatingScheduledFuture<Void> {
        final Runnable runnable;

        FixedDelayRunnableScheduledFuture(final Runnable runnable, final long delay, final long period, final TimeUnit unit) {
            super(delay, period, unit);
            this.runnable = runnable;
        }

        void adjustTime() {
            this.when = schedulerTask.age() + period;
        }

        Void performTask() {
            runnable.run();
            return null;
        }

        StringBuilder toString(final StringBuilder b) {
            return super.toString(b).append(runnable);
        }
    }

    // =======================================================
    // Scheduler task thread worker
    // =======================================================

    final class SchedulerTask implements Runnable {
        final long startMark = System.nanoTime();
        final ReentrantLock ql = new ReentrantLock();
        final Condition qc = ql.newCondition();
        // todo: switch to array queue on a more optimistic day
        // protected by {@link #ql}
        ScheduledFutureQueue q = new TreeSetQueue();
        boolean shutdownDetected;

        void shutdown() {
            ql.lock();
            try {
                shutdownDetected = true;
                qc.signal();
            } finally {
                ql.unlock();
            }
        }

        public void run() {
            ScheduledFutureQueue q = this.q;
            AbstractScheduledFuture<?>[] remainingFutures;
            AbstractScheduledFuture<?> first;
            long startMark = this.startMark;
            outerLoop: for (;;) {
                ql.lock();
                try {
                    innerLoop: for (;;) {
                        long now = System.nanoTime();
                        if (shutdownDetected) {
                            // drop all tasks and return
                            remainingFutures = q.toArray();
                            q.clear();
                            break outerLoop;
                        } else if (q.isEmpty()) try {
                            qc.await();
                        } catch (InterruptedException ignored) {
                            // clear interrupt status
                            continue innerLoop;
                        } else {
                            first = q.first();
                            long firstWhen = first.when;
                            long currentWhen = max(0, now - startMark);
                            if (firstWhen <= currentWhen) {
                                // it's ready; run it outside of the lock
                                q.pollFirst();
                                //noinspection UnnecessaryLabelOnBreakStatement
                                break innerLoop;
                            } else {
                                long waitTime = firstWhen - currentWhen;
                                try {
                                    qc.awaitNanos(waitTime);
                                } catch (InterruptedException e) {
                                    // clear interrupt status
                                    continue innerLoop;
                                }
                            }
                        }
                    }
                } finally {
                    ql.unlock();
                }
                // outside of lock; `break innerLoop` goes ↓ here
                first.submit();
                // continue loop to find the next task
            }
            // ↓ `break outerLoop` goes here ↓
            if (remainingFutures.length > 0) {
                for (AbstractScheduledFuture<?> future : remainingFutures) {
                    future.cancel(true);
                }
            }
            return;
        }

        <T, F extends AbstractScheduledFuture<T>> F schedule(final F item) {
            Task wrappingTask = item.wrappingTask;
            if (item.when <= age()) {
                // just submit it now
                item.submit();
                return item;
            }
            ql.lock();
            try {
                if (shutdownDetected) {
                    rejectShutdown(wrappingTask);
                    return item;
                }
                // check to see if we need to wake up the scheduler
                boolean first;
                for (;;) try {
                    first = q.insertAndCheckForFirst(item);
                    break;
                } catch (QueueFullException ignored) {
                    q = q.grow();
                }
                if (first) {
                    // the delay time has changed, so wake up the waiter
                    qc.signal();
                }
                return item;
            } finally {
                ql.unlock();
            }
        }

        long age() {
            return System.nanoTime() - startMark;
        }
    }

    // =======================================================
    // Schedule queue API & implementations
    // =======================================================

    interface ScheduledFutureQueue {
        AbstractScheduledFuture<?>[] toArray();

        void clear();

        boolean isEmpty();

        int size();

        AbstractScheduledFuture<?> first();

        @SuppressWarnings("UnusedReturnValue") // must match signature for TreeSet
        AbstractScheduledFuture<?> pollFirst();

        /**
         * Insert the item in order, checking to see if it was added as the first item.
         *
         * @param item the item to insert (must not be {@code null})
         * @return {@code true} if the item is first, {@code false} otherwise
         * @throws QueueFullException if the queue is full; it must be recreated in this case
         */
        boolean insertAndCheckForFirst(AbstractScheduledFuture<?> item) throws QueueFullException;

        /**
         * Get a new queue with the same contents as this one, but with a larger capacity.
         *
         * @return the grown queue
         */
        ScheduledFutureQueue grow();
    }

    static final class ArrayQueue implements ScheduledFutureQueue {
        final AbstractScheduledFuture<?>[] array;
        // the removal point (lowest+least index)
        int head;
        // the number of elements
        int size;

        ArrayQueue(int capacity) {
            // next power of two
            capacity = Integer.highestOneBit(Math.max(capacity, 2) - 1) << 1;
            array = new AbstractScheduledFuture<?>[capacity];
        }

        private ArrayQueue(final ArrayQueue original, final int newCapacity) {
            assert Integer.bitCount(newCapacity) == 1;
            array = original.toArray(newCapacity);
            head = 0;
            size = original.size;
        }

        public AbstractScheduledFuture<?>[] toArray() {
            return toArray(size());
        }

        public AbstractScheduledFuture<?>[] toArray(int size) {
            int head = this.head;
            int end = head + size;
            AbstractScheduledFuture<?>[] copy = Arrays.copyOfRange(array, head, end);
            if (end > array.length) {
                // copy the wrapped elements
                System.arraycopy(array, 0, copy, size - (array.length - head), size - array.length);
            }
            return copy;
        }

        public void clear() {
            Arrays.fill(array, null);
            head = size = 0;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public int size() {
            return size;
        }

        public AbstractScheduledFuture<?> first() {
            if (size == 0) {
                throw new NoSuchElementException();
            }
            return array[head];
        }

        public AbstractScheduledFuture<?> pollFirst() {
            if (size == 0) {
                throw new NoSuchElementException();
            }
            int head = this.head;
            AbstractScheduledFuture<?> item = array[head];
            array[head] = null;
            this.size --;
            int mask = array.length - 1;
            this.head = head + 1 & mask;
            return item;
        }

        public boolean insertAndCheckForFirst(final AbstractScheduledFuture<?> item) {
            // find the insertion point
            int size = this.size;
            AbstractScheduledFuture<?>[] array = this.array;
            int arrayLen = array.length;
            if (size == arrayLen) {
                throw new QueueFullException();
            }
            int mask = arrayLen - 1;
            int idx = 0;
            int high = size - 1;
            // at this point and onwards, there is definitely space in the array
            int head = this.head;

            while (idx <= high) {
                int mid = (idx + high) >>> 1;
                AbstractScheduledFuture<?> testVal = array[head + mid & mask];
                int cmp = testVal.compareTo(item);
                if (cmp < 0) {
                    idx = mid + 1;
                } else if (cmp > 0) {
                    high = mid - 1;
                } else {
                    // we found this task already present in the queue (should never happen)
                    return false;
                }
            }

            return insertAt(idx, item);
        }

        /**
         * Move all elements starting at the given index forward to make space at that position, wrapping if needed.
         *
         * @param idx the element index relative to {@code head} to open up
         */
        void moveForward(final int idx, final AbstractScheduledFuture<?> storeVal) {
            AbstractScheduledFuture<?>[] array = this.array;
            int size = this.size;
            int moveCnt = size - idx;
            int arrayLength = array.length;
            int mask = arrayLength - 1;
            int head = this.head;
            int start = head + idx;
            // TODO:
            //    - change this to three calls to System.arraycopy
            //    - one for the already-wrapped portion
            //    - one for the portion that is being newly wrapped
            //    - one for the leading (pre-wrap) portion
            for (int i = moveCnt - 1; i >= 0; i --) {
                int pos = start + i;
                array[pos + 1 & mask] = array[pos & mask];
            }
            array[start & mask] = storeVal;
        }

        /**
         * Move all elements starting <em>before</em> the given index backward to make space at that position, wrapping if needed.
         *
         * @param idx the element index relative to {@code head} to open up
         */
        void moveBackward(final int idx, final AbstractScheduledFuture<?> storeVal) {
            AbstractScheduledFuture<?>[] array = this.array;
            int size = this.size;
            int moveCnt = size - idx + 1;
            int arrayLength = array.length;
            int mask = arrayLength - 1;
            int head = this.head;
            int start = head + idx - 1;
            // TODO:
            //    - change this to three calls to System.arraycopy
            //    - one for the leading (pre-wrap) portion
            //    - one for the portion that is being newly de-wrapped
            //    - one for the already-wrapped portion
            for (int i = moveCnt - 1; i >= 0; i --) {
                int pos = start - i;
                array[pos - 1 & mask] = array[pos & mask];
            }
            array[start & mask] = storeVal;
            this.head = head - 1 & mask;
        }

        boolean insertAt(final int idx, final AbstractScheduledFuture<?> item) {
            // this is a separate method for easier testing of the arraycopy algebraic mayhem
            int size = this.size;
            // no matter what, we're growing by one
            this.size = size + 1;
            int halfSize = size + 1 >> 1;
            if (idx >= halfSize) {
                moveForward(idx, item);
            } else {
                moveBackward(idx, item);
            }
            return idx == 0;
        }

        public ScheduledFutureQueue grow() {
            // todo: calibrate this threshold
            if (array.length >= 256) {
                return new TreeSetQueue(this);
            } else {
                return new ArrayQueue(this, array.length << 1);
            }
        }

        // test points for white-box unit tests

        int testPoint_arrayLength() {
            return array.length;
        }

        int testPoint_head() {
            return head;
        }

        void testPoint_setHead(int newHead) {
            head = newHead;
        }

        void testPoint_setSize(int newSize) {
            size = newSize;
        }

        AbstractScheduledFuture<?> testPoint_getArrayItem(int index) {
            return array[index & array.length - 1];
        }

        AbstractScheduledFuture<?> testPoint_setArrayItem(int index, AbstractScheduledFuture<?> item) {
            try {
                return array[index & array.length - 1];
            } finally {
                array[index & array.length - 1] = item;
            }
        }
    }

    @SuppressWarnings("serial")
    static class TreeSetQueue extends TreeSet<AbstractScheduledFuture<?>> implements ScheduledFutureQueue {
        TreeSetQueue(final ScheduledFutureQueue original) {
            Collections.addAll(this, original.toArray());
        }

        TreeSetQueue() {
        }

        public AbstractScheduledFuture<?>[] toArray() {
            return super.toArray(NO_FUTURES);
        }

        public boolean insertAndCheckForFirst(final AbstractScheduledFuture<?> item) {
            add(item);
            return item == first();
        }

        public ScheduledFutureQueue grow() {
            return this;
        }
    }

    @SuppressWarnings("serial")
    static final class QueueFullException extends RuntimeException {
        QueueFullException() {
            super(null, null, false, false);
        }
    }
}
