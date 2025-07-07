package org.dromara.dynamictp.sdk;

import lombok.Getter;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public class AdminRequestBody implements Serializable {

    private static final long  serialVersionUID = -1288207208017808618L;

    @Getter
    private final long id;

    private AdminRequestTypeEnum requestType;

    private byte[] body;

    public AdminRequestBody(long id, AdminRequestTypeEnum requestType) {
        this.id = id;
        this.requestType = requestType;
    }

    public AdminRequestBody(int id, int size) {
        this.id = id;
        this.body = new byte[size];
        ThreadLocalRandom.current().nextBytes(this.body);
    }

}
