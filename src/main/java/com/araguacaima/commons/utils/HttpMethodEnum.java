package com.araguacaima.commons.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Alejandro on 21/11/2014.
 */
public enum HttpMethodEnum {

    GET("GET"),
    POST("POST"),
    PATCH("PATCH"),
    PUT("PUT"),
    DELETE("DELETE"),
    HEAD("HEAD");

    private final String value;

    HttpMethodEnum(String value) {
        this.value = value.trim();
    }

    public static HttpMethodEnum findValue(String value)
            throws IllegalArgumentException {
        HttpMethodEnum result = null;
        try {
            result = HttpMethodEnum.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            for (HttpMethodEnum remotingEnum : HttpMethodEnum.values()) {
                String tecnologiaRemotizacionEnumStr = remotingEnum.value();
                if (tecnologiaRemotizacionEnumStr.equalsIgnoreCase(value)) {
                    return remotingEnum;
                } else {
                    if (tecnologiaRemotizacionEnumStr.replaceAll("-",
                            StringUtils.EMPTY).equalsIgnoreCase(value.replaceAll("_", StringUtils.EMPTY).replaceAll("-",
                            StringUtils.EMPTY).replaceAll(" ", StringUtils.EMPTY))) {
                        return remotingEnum;
                    }
                }
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("No enum const " + HttpMethodEnum.class.getName() + "." + value);
        }
        return result;
    }

    public static HttpMethodEnum[] all() {
        return new HttpMethodEnum[]{GET, POST, PATCH, PUT};
    }

    public String value() {
        return this.value;
    }
}
