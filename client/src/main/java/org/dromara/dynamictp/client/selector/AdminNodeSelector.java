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

import org.dromara.dynamictp.client.node.AdminNode;

import java.util.List;

/**
 * Admin节点选择器接口，用于负载均衡选择admin节点
 *
 * @author eachann
 * @since 1.2.3
 */
public interface AdminNodeSelector {

  /**
   * 选择admin节点
   *
   * @param nodes 可用的admin节点列表
   * @param arg   选择参数
   * @return 选中的admin节点
   */
  AdminNode select(List<AdminNode> nodes);
}

