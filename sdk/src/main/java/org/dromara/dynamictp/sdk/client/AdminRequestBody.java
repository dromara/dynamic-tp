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

package org.dromara.dynamictp.sdk.client;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

@Slf4j
public class AdminRequestBody implements Serializable {

    private static final long  serialVersionUID = -1288207208017808618L;

    @Getter
    private final long id;

    @Getter
    private final AdminRequestTypeEnum requestType;

    private byte[] body;

    private final SnowflakeGenerator idGenerator = new SnowflakeGenerator();

    public AdminRequestBody(AdminRequestTypeEnum requestType) {
        this.id = idGenerator.next();
        this.requestType = requestType;
    }

    public AdminRequestBody(AdminRequestTypeEnum requestType, Object body) {
        this.id = idGenerator.next();
        serializeBody(body);
        this.requestType = requestType;
    }

    public void serializeBody(Object object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output  objectOutputStream = new Hessian2Output(byteArrayOutputStream);
        try {
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        } catch (IOException e) {
            log.warn("DynamicTp admin client serialize failed.");
        }
        this.body = byteArrayOutputStream.toByteArray();
    }

    public Object deserializeBody() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        Hessian2Input   objectOutputStream = new Hessian2Input(byteArrayInputStream);
        Object object = null;
        try {
            object = objectOutputStream.readObject();
        } catch (IOException e) {
            log.warn("DynamicTp admin client deserialize failed.");
        }
        return object;
    }

}
