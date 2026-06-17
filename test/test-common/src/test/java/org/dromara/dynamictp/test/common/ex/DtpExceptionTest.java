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

package org.dromara.dynamictp.test.common.ex;

import org.dromara.dynamictp.common.ex.DtpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * DtpExceptionTest related.
 */
class DtpExceptionTest {

    @Test
    void testDefaultConstructor() {
        DtpException exception = new DtpException();

        Assertions.assertNull(exception.getMessage());
        Assertions.assertNull(exception.getCause());
    }

    @Test
    void testMessageConstructor() {
        DtpException exception = new DtpException("failed");

        Assertions.assertEquals("failed", exception.getMessage());
        Assertions.assertNull(exception.getCause());
    }

    @Test
    void testMessageAndCauseConstructor() {
        IllegalStateException cause = new IllegalStateException("cause");

        DtpException exception = new DtpException("failed", cause);

        Assertions.assertEquals("failed", exception.getMessage());
        Assertions.assertSame(cause, exception.getCause());
    }

    @Test
    void testCauseConstructor() {
        IllegalArgumentException cause = new IllegalArgumentException("cause");

        DtpException exception = new DtpException(cause);

        Assertions.assertSame(cause, exception.getCause());
        Assertions.assertTrue(exception.getMessage().contains("cause"));
    }
}
