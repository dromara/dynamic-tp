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

package org.dromara.dynamictp.client.cluster;

import lombok.Getter;

/**
 * Health checker for admin nodes
 * Separates health check logic from the AdminNode data model
 *
 * @author eachann
 * @since 1.2.3
 */
@Getter
public class AdminNodeHealthChecker {

    private final int maxFailCount;
    private final long healthCheckIntervalMs;

    /**
     * Create a health checker with specified parameters
     *
     * @param maxFailCount          maximum allowed failure count before marking unhealthy
     * @param healthCheckIntervalMs health check interval in milliseconds
     */
    public AdminNodeHealthChecker(int maxFailCount, long healthCheckIntervalMs) {
        this.maxFailCount = maxFailCount;
        this.healthCheckIntervalMs = healthCheckIntervalMs;
    }

    /**
     * Check if the given node is healthy
     *
     * @param node the admin node to check
     * @return true if healthy, false otherwise
     */
    public boolean isHealthy(AdminNode node) {
        if (node == null) {
            return false;
        }

        if (node.getFailCount() >= maxFailCount) {
            node.setAvailable(false);
            return false;
        }

        long timeSinceLastHeartbeat = System.currentTimeMillis() - node.getLastHeartbeatTime();
        if (node.getLastHeartbeatTime() > 0 && timeSinceLastHeartbeat > healthCheckIntervalMs) {
            node.setAvailable(false);
            return false;
        }

        return node.isAvailable();
    }

    /**
     * Mark node as failed and update its state
     *
     * @param node the node to mark as failed
     */
    public void markFailed(AdminNode node) {
        if (node != null) {
            node.markFailed();
            if (node.getFailCount() >= maxFailCount) {
                node.setAvailable(false);
            }
        }
    }

    /**
     * Mark node as successful and reset its state
     *
     * @param node the node to mark as successful
     */
    public void markSuccess(AdminNode node) {
        if (node != null) {
            node.markSuccess();
        }
    }

}
