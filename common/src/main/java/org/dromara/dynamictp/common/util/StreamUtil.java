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

package org.dromara.dynamictp.common.util;

import com.google.common.base.Preconditions;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * StreamUtil related
 *
 * @author yanhom
 * @since 1.0.0
 **/
public final class StreamUtil {

    private StreamUtil() { }

    /**
     * Fetches id to list.
     *
     * @param data    data collection
     * @param mapping calculate the id in data list
     * @param <I>     id type
     * @param <T>     data type
     * @return a list of id
     */
    public static <I, T> List<I> fetchProperty(Collection<T> data,
                                               Function<T, I> mapping) {
        Preconditions.checkNotNull(mapping, "mapping function must not be null");
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        return data.stream().map(mapping).collect(Collectors.toList());
    }

    /**
     * Converts to map (key from the list data)
     *
     * @param coll data list
     * @param key  key mapping function
     * @param <O>  id type
     * @param <P>  data type
     * @return a map which key from list data and value is data
     */
    public static <P, O> Map<O, P> toMap(Collection<P> coll, Function<P, O> key) {
        Preconditions.checkNotNull(key, "key function must not be null");
        if (CollectionUtils.isEmpty(coll)) {
            return Collections.emptyMap();
        }

        return coll.stream().collect(Collectors.toMap(key, Function.identity(), (v1, v2) -> v2));
    }

    /**
     * Converts to map (key from the list data)
     *
     * @param list  data list
     * @param key   key mapping function
     * @param value value mapping function
     * @param <O>   id type
     * @param <D>   data type
     * @param <P>   value type
     * @return a map which key from list data and value is data
     */
    public static <O, D, P> Map<O, P> toMap(Collection<D> list,
                                            Function<D, O> key,
                                            Function<D, P> value) {
        Preconditions.checkNotNull(key, "Key function must not be null");
        Preconditions.checkNotNull(value, "Value function must not be null");
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.toMap(key, value, (v1, v2) -> v2));
    }

    /**
     * Converts a list to a list map where list contains id in ids.
     *
     * @param ids  id collection
     * @param list data list
     * @param key  calculate the id in data list
     * @param <I>  id type
     * @param <D>  data type
     * @return a map which key is in ids and value containing in list
     */
    public static <I, D> Map<I, List<D>> toListMap(Collection<I> ids,
                                                   Collection<D> list,
                                                   Function<D, I> key) {
        Preconditions.checkNotNull(key, "mapping function must not be null");
        if (CollectionUtils.isEmpty(ids) || CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Map<I, List<D>> resultMap = list.stream().collect(Collectors.groupingBy(key));
        ids.forEach(id -> resultMap.putIfAbsent(id, Collections.emptyList()));
        return resultMap;
    }

}
