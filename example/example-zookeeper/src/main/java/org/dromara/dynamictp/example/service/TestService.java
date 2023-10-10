package org.dromara.dynamictp.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author <a href = "mailto:kamtohung@gmail.com">KamTo Hung</a>
 */
@Slf4j
@Service
public class TestService {

    @Async("commonExecutor")
    public void test() {
      log.info("I am dynamic-tp-test-1 task");
    }

}
