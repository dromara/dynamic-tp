package io.lyh.dynamic.tp.core.notify.wechat;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import io.lyh.dynamic.tp.common.constant.WechatNotifyConst;
import io.lyh.dynamic.tp.common.dto.MarkdownReq;
import io.lyh.dynamic.tp.common.dto.NotifyPlatform;
import io.lyh.dynamic.tp.core.notify.AbstractNotifier;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * AbstractNotifier related
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Slf4j
public abstract class AbstractWechatNotifier extends AbstractNotifier {

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
