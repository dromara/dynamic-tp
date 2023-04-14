package com.dtp.core.notifier.base;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.dtp.common.constant.WechatNotifyConst;
import com.dtp.common.entity.MarkdownReq;
import com.dtp.common.entity.NotifyPlatform;
import com.dtp.common.em.NotifyPlatformEnum;
import com.dtp.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * WechatNotifier related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class WechatNotifier implements Notifier {

    @Override
    public String platform() {
        return NotifyPlatformEnum.WECHAT.name().toLowerCase();
    }

    /**
     * Execute real wechat send.
     *
     * @param platform send platform
     * @param text send content
     */
    @Override
    public void send(NotifyPlatform platform, String text) {
        String serverUrl = WechatNotifyConst.WECHAT_WEH_HOOK + platform.getUrlKey();
        MarkdownReq markdownReq = new MarkdownReq();
        markdownReq.setMsgtype("markdown");
        MarkdownReq.Markdown markdown = new MarkdownReq.Markdown();
        markdown.setContent(text);
        markdownReq.setMarkdown(markdown);

        try {
            HttpResponse response = HttpRequest.post(serverUrl).body(JsonUtil.toJson(markdownReq)).execute();
            if (Objects.nonNull(response)) {
                log.info("DynamicTp notify, wechat send success, response: {}, request:{}",
                        response.body(), JsonUtil.toJson(markdownReq));
            }
        } catch (Exception e) {
            log.error("DynamicTp notify, wechat send failed...", e);
        }
    }
}
