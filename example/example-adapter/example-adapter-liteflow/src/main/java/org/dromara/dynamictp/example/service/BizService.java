package org.dromara.dynamictp.example.service;

import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * BizService related
 *
 * @author yanhom
 * @since 1.1.0
 */
@Slf4j
@Service
public class BizService {

    @Resource
    private FlowExecutor flowExecutor;

    public void testConfig(){
        LiteflowResponse response = flowExecutor.execute2Resp("chain1", "arg");
        log.info("response:{}", response);
    }
}
