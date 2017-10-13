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
import com.araguacaima.commons.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component("URL")
public class PropertiesHandlerStrategyURL extends PropertiesHandlerStrategy {

    private static final String PROPERTIES_HANDLER_STRATEGY_NAME = PropertiesHandlerStrategy
            .PROPERTIES_HANDLER_STRATEGY_URL;
    private static final String PROPERTIES_URL_LOCAL_FILE_PATH = "PROPERTIES_URL_LOCAL_FILE_PATH";
    private static final String PROPERTIES_URL_REMOTE_FILE_PATH = "PROPERTIES_URL_REMOTE_FILE_PATH";
    private static final String PROPERTIES_URL_SERVER_DOMAIN_AND_PORT = "PROPERTIES_URL_SERVER_DOMAIN_AND_PORT";
    private static final String PROPERTIES_URL_SERVER_DOMAIN_LOGIN = "PROPERTIES_URL_SERVER_DOMAIN_LOGIN";
    private static final String PROPERTIES_URL_SERVER_DOMAIN_PASSWORD = "PROPERTIES_URL_SERVER_DOMAIN_PASSWORD";
    private PropertiesHandlerStrategyInterface next;
    @Value("${url.local.file.path}")
    private String urlLocalFilePath = StringUtils.EMPTY;
    @Value("${url.remote.file.path}")
    private String urlRemoteFilePath = StringUtils.EMPTY;
    @Value("${url.server.domain.and.port}")
    private String urlServerDomainAndPort = StringUtils.EMPTY;
    @Value("${url.server.domain.login}")
    private String urlServerDomainLogin = StringUtils.EMPTY;
    @Value("${url.server.domain.password}")
    private String urlServerDomainPassword = StringUtils.EMPTY;

    @Override
    public PropertiesHandlerStrategyInterface getNext() {
        return next;
    }

    @Override
    public void setNext(PropertiesHandlerStrategyInterface next) {
        this.next = next;
    }

    @Override
    public Map<String, String> getOriginProperties() {
        Map<String, String> originProperties = new HashMap<>();
        originProperties.put(PROPERTIES_URL_REMOTE_FILE_PATH, PROPERTIES_URL_REMOTE_FILE_PATH);
        originProperties.put(PROPERTIES_URL_LOCAL_FILE_PATH, PROPERTIES_URL_LOCAL_FILE_PATH);
        originProperties.put(PROPERTIES_URL_SERVER_DOMAIN_AND_PORT, PROPERTIES_URL_SERVER_DOMAIN_AND_PORT);
        originProperties.put(PROPERTIES_URL_SERVER_DOMAIN_LOGIN, PROPERTIES_URL_SERVER_DOMAIN_LOGIN);
        originProperties.put(PROPERTIES_URL_SERVER_DOMAIN_PASSWORD, PROPERTIES_URL_SERVER_DOMAIN_PASSWORD);
        return originProperties;
    }

    @Override
    public Properties getProperties()
            throws PropertiesUtilException {

        File file = fileUtils.getFileFromURL(urlLocalFilePath,
                urlRemoteFilePath,
                urlServerDomainAndPort,
                urlServerDomainLogin,
                urlServerDomainPassword);
        if (file != null) {
            properties = propertiesHandlerUtils.getHandler(file.getPath(), true).getProperties();
        }
        return super.getProperties();
    }

    /**
     * @return the file obtained from URL
     */

    @Override
    public String getPropertiesHandlerStrategyName() {
        return PROPERTIES_HANDLER_STRATEGY_NAME;
    }

}