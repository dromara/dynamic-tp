package com.dtp.example;

import com.dtp.core.thread.DtpExecutor;
import com.dtp.core.DtpRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * IndexController related
 *
 * @author yanhom
 * @date 2021-08-13 下午11:49
 */
@RestController
@RequestMapping("/dtp")
public class IndexController {

    private static final Logger logger = LogManager.getLogger();

    @GetMapping("/test")
    public void test() {
        DtpExecutor dtpExecutor = DtpRegistry.getExecutor("dynamic-tp-test");
        dtpExecutor.execute(() -> System.out.println("test"));
    }
}
