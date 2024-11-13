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
import lombok.EqualsAndHashCode;

/**
 * ClassName: VTExecutorStats
 * Package: org.dromara.dynamictp.common.entity
 * Description:
 *
 * @author CYC
 * @create 2024/11/4 16:52
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VTExecutorStats extends Metrics {

    /**
     * 虚拟线程执行器名字
     */
    private String executorName;

    /**
     * 虚拟线程执行器别名
     */
    private String executorAliasName;

}
