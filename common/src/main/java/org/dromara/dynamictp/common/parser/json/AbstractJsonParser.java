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

package org.dromara.dynamictp.common.parser.json;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author topsuder
 * @since 1.1.3
 */
@Slf4j
public abstract class AbstractJsonParser implements JsonParser {

    @Override
    public boolean supports() {
        String[] mapperClassNames = getMapperClassNames();
        for (String mapperClassName : mapperClassNames) {
            try {
                Class.forName(mapperClassName);
            } catch (ClassNotFoundException e) {
                log.warn("the current parser is {}, Can not find class: {}", this.getClass().getSimpleName(), mapperClassName);
                return false;
            }
        }
        return true;
    }

    /**
     * get mapper class name
     *
     * @return mapper class name
     */
    protected abstract String[] getMapperClassNames();
}
