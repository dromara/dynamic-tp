package com.dtp.starter.zookeeper.refresh;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.core.refresh.AbstractRefresher;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.WatchedEvent;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Redick01
 */
@Slf4j
public class ZookeeperRefresher extends AbstractRefresher implements InitializingBean {

    @Resource
    private DtpProperties dtpProperties;

    private CuratorFramework curatorFramework;

    @Override
    public void afterPropertiesSet() throws Exception {

        DtpProperties.Zookeeper zookeeper = dtpProperties.getZookeeper();
        curatorFramework = CuratorFrameworkFactory.newClient(zookeeper.getZkConnectStr(),
                new ExponentialBackoffRetry(1000, 3));
        String nodePath = ZKPaths.makePath(ZKPaths.makePath(zookeeper.getRootNode(),
                zookeeper.getConfigVersion()), zookeeper.getNode());

        final ConnectionStateListener connectionStateListener = (client, newState) -> {
            if (newState == ConnectionState.CONNECTED) {
                loadNode(nodePath);
            } else if (newState == ConnectionState.RECONNECTED) {
                loadNode(nodePath);
            }};

        final CuratorListener curatorListener = (client, curatorEvent) -> {
            final WatchedEvent watchedEvent = curatorEvent.getWatchedEvent();
            if (null != watchedEvent) {
                switch (watchedEvent.getType()) {
                    case NodeChildrenChanged:
                    case NodeDataChanged:
                        loadNode(nodePath);
                        break;
                    default:
                        break;
                }
            }};
        curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);
        curatorFramework.getCuratorListenable().addListener(curatorListener);
        curatorFramework.start();
        log.info("DynamicTp refresher, add listener success, nodePath: {}", nodePath);
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
                    value = new String(data.watched().forPath(n), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    log.error("zk config value watched exception.", e);
                }
                content.append(nodeName).append("=").append(value).append("\n");
            });
            refresh(content.toString(), ConfigFileTypeEnum.PROPERTIES);
        } catch (Exception e) {
            log.error("load zk node error, nodePath is {}", nodePath, e);
        }
    }
}
