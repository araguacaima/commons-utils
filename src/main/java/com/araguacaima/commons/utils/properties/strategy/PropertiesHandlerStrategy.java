/*
 * Copyright 2017 araguacaima
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.araguacaima.commons.utils.properties.strategy;

import com.araguacaima.commons.exception.core.PropertiesUtilException;
import com.araguacaima.commons.utils.FileUtils;
import com.araguacaima.commons.utils.PropertiesHandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;


public abstract class PropertiesHandlerStrategy implements PropertiesHandlerStrategyInterface {

    public static final String PROPERTIES_HANDLER_STRATEGY_CLASSPATH = "CLASSPATH";
    public static final String PROPERTIES_HANDLER_STRATEGY_DB = "DB";
    public static final String PROPERTIES_HANDLER_STRATEGY_FTP = "FTP";
    public static final String PROPERTIES_HANDLER_STRATEGY_PATH = "PATH";
    public static final String PROPERTIES_HANDLER_STRATEGY_URL = "URL";
    static final String PROPERTIES_HANDLER_STRATEGY_DEFAULT = "DEFAULT";
    static final Logger log = LoggerFactory.getLogger(PropertiesHandlerStrategy.class);
    public static boolean isInitialized = false;
    protected String PROPERTY_KEY_PREFIX;

    protected String applicationId;
    protected PropertiesHandlerStrategyPath defaultHandler;
    protected FileUtils fileUtils;
    protected PropertiesHandlerUtils propertiesHandlerUtils;
    Properties properties;


    public PropertiesHandlerStrategy(PropertiesHandlerUtils propertiesHandlerUtils,
                                     FileUtils fileUtils,
                                     PropertiesHandlerStrategyPath propertiesHandlerStrategyPath) {
        this.propertiesHandlerUtils = propertiesHandlerUtils;
        this.fileUtils = fileUtils;
        this.defaultHandler = propertiesHandlerStrategyPath;
    }

    PropertiesHandlerStrategy() {

    }

    public abstract Map<String, String> getOriginProperties();

    public Properties getProperties()
            throws PropertiesUtilException {
        if (properties == null || properties.size() == 0) {
            if (getNext() != null) {
                return getNext().getProperties();
            } else {
                return new Properties();
            }
        } else {
            return properties;
        }
    }

    public abstract PropertiesHandlerStrategyInterface getNext();

    public abstract void setNext(PropertiesHandlerStrategyInterface next);

    public abstract String getPropertiesHandlerStrategyName();

    public String getProperty(String key) {
        return String.valueOf(properties.get(key));
    }

}