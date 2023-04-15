package com.dtp.common.parser.json;

/**
 *
 * @author topsuder
 * @since 1.1.3
 */
public abstract class AbstractJsonParser implements JsonParser {

    @Override
    public boolean supports() {
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
