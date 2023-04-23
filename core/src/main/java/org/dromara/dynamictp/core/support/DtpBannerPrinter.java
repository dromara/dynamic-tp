package org.dromara.dynamictp.core.support;

import org.dromara.dynamictp.common.constant.DynamicTpConst;
import org.dromara.dynamictp.common.util.VersionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * DtpBannerPrinter related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpBannerPrinter implements EnvironmentAware {

    private static final String NAME = " :: Dynamic Thread Pool :: ";

    private static final String SITE = " :: https://dynamictp.cn ::";

    private static final String BANNER = "\n" +
            "|  __ \\                            (_) |__   __|   \n" +
            "| |  | |_   _ _ __   __ _ _ __ ___  _  ___| |_ __  \n" +
            "| |  | | | | | '_ \\ / _` | '_ ` _ \\| |/ __| | '_ \\ \n" +
            "| |__| | |_| | | | | (_| | | | | | | | (__| | |_) |\n" +
            "|_____/ \\__, |_| |_|\\__,_|_| |_| |_|_|\\___|_| .__/ \n" +
            "         __/ |                              | |    \n" +
            "        |___/                               |_|    ";

    @Override
    public void setEnvironment(Environment environment) {
        boolean enable = environment.getProperty(DynamicTpConst.BANNER_ENABLED_PROP,
                boolean.class, true);
        if (enable) {
            log.info(BANNER + "\n" + NAME + "\n :: " + VersionUtil.getVersion() + " :: \n" + SITE + "\n");
        }
    }
}
