package io.lyh.dtp.notify.ding;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import io.lyh.dtp.notify.AbstractNotifier;
import io.lyh.dtp.notify.NotifyPlatform;
import io.lyh.dtp.notify.MarkdownReq;
import io.lyh.dtp.util.DingSignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * DingAbsNotifier related
 *
 * @author: yanhom1314@gmail.com
 * @date: 2021-12-31 17:43
 * @since 1.0.0
 **/
@Slf4j
public abstract class DingAbsNotifier extends AbstractNotifier {

    public void doSend(NotifyPlatform notifyPlatform, String title, String text, List<String> mobiles) {

        MarkdownReq.Markdown markdown = new MarkdownReq.Markdown();
        markdown.setTitle(title);
        markdown.setText(text);

        MarkdownReq.At at = new MarkdownReq.At();
        at.setAtMobiles(mobiles);
        if (CollUtil.isEmpty(mobiles)) {
            at.setAtAll(true);
        }

        MarkdownReq markdownReq = new MarkdownReq();
        markdownReq.setMsgtype("markdown");
        markdownReq.setMarkdown(markdown);
        markdownReq.setAt(at);

        String hookUrl = getTargetUrl(notifyPlatform.getSecret(), notifyPlatform.getUrlKey());
        HttpResponse response = null;
        try {
            response = HttpRequest.post(hookUrl).body(JSONUtil.toJsonStr(markdownReq)).execute();
        } catch (Exception e) {
            log.error("DynamicTp notify, ding send fail...", e);
        } finally {
            if (Objects.nonNull(response)) {
                log.info("DynamicTp notify, ding send success, response: {}, request: {}",
                        response.body(), JSONUtil.toJsonStr(markdownReq));
            }
        }
    }

    private String getTargetUrl(String secret, String accessToken) {
        if (StringUtils.isBlank(secret)) {
            return DingNotifyConst.DING_WEBHOOK + accessToken;
        }
        long timestamp = System.currentTimeMillis();
        String sign = DingSignUtil.dingSign(secret, timestamp);
        return DingNotifyConst.DING_WEBHOOK + accessToken + "&timestamp=" + timestamp + "&sign=" + sign;
    }
}
