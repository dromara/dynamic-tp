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

package org.dromara.dynamictp.client.node;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Admin节点信息
 *
 * @author eachann
 * @since 1.2.3
 */
@Data
@EqualsAndHashCode
public class AdminNode {

  /**
   * 节点IP地址
   */
  private String ip;

  /**
   * 节点端口
   */
  private int port;

  /**
   * 节点权重，用于加权负载均衡
   */
  private int weight = 1;

  /**
   * 节点是否可用
   */
  private boolean available = true;

  /**
   * 节点最后心跳时间
   */
  private long lastHeartbeatTime;

  /**
   * 节点连接失败次数
   */
  private int failCount = 0;

  public AdminNode(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  public AdminNode(String ip, int port, int weight) {
    this.ip = ip;
    this.port = port;
    this.weight = weight;
  }

  /**
   * 获取节点地址字符串
   *
   * @return 节点地址
   */
  public String getAddress() {
    return ip + ":" + port;
  }

  /**
   * 标记节点失败
   */
  public void markFailed() {
    this.failCount++;
    this.lastHeartbeatTime = System.currentTimeMillis();
  }

  /**
   * 标记节点成功
   */
  public void markSuccess() {
    this.failCount = 0;
    this.lastHeartbeatTime = System.currentTimeMillis();
    this.available = true;
  }

  /**
   * 检查节点是否健康
   *
   * @param maxFailCount        最大失败次数
   * @param healthCheckInterval 健康检查间隔（毫秒）
   * @return 是否健康
   */
  public boolean isHealthy(int maxFailCount, long healthCheckInterval) {
    if (failCount >= maxFailCount) {
      this.available = false;
      return false;
    }

    if (System.currentTimeMillis() - lastHeartbeatTime > healthCheckInterval) {
      this.available = false;
      return false;
    }

    return this.available;
  }
}

