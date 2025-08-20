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
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.common.em.AdminRequestTypeEnum;

import java.io.Serializable;

/**
 * @author eachann
 */
@Slf4j
public class AdminRequestBody implements Serializable {

    private static final long serialVersionUID = -1288207208017808618L;

    @Getter
    private final long id;

    @Getter
    private final AdminRequestTypeEnum requestType;

    @Setter
    @Getter
    private Object body;

    public AdminRequestBody(long id, AdminRequestTypeEnum requestType) {
        this.id = id;
        this.requestType = requestType;
    }

    public AdminRequestBody(long id, AdminRequestTypeEnum requestType, Object body) {
        this.id = id;
        this.body = body;
        this.requestType = requestType;
    }

}
