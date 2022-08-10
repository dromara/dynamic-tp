package com.dtp.starter.etcd.util;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.config.DtpProperties.Etcd;
import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.core.handler.ConfigHandler;
import com.google.common.collect.Maps;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.watch.WatchEvent;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Redick01
 */
@Slf4j
public final class EtcdUtil {

    private EtcdUtil() { }

    private static Client client;

    /**
     * {@link Client}.
     * @param etcd {@link com.dtp.common.config.DtpProperties.Etcd}
     * @return Client
     */
    public static Client client(DtpProperties.Etcd etcd) {
        if (Objects.isNull(client)) {
            if (!etcd.getAuthEnable()) {
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
     * @param etcd {@link com.dtp.common.config.DtpProperties.Etcd}
     * @param configType config type
     * @return config content
     */
    public static Map<Object, Object> getConfigContent(final DtpProperties.Etcd etcd,
        final String configType) throws ExecutionException, InterruptedException, IOException {
        Map<Object, Object> resultMap = Maps.newHashMap();
        if (ConfigFileTypeEnum.of(configType).equals(ConfigFileTypeEnum.JSON)) {
            KeyValue keyValue = client(etcd)
                    .getKVClient()
                    .get(ByteSequence.from(etcd.getKey(), StandardCharsets.UTF_8))
                    .get()
                    .getKvs()
                    .get(0);
            if (Objects.isNull(keyValue)) {
                return resultMap;
            }
            resultMap = ConfigHandler.getInstance().parseConfig(keyValue.getValue().toString(Charset.forName(etcd.getCharset()))
                    , ConfigFileTypeEnum.of(configType));
        } else if (ConfigFileTypeEnum.of(configType).equals(ConfigFileTypeEnum.PROPERTIES)) {
            List<KeyValue> keyValues = client(etcd)
                    .getKVClient()
                    .get(ByteSequence.from(etcd.getKey(), StandardCharsets.UTF_8))
                    .get()
                    .getKvs();
            Map<Object, Object> finalResultMap = resultMap;
            keyValues.forEach(keyValue -> finalResultMap.put(keyValue.getKey(), keyValue.getValue().toString(Charset.forName(etcd.getCharset()))));
        }
        return resultMap;
    }

    public static Map<Object, Object> watchValMap(final String configType, final List<WatchEvent> events, final DtpProperties dtpProperties)
            throws IOException, ExecutionException, InterruptedException {
        Etcd etcd = dtpProperties.getEtcd();
        Map<Object, Object> properties = Maps.newHashMap();
        if (ConfigFileTypeEnum.of(configType).equals(ConfigFileTypeEnum.JSON)) {
            KeyValue keyValue = events.get(0).getKeyValue();
            properties = ConfigHandler.getInstance().parseConfig(keyValue.getValue().toString(Charset.forName(etcd.getCharset()))
                    , ConfigFileTypeEnum.of(configType));
        } else if (ConfigFileTypeEnum.of(configType).equals(ConfigFileTypeEnum.PROPERTIES)) {
            properties = getConfigContent(dtpProperties.getEtcd(), configType);
        }
        return properties;
    }
}
