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

package org.dromara.dynamictp.test.extension.agent;

public class MyAgentWrapperTwoRunnable implements Runnable {

    private Runnable r1;

    private Runnable r2;

    private Object busiObj;

    public MyAgentWrapperTwoRunnable(Runnable r1, Runnable r2, Object busiObj) {
        this.r1 = r1;
        this.r2 = r2;
        this.busiObj = busiObj;
    }

    @Override
    public void run() {
        System.out.println("before");
        try {
            r1.run();
            r2.run();
        } finally {
            System.out.println("after");
        }
    }
}
