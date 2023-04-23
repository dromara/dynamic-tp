package org.dromara.dynamictp.core.notifier.base;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.dromara.dynamictp.common.constant.DingNotifyConst;
import org.dromara.dynamictp.common.entity.MarkdownReq;
import org.dromara.dynamictp.common.entity.NotifyPlatform;
import org.dromara.dynamictp.common.em.NotifyPlatformEnum;
import org.dromara.dynamictp.common.util.DingSignUtil;
import org.dromara.dynamictp.common.util.JsonUtil;
import org.dromara.dynamictp.common.util.TimeUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

import static org.dromara.dynamictp.common.constant.DingNotifyConst.DING_NOTICE_TITLE;

/**
 * DingNotifier related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DingNotifier implements Notifier {

    @Override
    public String platform() {
        return NotifyPlatformEnum.DING.name().toLowerCase();
    }

    /**
     * Execute real ding send.
     *
     * @param platform send platform
     * @param text send content
     */
    @Override
    public void send(NotifyPlatform platform, String text) {

        MarkdownReq.Markdown markdown = new MarkdownReq.Markdown();
        markdown.setTitle(DING_NOTICE_TITLE);
        markdown.setText(text);

        MarkdownReq.At at = new MarkdownReq.At();

        List<String> mobiles = Lists.newArrayList(StringUtils.split(platform.getReceivers(), ','));
        at.setAtMobiles(mobiles);
        if (CollectionUtils.isEmpty(mobiles)) {
            at.setAtAll(true);
        }

        MarkdownReq markdownReq = new MarkdownReq();
        markdownReq.setMsgtype("markdown");
        markdownReq.setMarkdown(markdown);
        markdownReq.setAt(at);

        String hookUrl = getTargetUrl(platform.getSecret(), platform.getUrlKey());
        try {
            HttpResponse response = HttpRequest.post(hookUrl).body(JsonUtil.toJson(markdownReq)).execute();
            if (Objects.nonNull(response)) {
                log.info("DynamicTp notify, ding send success, response: {}, request: {}",
                        response.body(), JsonUtil.toJson(markdownReq));
            }
        } catch (Exception e) {
            log.error("DynamicTp notify, ding send failed...", e);
        }
    }

    /**
     * Build target url.
     * @param secret secret
     * @param accessToken accessToken
     * @return url
     */
    private String getTargetUrl(String secret, String accessToken) {
        if (StringUtils.isBlank(secret)) {
            return DingNotifyConst.DING_WEBHOOK + accessToken;
        }
        long timestamp = TimeUtil.currentTimeMillis();
        String sign = DingSignUtil.dingSign(secret, timestamp);
        return DingNotifyConst.DING_WEBHOOK + accessToken + "&timestamp=" + timestamp + "&sign=" + sign;
    }
}
