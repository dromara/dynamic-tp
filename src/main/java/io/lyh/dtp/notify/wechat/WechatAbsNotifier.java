package io.lyh.dtp.notify.wechat;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import io.lyh.dtp.notify.AbstractNotifier;
import io.lyh.dtp.notify.NotifyPlatform;
import io.lyh.dtp.notify.MarkdownReq;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * AbstractNotifier related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-23 10:39
 * @since 1.0.0
 **/
@Slf4j
public abstract class WechatAbsNotifier extends AbstractNotifier {

    /**
     * Execute real WeChat send.
     * @param platform send platform
     * @param text send content
     */
    protected void doSend(NotifyPlatform platform, String text) {
        String serverUrl = WechatNotifyConst.WECHAT_WEH_HOOK + platform.getUrlKey();
        MarkdownReq markdownReq = new MarkdownReq();
        markdownReq.setMsgtype("markdown");
        MarkdownReq.Markdown markdown = new MarkdownReq.Markdown();
        markdown.setContent(text);
        markdownReq.setMarkdown(markdown);

        HttpResponse response = null;
        try {
             response = HttpRequest.post(serverUrl).body(JSONUtil.toJsonStr(markdownReq)).execute();
        } catch (Exception e) {
            log.error("DynamicTp notify, wechat send fail...", e);
        } finally {
            if (Objects.nonNull(response)) {
                log.info("DynamicTp notify, wechat send success, response: {}, request:{}",
                        response.body(), JSONUtil.toJsonStr(markdownReq));
            }
        }
    }
}
