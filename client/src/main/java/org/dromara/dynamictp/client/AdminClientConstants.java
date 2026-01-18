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

package org.dromara.dynamictp.client;

/**
 * Constants for Admin client configuration
 *
 * @author eachann
 * @since 1.2.3
 */
public final class AdminClientConstants {

    private AdminClientConstants() {
        // Prevent instantiation
    }

    // ==================== Connection Configuration ====================

    /**
     * Maximum retry count for connection attempts
     */
    public static final int CONNECTION_MAX_RETRY_COUNT = 3;

    /**
     * Delay between connection retry attempts (milliseconds)
     */
    public static final long CONNECTION_RETRY_DELAY_MS = 1000;

    /**
     * Connection timeout (milliseconds)
     */
    public static final int CONNECTION_TIMEOUT_MS = 30000;

    // ==================== Request Timeout Configuration ====================

    /**
     * Default request timeout (milliseconds)
     */
    public static final int DEFAULT_REQUEST_TIMEOUT_MS = 30000;

    /**
     * Quick request timeout for lightweight operations (milliseconds)
     */
    public static final int QUICK_REQUEST_TIMEOUT_MS = 5000;

    // ==================== Heartbeat Configuration ====================

    /**
     * Heartbeat interval (seconds)
     */
    public static final long HEARTBEAT_INTERVAL_SECONDS = 30;

    // ==================== Health Check Configuration ====================

    /**
     * Health check interval (milliseconds)
     */
    public static final long HEALTH_CHECK_INTERVAL_MS = 30000;

    /**
     * Maximum failure count before marking node as unhealthy
     */
    public static final int MAX_FAIL_COUNT = 3;

    // ==================== Load Balance Strategies ====================

    /**
     * Round-robin load balance strategy
     */
    public static final String STRATEGY_ROUND_ROBIN = "roundrobin";

    /**
     * Random load balance strategy
     */
    public static final String STRATEGY_RANDOM = "random";

    /**
     * Weighted round-robin load balance strategy
     */
    public static final String STRATEGY_WEIGHTED_ROUND_ROBIN = "weightedroundrobin";

    // ==================== Thread Names ====================

    /**
     * Thread name for heartbeat executor
     */
    public static final String THREAD_NAME_HEARTBEAT = "DynamicTp-AdminClient-Heartbeat";

    /**
     * Thread name for health check executor
     */
    public static final String THREAD_NAME_HEALTH_CHECK = "DynamicTp-AdminCluster-HealthCheck";

    // ==================== Default Values ====================

    /**
     * Default node weight for load balancing
     */
    public static final int DEFAULT_NODE_WEIGHT = 1;

    // ==================== Request Types ====================

    /**
     * Request type for executor monitoring
     */
    public static final String REQUEST_TYPE_EXECUTOR_MONITOR = "executor_monitor";

    /**
     * Request type for executor refresh/configuration update
     */
    public static final String REQUEST_TYPE_EXECUTOR_REFRESH = "executor_refresh";

    /**
     * Request type for alarm management
     */
    public static final String REQUEST_TYPE_ALARM_MANAGE = "alarm_manage";

    /**
     * Request type for log management
     */
    public static final String REQUEST_TYPE_LOG_MANAGE = "log_manage";
}
