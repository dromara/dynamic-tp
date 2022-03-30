package com.dtp.starter.zookeeper.refresh;

import com.dtp.common.config.DtpProperties;
import com.dtp.common.em.ConfigFileTypeEnum;
import com.dtp.core.refresh.AbstractRefresher;
import com.dtp.starter.zookeeper.util.CuratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.WatchedEvent;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;

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
        curatorFramework = CuratorUtil.getCuratorFramework(dtpProperties);
        String nodePath = CuratorUtil.nodePath(dtpProperties);

        final ConnectionStateListener connectionStateListener = (client, newState) -> {
            if (newState == ConnectionState.RECONNECTED) {
                loadAndRefresh(nodePath);
            }};

        final CuratorListener curatorListener = (client, curatorEvent) -> {
            final WatchedEvent watchedEvent = curatorEvent.getWatchedEvent();
            if (null != watchedEvent) {
                switch (watchedEvent.getType()) {
                    case NodeChildrenChanged:
                    case NodeDataChanged:
                        loadAndRefresh(nodePath);
                        break;
                    default:
                        break;
                }
            }};
        curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);
        curatorFramework.getCuratorListenable().addListener(curatorListener);

        log.info("DynamicTp refresher, add listener success, nodePath: {}", nodePath);
    }

    /**
     * load config and refresh
     * @param nodePath config path
     */
    private void loadAndRefresh(String nodePath) {
        refresh(CuratorUtil.propertiesContent(curatorFramework, nodePath), ConfigFileTypeEnum.PROPERTIES);
    }
}
