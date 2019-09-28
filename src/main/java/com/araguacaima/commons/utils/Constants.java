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
    public static final String BASE_API_NAMESPACE = "https://www.bbvaapis.com/";
    public static final String BASE_API_NAMESPACE_PATTERN = BASE_API_NAMESPACE + "${serviceName}/${version}";
    public static final String API_BASE_NAME = "api.raml";
    public static final String COMPLETE_TEXT = "$$$COMPLETE$$$";
    public static final String APIS_COMMONS_COMMONS = "glapi-global-apis-commons-commons";
    public static final Locale LOCALE_EN = new Locale("en", "us");
    public static final Locale LOCALE_ES = new Locale("es", "es");
    public static Collection<Locale> LOCALES = new ArrayList<Locale>() {
        {
            add(LOCALE_EN);
            add(LOCALE_ES);
        }
    };

    public enum SOURCE_TYPE {
        LOCAL,
        GOOGLE_DRIVE,
        REMOTE
    }

    public enum DROOLS_SESSION_TYPE {
        STATELESS,
        STATEFULL
    }

    public enum URL_RESOURCE_STRATEGIES {
        WORKBENCH,
        MAVEN,
        ABSOLUTE_DECISION_TABLE_PATH,
        GOOGLE_DRIVE_DECISION_TABLE_PATH,
        ABSOLUTE_DRL_PATH
    }

    public enum RULES_SESSION_TYPE {
        STATEFUL,
        STATELESS
    }

    public enum RULES_REPOSITORY_STRATEGIES {
        DRL,
        DECISION_TABLE
    }

    public enum GOOGLE_DRIVE_CREDENTIALS_STRATEGIES {
        SERVER_TO_SERVER
    }

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
        ASO("Aso"),
        APX("Apx");

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
