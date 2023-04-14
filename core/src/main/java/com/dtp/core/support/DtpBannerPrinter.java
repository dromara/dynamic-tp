package com.dtp.core.support;

import com.dtp.common.constant.DynamicTpConst;
import com.dtp.common.util.VersionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

/**
 * DtpBannerPrinter related
 *
 * @author yanhom
 * @since 1.0.0
 **/
@Slf4j
public class DtpBannerPrinter implements InitializingBean {

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

    @Resource
    private Environment env;
    
    @Override
    public void afterPropertiesSet() {
        final Boolean enable = env.getProperty(DynamicTpConst.BANNER_ENABLED_PROP, boolean.class, true);
        if (enable) {
            log.info(BANNER + "\n" + NAME + "\n :: " + VersionUtil.getVersion() + " :: \n" + SITE + "\n");
        }
    }
}
