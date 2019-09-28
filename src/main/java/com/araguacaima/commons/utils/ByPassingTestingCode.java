package com.araguacaima.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;

@Component
public class ByPassingTestingCode {

    private static final Logger log = LoggerFactory.getLogger(ByPassingTestingCode.class);
    private final MapUtils mapUtils;
    private final NotNullOrEmptyStringObjectPredicate notNullOrEmptyStringObjectPredicate;
    private final PropertiesHandlerUtils propertiesHandlerUtils;
    public Map BYPASSING_TESTING_CODE_PARAMETERS_MAP;
    private String logFileSourceName;

    public ByPassingTestingCode(PropertiesHandlerUtils propertiesHandlerUtils,
                                MapUtils mapUtils,
                                NotNullOrEmptyStringObjectPredicate notNullOrEmptyStringObjectPredicate) {
        this.propertiesHandlerUtils = propertiesHandlerUtils;
        this.mapUtils = mapUtils;
        this.notNullOrEmptyStringObjectPredicate = notNullOrEmptyStringObjectPredicate;
    }

    public void init(String logFileSourceName) {
        final Properties properties;

        try {
            properties = propertiesHandlerUtils.getHandler(logFileSourceName).getProperties();
            BYPASSING_TESTING_CODE_PARAMETERS_MAP = mapUtils.select(properties,
                    object -> object.toString().startsWith(ByPassingTestingConstants
                            .BYPASSING_TESTING_CODE_PARAMETER_PREFIX),
                    notNullOrEmptyStringObjectPredicate);

        } catch (Exception exception) {
            log.debug("Config file (" + logFileSourceName + ") is null or empty. No Bypassing Testing Code", 2);
            log.debug(exception.getMessage(), 2);
        } finally {
            if (MapUtils.isEmpty(BYPASSING_TESTING_CODE_PARAMETERS_MAP)) {
                log.debug("No Bypassing Testing Code (bypassing.testing.code_) found inside the file");
            } else {
                log.debug("Bypassing Testing Code (bypassing.testing.code_): " + BYPASSING_TESTING_CODE_PARAMETERS_MAP);
            }
        }
    }

}
