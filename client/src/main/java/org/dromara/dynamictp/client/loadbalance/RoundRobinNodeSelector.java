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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round-robin selector implementation
 *
 * @author eachann
 * @since 1.2.3
 */
@Slf4j
public class RoundRobinNodeSelector implements NodeSelector {

  private final AtomicInteger counter = new AtomicInteger(0);

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

    // Use bitwise AND to ensure positive index, avoid array out of bounds due to integer overflow
    int index = (counter.getAndIncrement() & Integer.MAX_VALUE) % healthyNodes.size();
    AdminNode selectedNode = healthyNodes.get(index);

    log.debug("RoundRobin selector selected admin node: {}", selectedNode.getAddress());
    return selectedNode;
  }
}
