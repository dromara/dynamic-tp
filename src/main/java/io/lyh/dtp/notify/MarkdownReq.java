package io.lyh.dtp.notify;

import lombok.Data;

import java.util.List;

/**
 * MarkdownReq related
 *
 * @author: yanhom1314@gmail.com
 * @date 2022-01-02 下午3:31
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
