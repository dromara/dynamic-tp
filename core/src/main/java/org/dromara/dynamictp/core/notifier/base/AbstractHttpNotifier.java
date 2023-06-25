package org.dromara.dynamictp.core.notifier.base;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.dromara.dynamictp.common.entity.NotifyPlatform;

import java.util.Objects;

/**
 * The notification is sent over http
 *
 * @author kyao
 * @since 1.1.3
 */
@Slf4j
public abstract class AbstractHttpNotifier extends AbstractNotifier{

    /**
     * Message sending mode
     * @author kyao
     * @param platform
     * @param content
     */
    @Override
    protected void sendMode(NotifyPlatform platform, String content) {
        var url = buildUrl(platform);
        var msgBody = buildMsgBody(platform, content);
        HttpResponse response = HttpRequest.post(url).body(msgBody).execute();
        if (Objects.nonNull(response)) {
            log.info("DynamicTp notify, {} send success, response: {}, request: {}",
                    platform(), response.body(), msgBody);
        }
    }

    /**
     * build http message body
     * @author hanli
     * @param platform
     * @param content
     * @return java.lang.String
     */
    protected abstract String buildMsgBody(NotifyPlatform platform, String content);

    /**
     * build http url
     * @author hanli
     * @date 2023/6/25 12:09 PM
     * @param platform
     * @return java.lang.String
     */
    protected abstract String buildUrl(NotifyPlatform platform);

}
