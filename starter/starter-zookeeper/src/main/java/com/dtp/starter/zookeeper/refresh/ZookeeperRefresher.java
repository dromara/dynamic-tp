package com.dtp.starter.zookeeper.refresh;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.core.refresh.AbstractRefresher;
import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.WatchedEvent;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Redick01
 */
@Slf4j
public class ZookeeperRefresher extends AbstractRefresher implements InitializingBean {

    @Resource
    private DtpProperties dtpProperties;

    private CuratorFramework curatorFramework;

    @Override
    public void afterPropertiesSet() {
        DtpProperties.Zookeeper zookeeper = dtpProperties.getZookeeper();
        this.curatorFramework = CuratorFrameworkFactory.newClient(zookeeper.getZkConnectStr(), new ExponentialBackoffRetry(1000, 3));
        this.curatorFramework.start();
        String nodePath = ZKPaths.makePath(ZKPaths.makePath(zookeeper.getRootNode(), zookeeper.getConfigVersion()), zookeeper.getNode());
        curatorFramework.getCuratorListenable().addListener((client, event) -> {
            final WatchedEvent watchedEvent = event.getWatchedEvent();
            if (null != watchedEvent) {
                switch (watchedEvent.getType()) {
                    case NodeChildrenChanged:
                    case NodeDataChanged:
                        loadNode(nodePath);
                        break;
                    default:
                        break;
                }
            }
        });
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        this.curatorFramework.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.CONNECTED) {
                    loadNode(nodePath);
                    countDownLatch.countDown();
                } else if (newState == ConnectionState.RECONNECTED) {
                    loadNode(nodePath);
                }
            }
        });
        log.info("DynamicTp refresher, add listener success, nodePath: {}", nodePath);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("zk connection state listener countDownLatch InterruptedException", e);
        }
    }

    /**
     * load config and refresh
     * @param nodePath config path
     */
    public void loadNode(String nodePath) {
        try {
            final GetChildrenBuilder childrenBuilder = curatorFramework.getChildren();
            final List<String> children = childrenBuilder.watched().forPath(nodePath);
            StringBuilder content = new StringBuilder();
            children.forEach(c -> {
                String n = ZKPaths.makePath(nodePath, c);
                final String nodeName = ZKPaths.getNodeFromPath(n);
                final GetDataBuilder data = curatorFramework.getData();
                String value = "";
                try {
                    value = new String(data.watched().forPath(n), Charsets.UTF_8);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final Pair<String, String> keyValue = new ImmutablePair<>(nodeName, value);
                content.append(keyValue.getKey()).append("=").append(keyValue.getValue()).append("\n");
            });
            refresh(content.toString(), ConfigFileTypeEnum.PROPERTIES);
        } catch (Exception e) {
            log.error("load zk node error, nodePath is {}", nodePath, e);
        }
    }
}
