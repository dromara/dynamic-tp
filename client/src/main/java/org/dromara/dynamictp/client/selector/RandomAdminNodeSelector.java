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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 随机选择器实现
 *
 * @author eachann
 * @since 1.2.3
 */
@Slf4j
public class RandomAdminNodeSelector implements AdminNodeSelector {

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

    int index = ThreadLocalRandom.current().nextInt(healthyNodes.size());
    AdminNode selectedNode = healthyNodes.get(index);

    log.debug("Random selector selected admin node: {}", selectedNode.getAddress());
    return selectedNode;
  }
}

