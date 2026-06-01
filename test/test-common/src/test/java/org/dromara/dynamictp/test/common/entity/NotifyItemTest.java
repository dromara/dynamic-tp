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

package org.dromara.dynamictp.test.common.entity;

import com.google.common.collect.Lists;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.entity.NotifyItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * NotifyItemTest related.
 */
class NotifyItemTest {

    @Test
    void testGetAllNotifyItemsReturnsDefaultItemsWithDefaultValues() {
        Map<String, NotifyItem> notifyItems = NotifyItem.getAllNotifyItems().stream()
                .collect(Collectors.toMap(NotifyItem::getType, Function.identity()));

        Assertions.assertEquals(6, notifyItems.size());
        Assertions.assertEquals(1, notifyItems.get(NotifyItemEnum.REJECT.getValue()).getCount());
        Assertions.assertEquals(10, notifyItems.get(NotifyItemEnum.RUN_TIMEOUT.getValue()).getCount());
        Assertions.assertEquals(10, notifyItems.get(NotifyItemEnum.QUEUE_TIMEOUT.getValue()).getCount());
        Assertions.assertEquals(70, notifyItems.get(NotifyItemEnum.LIVENESS.getValue()).getThreshold());
        Assertions.assertEquals(70, notifyItems.get(NotifyItemEnum.CAPACITY.getValue()).getThreshold());
        Assertions.assertEquals(1, notifyItems.get(NotifyItemEnum.CHANGE.getValue()).getSilencePeriod());
    }

    @Test
    void testMergeAllNotifyItemsReturnsDefaultsWhenSourceIsEmpty() {
        List<NotifyItem> notifyItems = NotifyItem.mergeAllNotifyItems(null);

        Assertions.assertEquals(6, notifyItems.size());
    }

    @Test
    void testMergeAllNotifyItemsKeepsConfiguredItemAndAddsMissingDefaults() {
        NotifyItem configured = new NotifyItem();
        configured.setType(NotifyItemEnum.REJECT.getValue());
        configured.setCount(5);

        List<NotifyItem> notifyItems = NotifyItem.mergeAllNotifyItems(Lists.newArrayList(configured));
        Map<String, NotifyItem> itemMap = notifyItems.stream()
                .collect(Collectors.toMap(NotifyItem::getType, Function.identity()));

        Assertions.assertEquals(6, notifyItems.size());
        Assertions.assertSame(configured, itemMap.get(NotifyItemEnum.REJECT.getValue()));
        Assertions.assertEquals(5, itemMap.get(NotifyItemEnum.REJECT.getValue()).getCount());
        Assertions.assertTrue(itemMap.containsKey(NotifyItemEnum.RUN_TIMEOUT.getValue()));
    }

    @Test
    void testMergeAllNotifyItemsPopulatesConfiguredZeroDefaults() {
        NotifyItem configured = new NotifyItem();
        configured.setType(NotifyItemEnum.CAPACITY.getValue());

        NotifyItem merged = NotifyItem.mergeAllNotifyItems(Lists.newArrayList(configured)).stream()
                .filter(item -> NotifyItemEnum.CAPACITY.getValue().equals(item.getType()))
                .findFirst()
                .orElseThrow(AssertionError::new);

        Assertions.assertEquals(70, merged.getThreshold());
        Assertions.assertEquals(1, merged.getCount());
    }
}
