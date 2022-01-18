package com.dtp.common.em;

import lombok.Getter;

/**
 * Config file type.
 *
 * @author: yanhom
 * @since 1.0.0
 **/
@Getter
public enum ConfigFileTypeEnum {

    /**
     * Config file type.
     */
    PROPERTIES("properties"),
    XML("xml"),
    JSON("json"),
    YML("yml"),
    YAML("yaml"),
    TXT("txt");

    private final String value;

    ConfigFileTypeEnum(String value) {
        this.value = value;
    }

    public static ConfigFileTypeEnum of(String value) {
        for (ConfigFileTypeEnum typeEnum : ConfigFileTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return PROPERTIES;
    }
}
