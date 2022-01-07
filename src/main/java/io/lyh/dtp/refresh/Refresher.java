package io.lyh.dtp.refresh;

import io.lyh.dtp.common.em.ConfigFileTypeEnum;

/**
 * Refresher related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-27 17:37
 * @since 1.0.0
 **/
public interface Refresher {

    /**
     * Refresh with specify content.
     * @param content content
     * @param fileType file type
     */
    void refresh(String content, ConfigFileTypeEnum fileType);
}
