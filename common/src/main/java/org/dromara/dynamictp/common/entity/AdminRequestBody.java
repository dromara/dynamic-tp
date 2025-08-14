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

import com.alipay.remoting.exception.CodecException;
import com.alipay.remoting.serialization.HessianSerializer;
import lombok.Getter;
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

    private byte[] body;

    public AdminRequestBody(long id, AdminRequestTypeEnum requestType) {
        this.id = id;
        this.requestType = requestType;
    }

    public AdminRequestBody(long id, AdminRequestTypeEnum requestType, Object body) {
        this.id = id;
        serializeBody(body);
        this.requestType = requestType;
    }

    public void serializeBody(Object object) {
        HessianSerializer serializer = new HessianSerializer();
        try {
            this.body = serializer.serialize(object);
        } catch (CodecException e) {
            log.error("DynamicTp admin client serialize failed.", e);
        }
    }

    public Object deserializeBody() {
        HessianSerializer serializer = new HessianSerializer();
        Object object = null;
        try {
            object = serializer.deserialize(this.body, null);
        } catch (CodecException e) {
            log.error("DynamicTp admin client deserialize failed.", e);
        }
        return object;
    }

}
