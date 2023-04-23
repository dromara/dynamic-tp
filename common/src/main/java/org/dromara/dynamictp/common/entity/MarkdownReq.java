package org.dromara.dynamictp.common.entity;

import lombok.Data;

import java.util.List;

/**
 * MarkdownReq related
 *
 * @author yanhom
 * @since 1.0.0
 */
@Data
public class MarkdownReq {

    /**
     * msgType
     */
    private String msgtype;

    /**
     * markdown
     */
    private Markdown markdown;

    private At at;

    /**
     * Markdown entity.
     */
    @Data
    public static class Markdown {

        private String title;

        // for wechat
        private String content;

        // for ding
        private String text;
    }

    /**
     * At info.
     */
    @Data
    public static class At {

        private List<String> atMobiles;

        private boolean isAtAll;
    }
}

