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

package org.dromara.dynamictp.example.cmp;

import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.dromara.dynamictp.example.ctx.CusCtx;
import org.springframework.stereotype.Component;

/**
 * CCmp related
 *
 * @author yanhom
 * @since 1.1.9
 */
@Slf4j
@Component("c")
public class CCmp extends NodeComponent {

    @Override
    public void process() {
        CusCtx ctx = this.getFirstContextBean();
        log.info("c, ctx:{}", ctx);
    }
}

