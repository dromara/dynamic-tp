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
import org.dromara.dynamictp.client.node.AdminNode;
import org.dromara.dynamictp.client.selector.AdminNodeSelector;
import org.dromara.dynamictp.client.selector.RoundRobinAdminNodeSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Admin集群管理器
 *
 * @author eachann
 * @since 1.2.3
 */
@Slf4j
public class AdminClusterManager {

  private final List<AdminNode> adminNodes = new CopyOnWriteArrayList<>();
  private final AdminNodeSelector nodeSelector;
  private final ScheduledExecutorService healthCheckExecutor;

  private static final long HEALTH_CHECK_INTERVAL = 30000; // 30秒
  private static final int MAX_FAIL_COUNT = 3;

  public AdminClusterManager() {
    this(new RoundRobinAdminNodeSelector());
  }

  public AdminClusterManager(AdminNodeSelector nodeSelector) {
    this.nodeSelector = nodeSelector;
    this.healthCheckExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread thread = new Thread(r, "DynamicTp-AdminCluster-HealthCheck");
      thread.setDaemon(true);
      return thread;
    });

    startHealthCheck();
  }

  /**
   * 添加admin节点
   *
   * @param ip   节点IP
   * @param port 节点端口
   */
  public void addNode(String ip, int port) {
    addNode(ip, port, 1);
  }

  /**
   * 添加admin节点
   *
   * @param ip     节点IP
   * @param port   节点端口
   * @param weight 节点权重
   */
  public void addNode(String ip, int port, int weight) {
    AdminNode node = new AdminNode(ip, port, weight);
    if (!adminNodes.contains(node)) {
      adminNodes.add(node);
      log.info("Added admin node: {}:{} with weight {}", ip, port, weight);
    }
  }

  /**
   * 移除admin节点
   *
   * @param ip   节点IP
   * @param port 节点端口
   */
  public void removeNode(String ip, int port) {
    adminNodes.removeIf(node -> node.getIp().equals(ip) && node.getPort() == port);
    log.info("Removed admin node: {}:{}", ip, port);
  }

  /**
   * 选择admin节点
   *
   * @param arg 选择参数
   * @return 选中的admin节点
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
   * 获取所有admin节点
   *
   * @return admin节点列表
   */
  public List<AdminNode> getAllNodes() {
    return new ArrayList<>(adminNodes);
  }

  /**
   * 获取健康的admin节点
   *
   * @return 健康的admin节点列表
   */
  public List<AdminNode> getHealthyNodes() {
    return adminNodes.stream()
        .filter(AdminNode::isAvailable)
        .collect(java.util.stream.Collectors.toList());
  }

  /**
   * 标记节点失败
   *
   * @param node 失败的节点
   */
  public void markNodeFailed(AdminNode node) {
    if (node != null) {
      node.markFailed();
      log.warn("Marked admin node as failed: {}", node.getAddress());
    }
  }

  /**
   * 标记节点成功
   *
   * @param node 成功的节点
   */
  public void markNodeSuccess(AdminNode node) {
    if (node != null) {
      node.markSuccess();
      log.debug("Marked admin node as success: {}", node.getAddress());
    }
  }

  /**
   * 启动健康检查
   */
  private void startHealthCheck() {
    healthCheckExecutor.scheduleAtFixedRate(() -> {
      try {
        performHealthCheck();
      } catch (Exception e) {
        log.warn("Health check execution failed", e);
      }
    }, HEALTH_CHECK_INTERVAL, HEALTH_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
  }

  /**
   * 执行健康检查
   */
  private void performHealthCheck() {
    for (AdminNode node : adminNodes) {
      boolean healthy = node.isHealthy(MAX_FAIL_COUNT, HEALTH_CHECK_INTERVAL);
      if (!healthy && node.isAvailable()) {
        log.warn("Admin node {} is marked as unhealthy", node.getAddress());
      }
    }
  }

  /**
   * 关闭集群管理器
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

