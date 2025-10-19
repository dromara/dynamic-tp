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

package org.dromara.dynamictp.client.selector;

import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.client.node.AdminNode;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 加权轮询选择器实现
 *
 * @author eachann
 * @since 1.2.3
 */
@Slf4j
public class WeightedRoundRobinAdminNodeSelector implements AdminNodeSelector {

  private final AtomicInteger currentWeight = new AtomicInteger(0);
  private final AtomicInteger currentIndex = new AtomicInteger(0);

  @Override
  public AdminNode select(List<AdminNode> nodes) {
    if (nodes == null || nodes.isEmpty()) {
      log.warn("No available admin nodes for selection");
      return null;
    }

    // 过滤出健康的节点
    List<AdminNode> healthyNodes = nodes.stream()
        .filter(AdminNode::isAvailable)
        .collect(Collectors.toList());

    if (healthyNodes.isEmpty()) {
      log.warn("No healthy admin nodes available, using all nodes");
      healthyNodes = nodes;
    }

    if (healthyNodes.size() == 1) {
      return healthyNodes.get(0);
    }

    // 计算最大权重
    int maxWeight = healthyNodes.stream()
        .mapToInt(AdminNode::getWeight)
        .max()
        .orElse(1);

    // 计算权重总和
    int weightSum = healthyNodes.stream()
        .mapToInt(AdminNode::getWeight)
        .sum();

    AdminNode selectedNode = null;
    while (selectedNode == null) {
      int current = currentIndex.get();
      int weight = currentWeight.get();

      if (weight == 0) {
        weight = maxWeight;
        currentWeight.set(weight);
      }

      AdminNode node = healthyNodes.get(current);
      if (node.getWeight() >= weight) {
        selectedNode = node;
        currentWeight.addAndGet(-weight);
      } else {
        currentWeight.addAndGet(-node.getWeight());
      }

      currentIndex.set((current + 1) % healthyNodes.size());
    }

    log.debug("WeightedRoundRobin selector selected admin node: {}", selectedNode.getAddress());
    return selectedNode;
  }
}

