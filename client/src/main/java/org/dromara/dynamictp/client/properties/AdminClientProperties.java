/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You Under the Apache License, Version 2.0
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

package org.dromara.dynamictp.client.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for DynamicTp admin client.
 * <p>
 * Binds to existing keys under {@code dynamictp.*} to keep backward compatibility.
 *
 * @author yanhom
 * @since 1.2.4
 */
@Data
@ConfigurationProperties(prefix = "dynamictp.admin")
public class AdminClientProperties {

    /**
     * Whether enable admin feature.
     */
    private boolean enabled = false;

    /**
     * Admin server nodes, separated by comma.
     * Format: ip:port[:weight]
     */
    private String nodes;

    /**
     * Load balance strategy for selecting admin node.
     */
    private String loadBalanceStrategy = "roundRobin";

    /**
     * Client name, default from spring.application.name.
     */
    private String clientName;

    /**
     * Service name, default from spring.application.name.
     */
    private String serviceName;
}
