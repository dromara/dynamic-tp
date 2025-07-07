package org.dromara.dynamictp.sdk.client;

import lombok.Getter;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class AdminRequestBody implements Serializable {

    private static final long  serialVersionUID = -1288207208017808618L;

    @Getter
    private final long id;

    @Getter
    private final AdminRequestTypeEnum requestType;

    private byte[] body;

    public AdminRequestBody(long id, AdminRequestTypeEnum requestType) {
        this(id, requestType, 1024);
    }

    public AdminRequestBody(long id, AdminRequestTypeEnum requestType, int size) {
        this.id = id;
        this.body = new byte[size];
        ThreadLocalRandom.current().nextBytes(this.body);
        this.requestType = requestType;
    }

}
