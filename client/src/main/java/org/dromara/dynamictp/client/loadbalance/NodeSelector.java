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

import org.dromara.dynamictp.client.cluster.AdminNode;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Node selector interface for load balancing node selection
 *
 * @author eachann
 * @since 1.2.3
 */
public interface NodeSelector {

  /**
   * Select an admin node
   *
   * @param nodes list of available admin nodes
   * @return selected admin node
   */
  AdminNode select(List<AdminNode> nodes);

  /**
   * Filter healthy nodes, return all nodes if no healthy nodes available
   *
   * @param nodes list of all nodes
   * @return list of healthy nodes (or original list if no healthy nodes)
   */
  default List<AdminNode> filterHealthyNodes(List<AdminNode> nodes) {
    if (nodes == null || nodes.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<AdminNode> healthyNodes = nodes.stream()
        .filter(AdminNode::isAvailable)
        .collect(Collectors.toList());
    
    return healthyNodes.isEmpty() ? nodes : healthyNodes;
  }
}

