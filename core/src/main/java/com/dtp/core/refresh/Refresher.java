package com.dtp.core.refresh;

import com.dtp.common.em.ConfigFileTypeEnum;

/**
 * Refresher related
 *
 * @author: yanhom
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
