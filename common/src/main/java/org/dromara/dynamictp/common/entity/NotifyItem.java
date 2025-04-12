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
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.dynamictp.common.em.NotifyItemEnum;
import org.dromara.dynamictp.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.dromara.dynamictp.common.util.DefaultValueUtil.setIfZero;

/**
 * NotifyItem related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Data
public class NotifyItem {

    /**
     * If enabled notify.
     */
    private boolean enabled = true;

    /**
     * Notify item, see {@link NotifyItemEnum}
     */
    private String type;

    /**
     * Alarm threshold.
     */
    private int threshold;

    /**
     * Within a cycle window, if the number of occurrences exceeding the threshold reaches count, an alarm will be triggered.
     */
    private int count;

    /**
     * The size of cache in seconds for checking the alarm conditions.
     */
    private int period = 120;

    /**
     * After the alarm is triggered at Time-N (TN), there will be silence during the TN -> TN + silencePeriod.
     */
    private int silencePeriod = 120;

    /**
     * Cluster notify limit.
     */
    private int clusterLimit = 1;

    /**
     * Receivers, split by, If NotifyPlatform.receivers have a value, they will be overwritten by the thread pool alarm
     */
    private String receivers;

    /**
     * Notify platform id
     */
    private List<String> platformIds;

    public static List<NotifyItem> mergeAllNotifyItems(List<NotifyItem> source) {
        if (CollectionUtils.isEmpty(source)) {
            return getAllNotifyItems();
        }
        val configuredTypes = source.stream().map(NotifyItem::getType).collect(toList());
        val defaultItems = getAllNotifyItems().stream()
                .filter(t -> !StringUtil.containsIgnoreCase(t.getType(), configuredTypes))
                .collect(Collectors.toList());
        List<NotifyItem> notifyItems = new ArrayList<>(6);
        notifyItems.addAll(defaultItems);
        populateDefaultValues(source);
        notifyItems.addAll(source);
        return notifyItems;
    }

    public static List<NotifyItem> getAllNotifyItems() {
        NotifyItem rejectNotify = new NotifyItem();
        rejectNotify.setType(NotifyItemEnum.REJECT.getValue());

        NotifyItem runTimeoutNotify = new NotifyItem();
        runTimeoutNotify.setType(NotifyItemEnum.RUN_TIMEOUT.getValue());

        NotifyItem queueTimeoutNotify = new NotifyItem();
        queueTimeoutNotify.setType(NotifyItemEnum.QUEUE_TIMEOUT.getValue());

        List<NotifyItem> notifyItems = new ArrayList<>(6);
        notifyItems.addAll(getSimpleNotifyItems());
        notifyItems.add(rejectNotify);
        notifyItems.add(runTimeoutNotify);
        notifyItems.add(queueTimeoutNotify);

        populateDefaultValues(notifyItems);
        return notifyItems;
    }

    public static List<NotifyItem> getSimpleNotifyItems() {
        NotifyItem changeNotify = new NotifyItem();
        changeNotify.setType(NotifyItemEnum.CHANGE.getValue());
        changeNotify.setSilencePeriod(1);

        NotifyItem livenessNotify = new NotifyItem();
        livenessNotify.setType(NotifyItemEnum.LIVENESS.getValue());

        NotifyItem capacityNotify = new NotifyItem();
        capacityNotify.setType(NotifyItemEnum.CAPACITY.getValue());

        List<NotifyItem> notifyItems = new ArrayList<>(3);
        notifyItems.add(livenessNotify);
        notifyItems.add(changeNotify);
        notifyItems.add(capacityNotify);

        return notifyItems;
    }

    private static void populateDefaultValues(List<NotifyItem> source) {
        if (CollectionUtils.isEmpty(source)) {
            return;
        }
        for (NotifyItem item : source) {
            NotifyItemEnum itemEnum = NotifyItemEnum.of(item.getType());
            switch (Objects.requireNonNull(itemEnum)) {
                case REJECT:
                    setIfZero(item::getCount, item::setCount, 1);
                    break;
                case RUN_TIMEOUT:
                case QUEUE_TIMEOUT:
                    setIfZero(item::getCount, item::setCount, 10);
                    break;
                case LIVENESS:
                case CAPACITY:
                    setIfZero(item::getThreshold, item::setThreshold, 70);
                    setIfZero(item::getCount, item::setCount, 2);
                    break;
                default:
                    break;
            }
        }
    }
}
