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

package org.dromara.dynamictp.common.entity;

import lombok.Data;

import java.util.UUID;

/**
 * NotifyPlatform related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
public class NotifyPlatform {

    /**
     * Notify platform id.
     */
    private String platformId = UUID.randomUUID().toString();

    /**
     * Notify platform name.
     */
    private String platform;

    /**
     * Token of url.
     */
    private String urlKey;

    /**
     * Secret, may be null.
     */
    private String secret;

    /**
     * webhook, may be null.
     */
    private String webhook;

    /**
     * Receivers, split by ,
     */
    private String receivers = "all";

    /**
     * http请求超时时间，单位（毫秒）<br>
     * 默认3000毫秒
     */
    private Integer timeout = 3000;
}
