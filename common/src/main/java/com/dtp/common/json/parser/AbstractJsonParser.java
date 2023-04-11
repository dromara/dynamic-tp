package com.dtp.common.json.parser;
/**
 * @author topsuder
 * @version v1.0.0
 * @DATE 2023/4/11-14:39
 * @see com.dtp.common.json.parser dynamic-tp
 */
public abstract class AbstractJsonParser<T> implements JsonParser {
    protected volatile T mapper;

    protected T createMapper() {
        return null;
    }

    protected T getMapper() {
        //子类是否跳过mapper的创建(静态类场景)
        if (skipMapper()) {
            return null;
        }
        if (mapper == null) {
            synchronized (this) {
                if (mapper == null) {
                    mapper = createMapper();
                }
            }
        }
        return mapper;
    }


    protected boolean skipMapper() {
        return false;
    }

    public boolean isSupport() {
        try {
            Class.forName(getMapperClassName());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected abstract String getMapperClassName();
}
