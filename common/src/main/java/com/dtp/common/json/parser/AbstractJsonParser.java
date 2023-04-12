package com.dtp.common.json.parser;
/**
 * @author topsuder
 * @version v1.0.0
 * @DATE 2023/4/11-14:39
 * @see com.dtp.common.json.parser dynamic-tp
 */
public abstract class AbstractJsonParser<T> implements JsonParser {


    protected T createMapper() {
        return null;
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
