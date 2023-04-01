package com.dtp.core.support;

import com.dtp.common.util.VersionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;

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

    @Override
    public void afterPropertiesSet() {
        log.info(AnsiOutput.toString(BANNER, "\n", AnsiColor.GREEN, NAME, "\n", " :: ", VersionUtil.getVersion(), " :: ",
                "\n", SITE, AnsiColor.DEFAULT, AnsiStyle.FAINT));
    }
}
