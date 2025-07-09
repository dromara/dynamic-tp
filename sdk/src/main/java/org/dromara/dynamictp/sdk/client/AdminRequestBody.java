package org.dromara.dynamictp.sdk.client;

import lombok.Getter;

import java.io.*;
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

    public void objectToBytes(Object object) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(object);
            this.body = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object bytesToObject() {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
             ObjectInputStream  objectOutputStream = new ObjectInputStream(byteArrayInputStream)) {
            return objectOutputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
