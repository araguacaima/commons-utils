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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesHandlerStrategyDefault extends PropertiesHandlerStrategy {

    private static final String PROPERTIES_HANDLER_STRATEGY_NAME = PropertiesHandlerStrategy
            .PROPERTIES_HANDLER_STRATEGY_DEFAULT;
    protected PropertiesHandlerStrategyInterface next;

    public PropertiesHandlerStrategyInterface getNext() {
        return defaultHandler.getNext();
    }

    @Override
    public void setNext(PropertiesHandlerStrategyInterface next) {
        defaultHandler.setNext(next);
    }

    @Override
    public Map<String, String> getOriginProperties() {
        Map<String, String> originProperties = new HashMap<>();
        originProperties.put(PROPERTIES_HANDLER_STRATEGY_NAME, defaultHandler.getClass().getName());
        return originProperties;
    }

    @Override
    public Properties getProperties()
            throws PropertiesUtilException {
        return defaultHandler.getProperties();
    }

    @Override
    public String getPropertiesHandlerStrategyName() {
        return PROPERTIES_HANDLER_STRATEGY_NAME;
    }

    public void setDefaultPath(String defaultPath) {
        defaultHandler.setFileInPath(defaultPath);
    }
}
