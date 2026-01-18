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

package org.dromara.dynamictp.client.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.client.cluster.AdminNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Weighted round-robin selector implementation (smooth weighted round-robin algorithm)
 *
 * @author eachann
 * @since 1.2.3
 */
@Slf4j
public class WeightedRoundRobinNodeSelector implements NodeSelector {

  /**
   * Current weight for each node
   */
  private final Map<String, Integer> currentWeights = new ConcurrentHashMap<>();

  @Override
  public AdminNode select(List<AdminNode> nodes) {
    if (CollectionUtils.isEmpty(nodes)) {
      log.warn("No available admin nodes for selection");
      return null;
    }

    // Use interface default method to filter healthy nodes
    List<AdminNode> healthyNodes = filterHealthyNodes(nodes);
    if (healthyNodes.isEmpty()) {
      log.warn("No healthy admin nodes available");
      return null;
    }

    if (healthyNodes.size() == 1) {
      return healthyNodes.get(0);
    }

    // Calculate total weight
    int totalWeight = healthyNodes.stream()
            .mapToInt(AdminNode::getWeight)
            .sum();

    // Smooth weighted round-robin algorithm
    AdminNode selectedNode = null;
    int maxCurrentWeight = Integer.MIN_VALUE;

    for (AdminNode node : healthyNodes) {
      String nodeKey = node.getAddress();

      // Get current weight, initialize to 0 if not exists
      int currentWeight = currentWeights.getOrDefault(nodeKey, 0);

      // Current weight += node weight
      currentWeight += node.getWeight();
      currentWeights.put(nodeKey, currentWeight);

      // Select node with max current weight
      if (currentWeight > maxCurrentWeight) {
        maxCurrentWeight = currentWeight;
        selectedNode = node;
      }
    }

    // Selected node's current weight -= total weight
    if (selectedNode != null) {
      String selectedKey = selectedNode.getAddress();
      currentWeights.put(selectedKey, currentWeights.get(selectedKey) - totalWeight);
    }

    log.debug("WeightedRoundRobin selector selected admin node: {}",
            selectedNode != null ? selectedNode.getAddress() : "null");
    return selectedNode;
  }
}

