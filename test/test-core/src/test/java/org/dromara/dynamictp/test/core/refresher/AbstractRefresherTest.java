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

package org.dromara.dynamictp.test.core.refresher;

import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.core.refresher.AbstractRefresher;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.springframework.test.context.junit4.SpringRunner;

/**
 * AbstractRefresherTest related
 *
 * @author vzer200
 * @since 1.1.8
 */
@RunWith(SpringRunner.class)
public class AbstractRefresherTest {
    private AbstractRefresher refresher;
    private DtpProperties dtpProperties;

    @Before
    public void setUp(){
        dtpProperties = DtpProperties.getInstance();
        refresher = Mockito.spy(new AbstractRefresher(dtpProperties) {
            @Override
            protected void doRefresh(DtpProperties properties) {
            }
        });
    }

    @Test
    public void testRefresh(){
        String initialEnv = dtpProperties.getEnv();
        System.out.println("Initial env: " + initialEnv);

        mockConfigChange();

        String refreshedEnv = dtpProperties.getEnv();
        System.out.println("Refreshed env: " + refreshedEnv);
        Assertions.assertEquals("newEnvValue", refreshedEnv);
    }

    private void mockConfigChange() {
        String content =
                "dynamictp:\n" +
                "  enabled: true\n" +
                "  env: newEnvValue";

        refresher.refresh(content, ConfigFileTypeEnum.YAML);
    }
}

