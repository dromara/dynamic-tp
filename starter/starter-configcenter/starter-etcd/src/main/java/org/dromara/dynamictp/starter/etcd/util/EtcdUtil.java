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

package org.dromara.dynamictp.starter.etcd.util;

import org.dromara.dynamictp.common.em.ConfigFileTypeEnum;
import org.dromara.dynamictp.common.properties.DtpProperties;
import org.dromara.dynamictp.common.properties.DtpProperties.Etcd;
import org.dromara.dynamictp.core.handler.ConfigHandler;
import org.dromara.dynamictp.starter.etcd.refresher.EtcdListener;
import org.dromara.dynamictp.starter.etcd.refresher.EtcdRefresher;
import com.google.common.collect.Maps;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Redick01
 */
@Slf4j
public final class EtcdUtil {

    private static Map<Object, Object> resultMap = Maps.newHashMap();

    private EtcdUtil() {
    }

    private static Client client;

    /**
     * {@link Client}.
     *
     * @param etcd {@link DtpProperties.Etcd}
     * @return Client
     */
    public static Client client(DtpProperties.Etcd etcd) {
        if (Objects.isNull(client)) {
            if (StringUtils.isBlank(etcd.getEndpoints())) {
                throw new IllegalArgumentException("Etcd endpoints is null, please check your config.");
            }
            if (!etcd.isAuthEnable()) {
                client = Client.builder()
                        .endpoints(etcd.getEndpoints().split(","))
                        .build();
            } else {
                client = Client.builder()
                        .endpoints(etcd.getEndpoints().split(","))
                        .user(ByteSequence.from(etcd.getUser(), Charset.forName(etcd.getCharset())))
                        .password(ByteSequence.from(etcd.getPassword(), Charset.forName(etcd.getCharset())))
                        .authority(etcd.getAuthority())
                        .build();
            }
        }
        return client;
    }

    /**
     * get config content.
     *
     * @param etcd       {@link DtpProperties.Etcd}
     * @param configType config type
     * @return config content
     */
    public static Map<Object, Object> getConfigMap(final DtpProperties.Etcd etcd, final String configType) {
        if (StringUtils.isBlank(etcd.getKey())) {
            log.debug("dynamic tp etcd config key is null.");
            return resultMap;
        }
        try {
            if (ConfigFileTypeEnum.of(configType).equals(ConfigFileTypeEnum.JSON)) {
                KeyValue keyValue = client(etcd)
                        .getKVClient()
                        .get(bytesOf(etcd.getKey()))
                        .get(etcd.getTimeout(), TimeUnit.MILLISECONDS)
                        .getKvs()
                        .get(0);
                if (Objects.isNull(keyValue)) {
                    return resultMap;
                }
                resultMap = ConfigHandler.getInstance().parseConfig(keyValue.getValue().toString(Charset.forName(etcd.getCharset())),
                        ConfigFileTypeEnum.of(configType));
            } else if (ConfigFileTypeEnum.of(configType).equals(ConfigFileTypeEnum.PROPERTIES)) {
                ByteSequence key = bytesOf(etcd.getKey());
                GetOption getOption = GetOption.newBuilder().withPrefix(key).build();
                GetResponse response = client(etcd)
                        .getKVClient()
                        .get(key, getOption)
                        .get(etcd.getTimeout(), TimeUnit.MILLISECONDS);
                List<KeyValue> keyValues = response.getKvs();
                Map<Object, Object> finalResultMap = resultMap;
                keyValues.forEach(keyValue -> {
                    String realKey = getRealKey(etcd.getKey(), keyValue);
                    finalResultMap.put(realKey, keyValue.getValue().toString(Charset.forName(etcd.getCharset())));
                });
            }
        } catch (Exception e) {
            log.error("get config info from etcd exception.", e);
        }
        return resultMap;
    }

    /**
     * init config watcher.
     *
     * @param etcdRefresher {@link EtcdRefresher}
     * @param dtpProperties {@link DtpProperties}
     * @param map           get watch key from the map
     */
    public static void initWatcher(final EtcdRefresher etcdRefresher, final DtpProperties dtpProperties,
                                   final Map<Object, Object> map) {
        if (null != client) {
            String configType = dtpProperties.getConfigType();
            if (ConfigFileTypeEnum.of(configType).equals(ConfigFileTypeEnum.JSON)) {
                String key = dtpProperties.getEtcd().getKey();
                client.getWatchClient().watch(ByteSequence.from(key, StandardCharsets.UTF_8),
                        new EtcdListener(dtpProperties, key, etcdRefresher));
            } else if (ConfigFileTypeEnum.of(configType).equals(ConfigFileTypeEnum.PROPERTIES)) {
                map.forEach((k, v) -> {
                    String key = dtpProperties.getEtcd().getKey().endsWith("/") ? dtpProperties.getEtcd().getKey()
                            : dtpProperties.getEtcd().getKey() + "/" + k;
                    client.getWatchClient().watch(ByteSequence.from(key, StandardCharsets.UTF_8),
                            new EtcdListener(dtpProperties, key, etcdRefresher));
                });
            }
        }
    }

    /**
     * etcd watcher get val.
     *
     * @param configType    config type
     * @param events        {@link WatchEvent}
     * @param dtpProperties {@link DtpProperties}
     */
    public static Map<Object, Object> watchValMap(final String configType, final List<WatchEvent> events, final DtpProperties dtpProperties)
            throws IOException {
        Etcd etcd = dtpProperties.getEtcd();
        if (ConfigFileTypeEnum.of(configType).equals(ConfigFileTypeEnum.JSON)) {
            KeyValue keyValue = events.get(0).getKeyValue();
            resultMap = ConfigHandler.getInstance().parseConfig(keyValue.getValue().toString(Charset.forName(etcd.getCharset())),
                    ConfigFileTypeEnum.of(configType));
        } else if (ConfigFileTypeEnum.of(configType).equals(ConfigFileTypeEnum.PROPERTIES)) {
            events.forEach(event -> {
                KeyValue keyValue = event.getKeyValue();
                String realKey = getRealKey(etcd.getKey(), keyValue);
                resultMap.put(realKey, keyValue.getValue().toString(Charset.forName(etcd.getCharset())));
            });
        }
        return resultMap;
    }

    private static ByteSequence bytesOf(final String val) {
        return ByteSequence.from(val, StandardCharsets.UTF_8);
    }

    private static String getRealKey(final String key, KeyValue keyValue) {
        String configKey = key;
        if (!configKey.endsWith("/")) {
            configKey += "/";
        }
        return StringUtils.removeStart(keyValue.getKey().toString(StandardCharsets.UTF_8), configKey);
    }

    /**
     * close etcd client.
     */
    public static void close() {
        if (Objects.nonNull(client)) {
            client.close();
        }
    }

}
