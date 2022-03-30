package com.dtp.starter.zookeeper.util;

import cn.hutool.core.map.MapUtil;
import com.dtp.common.config.DtpProperties;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * CuratorUtil related
 *
 * @author: yanhom
 * @since 1.0.4
 **/
@Slf4j
public class CuratorUtil {

    private static CuratorFramework curatorFramework;

    private static final CountDownLatch COUNT_DOWN_LATCH = new CountDownLatch(1);

    private CuratorUtil() {}

    public static CuratorFramework getCuratorFramework(DtpProperties dtpProperties) {
        if (curatorFramework == null) {
            DtpProperties.Zookeeper zookeeper = dtpProperties.getZookeeper();
            curatorFramework = CuratorFrameworkFactory.newClient(zookeeper.getZkConnectStr(),
                    new ExponentialBackoffRetry(1000, 3));
            final ConnectionStateListener connectionStateListener = (client, newState) -> {
                if (newState == ConnectionState.CONNECTED) {
                    COUNT_DOWN_LATCH.countDown();
                }};
            curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);
            curatorFramework.start();
            try {
                COUNT_DOWN_LATCH.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
        return curatorFramework;
    }

    public static String nodePath(DtpProperties dtpProperties) {
        DtpProperties.Zookeeper zookeeper = dtpProperties.getZookeeper();
        return ZKPaths.makePath(ZKPaths.makePath(zookeeper.getRootNode(),
                zookeeper.getConfigVersion()), zookeeper.getNode());
    }

    public static Map<String, String> genPropertiesMap(CuratorFramework curatorFramework, String nodePath) {
        try {
            final GetChildrenBuilder childrenBuilder = curatorFramework.getChildren();
            final List<String> children = childrenBuilder.watched().forPath(nodePath);
            Map<String, String> properties = Maps.newHashMap();
            children.forEach(c -> {
                String n = ZKPaths.makePath(nodePath, c);
                final String nodeName = ZKPaths.getNodeFromPath(n);
                final GetDataBuilder data = curatorFramework.getData();
                String value = "";
                try {
                    value = new String(data.watched().forPath(n), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.error("zk config value watched exception.", e);
                }
                properties.put(nodeName, value);
            });
            return properties;
        } catch (Exception e) {
            log.error("load zk node error, nodePath is {}", nodePath, e);
            return Maps.newHashMapWithExpectedSize(0);
        }
    }

    public static String propertiesContent(CuratorFramework curatorFramework, String nodePath) {
        Map<String, String> propertiesMap = genPropertiesMap(curatorFramework, nodePath);
        if (MapUtil.isEmpty(propertiesMap)) {
            return null;
        }
        StringBuilder content = new StringBuilder();
        propertiesMap.forEach((k, v) -> content.append(k).append("=").append(v).append("\n"));
        return content.toString();
    }
}
