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

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Admin node information
 *
 * @author eachann
 * @since 1.2.3
 */
@Data
@EqualsAndHashCode(of = {"ip", "port"})
public class AdminNode {

  /**
   * Node IP address
   */
  private String ip;

  /**
   * Node port
   */
  private int port;

  /**
   * Node weight for weighted load balancing
   */
  private int weight = 1;

  /**
   * Whether the node is available
   */
  private boolean available = true;

  /**
   * Last heartbeat timestamp
   */
  private long lastHeartbeatTime;

  /**
   * Connection failure count
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
   * Get the node address string
   *
   * @return node address in format "ip:port"
   */
  public String getAddress() {
    return ip + ":" + port;
  }

  /**
   * Mark node as failed and increment failure count
   */
  public void markFailed() {
    this.failCount++;
    this.lastHeartbeatTime = System.currentTimeMillis();
  }

  /**
   * Mark node as successful and reset failure count
   */
  public void markSuccess() {
    this.failCount = 0;
    this.lastHeartbeatTime = System.currentTimeMillis();
    this.available = true;
  }

  /**
   * Check if node is healthy based on failure count and heartbeat interval
   *
   * @param maxFailCount        maximum allowed failure count
   * @param healthCheckInterval health check interval in milliseconds
   * @return true if node is healthy, false otherwise
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

