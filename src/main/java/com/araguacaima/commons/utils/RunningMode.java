package com.araguacaima.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class RunningMode {

    public static final String RUNNING_MODE_BYPASSING = "running.mode_bypass";
    public static final String RUNNING_MODE_DEPLOYMENT = "running.mode_deployment";
    public static final String RUNNING_MODE_PRODUCTION = "running.mode_production";
    public static final String RUNNING_MODE_DEFAULT = RUNNING_MODE_PRODUCTION;
    public static final String RUNNING_MODE_TESTING = "running.mode_testing";
    private static final Logger log = LoggerFactory.getLogger(RunningMode.class);
    private String logFileSourceName;
    private PropertiesHandlerUtil propertiesHandlerUtil;
    private String runningMode = RUNNING_MODE_DEFAULT;

    @Autowired
    public RunningMode(PropertiesHandlerUtil propertiesHandlerUtil) {
        this.propertiesHandlerUtil = propertiesHandlerUtil;
    }

    public String getRunningMode() {
        return this.runningMode;
    }

    public void setRunningMode(String runningMode) {
        this.runningMode = runningMode;
    }

    public boolean isByPass() {
        return this.runningMode.equals(RUNNING_MODE_BYPASSING);
    }

    public boolean isDeployment() {
        return this.runningMode.equals(RUNNING_MODE_DEPLOYMENT);
    }

    public boolean isProduction() {
        return this.runningMode.equals(RUNNING_MODE_PRODUCTION);
    }

    public boolean isTesting() {
        return this.runningMode.equals(RUNNING_MODE_TESTING);
    }

    public void setLogFile(String logFileSourceName) {
        try {
            Properties properties = propertiesHandlerUtil.getHandler(logFileSourceName).getProperties();
            runningMode = properties.getProperty("running_mode");
            if (StringUtils.isBlank(runningMode)) {
                log.debug("Running mode (running_mode) is not found inside the file", 2);
            } else {
                log.debug("Running mode (running_mode) is: " + runningMode, 2);
            }

        } catch (Exception exception) {
            log.debug("Config file (" + logFileSourceName + ") is null or invalid. Running mode is: " +
                            RUNNING_MODE_DEFAULT,
                    1);
            log.debug(exception.getMessage(), 2);
        }
    }
}
