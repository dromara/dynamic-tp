package com.dtp.common.json.parser;

/**
 * @see com.dtp.common.json.parser dynamic-tp
 *
 * @author topsuder
 * @since 1.1.3
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

    /**
     * get mapper class name
     *
     * @return mapper class name
     */
    protected abstract String getMapperClassName();
}
