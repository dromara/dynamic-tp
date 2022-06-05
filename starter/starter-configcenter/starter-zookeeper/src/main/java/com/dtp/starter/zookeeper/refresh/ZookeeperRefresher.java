package com.dtp.starter.zookeeper.refresh;

import com.dtp.common.config.DtpProperties;
import com.dtp.core.refresh.AbstractRefresher;
import com.dtp.starter.zookeeper.util.CuratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.WatchedEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

import static com.dtp.starter.zookeeper.autoconfigure.ZkConfigEnvironmentProcessor.ZK_PROPERTY_SOURCE_NAME;

/**
 * @author Redick01
 */
@Slf4j
public class ZookeeperRefresher extends AbstractRefresher implements EnvironmentAware, InitializingBean {

    @Resource
    private DtpProperties dtpProperties;

    @Override
    public void afterPropertiesSet() {

        final ConnectionStateListener connectionStateListener = (client, newState) -> {
            if (newState == ConnectionState.RECONNECTED) {
                loadAndRefresh();
            }};

        final CuratorListener curatorListener = (client, curatorEvent) -> {
            final WatchedEvent watchedEvent = curatorEvent.getWatchedEvent();
            if (null != watchedEvent) {
                switch (watchedEvent.getType()) {
                    case NodeChildrenChanged:
                    case NodeDataChanged:
                        loadAndRefresh();
                        break;
                    default:
                        break;
                }
            }};

        CuratorFramework curatorFramework = CuratorUtil.getCuratorFramework(dtpProperties);
        String nodePath = CuratorUtil.nodePath(dtpProperties);

        curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);
        curatorFramework.getCuratorListenable().addListener(curatorListener);

        log.info("DynamicTp refresher, add listener success, nodePath: {}", nodePath);
    }

    /**
     * load config and refresh
     */
    private void loadAndRefresh() {
        doRefresh(CuratorUtil.genPropertiesMap(dtpProperties));
    }

    @Override
    public void setEnvironment(Environment environment) {
        ConfigurableEnvironment env = ((ConfigurableEnvironment) environment);
        env.getPropertySources().remove(ZK_PROPERTY_SOURCE_NAME);
    }
}
