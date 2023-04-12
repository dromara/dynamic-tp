package com.dtp.common.json.parser;
/**
 * @author topsuder
 * @version v1.0.0
 * @see com.dtp.common.json.parser dynamic-tp
 */
public abstract class AbstractJsonParser implements JsonParser {

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
