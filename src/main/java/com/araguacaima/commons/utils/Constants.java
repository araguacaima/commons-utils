package com.araguacaima.commons.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

/**
 * Created by Alejandro on 15/01/2015.
 */
public class Constants {
    public static final String SIMPLE = "Simple";
    public static final String SHARED = "Shared";
    public static final String API_BASE_NAME = "api.raml";
    public static final String COMPLETE_TEXT = "$$$COMPLETE$$$";

    public enum UrlParams {
        PATH,
        QUERY_PARAM,
        PAYLOAD
    }

    public enum SpecialQueryParams {

        FIELDS("$fields"),
        EXPANDS("$expands"),
        FILTER("$filter"),
        SORT("$sort"),
        SHOW_SENSITIVE_DATA("$showSensitiveData"),
        QUERY_PARAM(""),
        PAYLOAD("payload");

        final String value;

        SpecialQueryParams(String value) {
            this.value = value;
        }

        public static SpecialQueryParams findValue(String value)
                throws IllegalArgumentException {
            SpecialQueryParams result = null;
            try {
                result = SpecialQueryParams.valueOf(value);
            } catch (IllegalArgumentException ignored) {
                for (SpecialQueryParams specialQueryParamsEnum : SpecialQueryParams.values()) {
                    String specialQueryParamsEnumStr = specialQueryParamsEnum.value();
                    if (specialQueryParamsEnumStr.equalsIgnoreCase(value)) {
                        return specialQueryParamsEnum;
                    }
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("No enum const " + SpecialQueryParams.class.getName() + "." + value);
            }
            return result;
        }

        public String value() {
            return value;
        }
    }

    public enum TemplateMetaData {
        FIELD_DESCRIPTION,
        MAPPINGS
    }

    public enum RAML_DIRECTORY_TYPE {
        SCHEMA,
        EXAMPLE,
        ERROR,
        DOC,
        TYPE
    }

    public enum JaxRsGenerationOrigin {
        TEMPLATE("Template"),
        RAML("Raml");

        final String value;

        JaxRsGenerationOrigin(String value) {
            this.value = value;
        }

        public static JaxRsGenerationOrigin findValue(String value)
                throws IllegalArgumentException {
            JaxRsGenerationOrigin result = null;
            try {
                result = JaxRsGenerationOrigin.valueOf(value);
            } catch (IllegalArgumentException ignored) {
                for (JaxRsGenerationOrigin ramlVersionsEnum : JaxRsGenerationOrigin.values()) {
                    String ramlversionsEnumStr = ramlVersionsEnum.value();
                    if (ramlversionsEnumStr.equalsIgnoreCase(value)) {
                        return ramlVersionsEnum;
                    }
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("No enum const " + RAML_VERSIONS.class.getName() + "." + value);
            }
            return result;
        }

        public String value() {
            return value;
        }

    }

    public enum JaxRsGenerationStyle {
        TypeA("TypeA"),
        TypeB("TypeB");

        final String value;

        JaxRsGenerationStyle(String value) {
            this.value = value;
        }

        public static JaxRsGenerationStyle findValue(String value)
                throws IllegalArgumentException {
            JaxRsGenerationStyle result = null;
            try {
                result = JaxRsGenerationStyle.valueOf(value);
            } catch (IllegalArgumentException ignored) {
                for (JaxRsGenerationStyle ramlVersionsEnum : JaxRsGenerationStyle.values()) {
                    String ramlversionsEnumStr = ramlVersionsEnum.value();
                    if (ramlversionsEnumStr.equalsIgnoreCase(value)) {
                        return ramlVersionsEnum;
                    }
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("No enum const " + RAML_VERSIONS.class.getName() + "." + value);
            }
            return result;
        }

        public String value() {
            return value;
        }
    }

    public enum RAML_VERSIONS {
        RAML_1_0();

        final String value;

        RAML_VERSIONS() {
            this.value = "1.0";
        }

        public static RAML_VERSIONS findValue(String value)
                throws IllegalArgumentException {
            RAML_VERSIONS result = null;
            try {
                result = RAML_VERSIONS.valueOf(value);
            } catch (IllegalArgumentException ignored) {
                for (RAML_VERSIONS ramlVersionsEnum : RAML_VERSIONS.values()) {
                    String ramlversionsEnumStr = ramlVersionsEnum.value();
                    if (ramlversionsEnumStr.equalsIgnoreCase(value)) {
                        return ramlVersionsEnum;
                    }
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("No enum const " + RAML_VERSIONS.class.getName() + "." + value);
            }
            return result;
        }

        public String value() {
            return value;
        }

    }

    public enum COMMONS_DESCRIPTIONS {
        TBD("To Be Defined"),
        COMPLETE("Complete");

        final String value;

        COMMONS_DESCRIPTIONS(String value) {
            this.value = value;
        }

        public static COMMONS_DESCRIPTIONS findValue(String value)
                throws IllegalArgumentException {
            COMMONS_DESCRIPTIONS result = null;
            try {
                result = COMMONS_DESCRIPTIONS.valueOf(value);
            } catch (IllegalArgumentException ignored) {
                for (COMMONS_DESCRIPTIONS ramlVersionsEnum : COMMONS_DESCRIPTIONS.values()) {
                    String ramlversionsEnumStr = ramlVersionsEnum.value();
                    if (ramlversionsEnumStr.equalsIgnoreCase(value)) {
                        return ramlVersionsEnum;
                    }
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("No enum const " + COMMONS_DESCRIPTIONS.class.getName() + "." +
                        value);
            }
            return result;
        }

        public String value() {
            return value;
        }

    }

}
