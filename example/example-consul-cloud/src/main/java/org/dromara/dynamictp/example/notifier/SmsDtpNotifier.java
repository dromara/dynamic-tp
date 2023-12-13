package org.dromara.dynamictp.example.notifier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dromara.dynamictp.core.notifier.AbstractDtpNotifier;
import org.slf4j.MDC;

import static org.dromara.dynamictp.common.constant.DynamicTpConst.TRACE_ID;
import static org.dromara.dynamictp.common.constant.DynamicTpConst.UNKNOWN;

/**
 * SmsDtpNotifier related
 *
 * @author yanhom
 * @since 1.1.0
 */
public class SmsDtpNotifier extends AbstractDtpNotifier {

    public SmsDtpNotifier(SmsNotifier smsNotifier) {
        super(smsNotifier);
    }

    @Override
    public String platform() {
        return "sms";
    }

    @Override
    protected String getNoticeTemplate() {
        return SmsNotifyConst.SMS_NOTICE_TEMPLATE;
    }

    @Override
    protected String getAlarmTemplate() {
        return SmsNotifyConst.SMS_ALARM_TEMPLATE;
    }

    @Override
    protected Pair<String, String> getColors() {
        return null;
    }

    @Override
    protected String getTraceInfo() {
        if (StringUtils.isBlank(MDC.get(TRACE_ID))) {
            return UNKNOWN;
        }
        return "[跳转详情](" + getKibanaUrl(MDC.get(TRACE_ID)) + ")";
    }

    @Override
    protected String getExtInfo() {
        String extInfo = super.getExtInfo();
        String memoryMetrics = getMemoryMetrics();
        if (StringUtils.isBlank(extInfo)) {
            return memoryMetrics;
        }
        return extInfo + "\n" + memoryMetrics;
    }

    private String getKibanaUrl(String traceId) {
        return "https://kibana.com/app/kibana#/discover?_g=()&_a=(columns:!(_source),index:'logstash-*',interval:auto,query:(language:lucene,query:'traceId:" + traceId + "'),sort:!('@timestamp',desc))";
    }

    private String getMemoryMetrics() {
        int heapInit = 1024;
        int heapUsed = 521;
        int heapCommitted = 1000;
        int heapMax = 1024;
        return "MemoryMetrics{" +
                "heapInit=" + heapInit +
                ", heapUsed=" + heapUsed +
                ", heapCommitted=" + heapCommitted +
                ", heapMax=" + heapMax +
                "}";
    }
}
