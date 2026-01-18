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

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.client.AdminClientConstants;
import org.dromara.dynamictp.client.loadbalance.NodeSelector;
import org.dromara.dynamictp.client.loadbalance.RoundRobinNodeSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Admin cluster manager for managing admin nodes and health checks
 *
 * @author eachann
 * @since 1.2.3
 */
@Slf4j
public class AdminClusterManager {

  private final List<AdminNode> adminNodes = new CopyOnWriteArrayList<>();
  private final NodeSelector nodeSelector;
  private final AdminNodeHealthChecker healthChecker;
  private final ScheduledExecutorService healthCheckExecutor;

  public AdminClusterManager() {
    this(new RoundRobinNodeSelector());
  }

  public AdminClusterManager(NodeSelector nodeSelector) {
    this.nodeSelector = nodeSelector;
    this.healthChecker = new AdminNodeHealthChecker(
        AdminClientConstants.MAX_FAIL_COUNT,
        AdminClientConstants.HEALTH_CHECK_INTERVAL_MS
    );
    this.healthCheckExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread thread = new Thread(r, AdminClientConstants.THREAD_NAME_HEALTH_CHECK);
      thread.setDaemon(true);
      return thread;
    });

    startHealthCheck();
  }

  /**
   * Add admin node with default weight
   *
   * @param ip   node IP address
   * @param port node port
   */
  public void addNode(String ip, int port) {
    addNode(ip, port, 1);
  }

  /**
   * Add admin node with specified weight
   *
   * @param ip     node IP address
   * @param port   node port
   * @param weight node weight for load balancing
   */
  public void addNode(String ip, int port, int weight) {
    AdminNode node = new AdminNode(ip, port, weight);
    if (!adminNodes.contains(node)) {
      adminNodes.add(node);
      log.info("Added admin node: {}:{} with weight {}", ip, port, weight);
    }
  }

  /**
   * Remove admin node from cluster
   *
   * @param ip   node IP address
   * @param port node port
   */
  public void removeNode(String ip, int port) {
    adminNodes.removeIf(node -> node.getIp().equals(ip) && node.getPort() == port);
    log.info("Removed admin node: {}:{}", ip, port);
  }

  /**
   * Select an admin node from the cluster using the configured selector
   *
   * @param arg selection argument (for extensibility)
   * @return selected admin node, or null if no nodes available
   */
  public AdminNode selectNode(Object arg) {
    if (adminNodes.isEmpty()) {
      log.warn("No admin nodes available");
      return null;
    }

    AdminNode selectedNode = nodeSelector.select(adminNodes);
    if (selectedNode != null) {
      log.debug("Selected admin node: {}", selectedNode.getAddress());
    }
    return selectedNode;
  }

  /**
   * Get all admin nodes in the cluster
   *
   * @return list of all admin nodes
   */
  public List<AdminNode> getAllNodes() {
    return new ArrayList<>(adminNodes);
  }

  /**
   * Get all healthy admin nodes in the cluster
   *
   * @return list of healthy admin nodes
   */
  public List<AdminNode> getHealthyNodes() {
    return adminNodes.stream()
        .filter(AdminNode::isAvailable)
        .collect(java.util.stream.Collectors.toList());
  }

  /**
   * Mark a node as failed
   *
   * @param node the failed node
   */
  public void markNodeFailed(AdminNode node) {
    if (node != null) {
      node.markFailed();
      log.warn("Marked admin node as failed: {}", node.getAddress());
    }
  }

  /**
   * Mark a node as successful
   *
   * @param node the successful node
   */
  public void markNodeSuccess(AdminNode node) {
    if (node != null) {
      node.markSuccess();
      log.debug("Marked admin node as success: {}", node.getAddress());
    }
  }

  /**
   * Start the periodic health check task
   */
  private void startHealthCheck() {
    long interval = AdminClientConstants.HEALTH_CHECK_INTERVAL_MS;
    healthCheckExecutor.scheduleAtFixedRate(() -> {
      try {
        performHealthCheck();
      } catch (Exception e) {
        log.warn("Health check execution failed", e);
      }
    }, interval, interval, TimeUnit.MILLISECONDS);
  }

  /**
   * Perform health check on all nodes
   */
  private void performHealthCheck() {
    for (AdminNode node : adminNodes) {
      boolean healthy = healthChecker.isHealthy(node);
      if (!healthy && node.isAvailable()) {
        log.warn("Admin node {} is marked as unhealthy", node.getAddress());
      }
    }
  }

  /**
   * Shutdown the cluster manager and stop health checks
   */
  public void shutdown() {
    if (healthCheckExecutor != null && !healthCheckExecutor.isShutdown()) {
      try {
        healthCheckExecutor.shutdown();
        if (!healthCheckExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
          healthCheckExecutor.shutdownNow();
        }
        log.info("Admin cluster manager health check stopped");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        healthCheckExecutor.shutdownNow();
        log.warn("Admin cluster manager shutdown interrupted");
      }
    }
  }
}

