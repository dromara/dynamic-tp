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

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * RPC request body for admin communication.
 *
 * @author eachann
 */
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = -1288207208017808618L;

    @Getter
    private final long id;

    @Getter
    private final String requestType;

    @Setter
    @Getter
    private Object body;

    private final Map<String, String> attributes = new HashMap<>();

    public RpcRequest(long id, String requestType) {
        this.id = id;
        this.requestType = requestType;
    }

    public RpcRequest(long id, String requestType, Object body) {
        this(id, requestType);
        this.body = body;
    }

    /**
     * Get attributes as unmodifiable map
     */
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Add an attribute
     */
    public RpcRequest addAttribute(String key, String value) {
        this.attributes.put(key, value);
        return this;
    }
}
