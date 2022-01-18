package com.dtp.common.dto;

import lombok.Data;

import java.util.List;

/**
 * MarkdownReq related
 *
 * @author: yanhom
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

    @Data
    public static class Markdown {
        private String title;

        // for wechat
        private String content;

        // for ding
        private String text;
    }

    @Data
    public static class At {

        private List<String> atMobiles;

        private boolean isAtAll;
    }
}
