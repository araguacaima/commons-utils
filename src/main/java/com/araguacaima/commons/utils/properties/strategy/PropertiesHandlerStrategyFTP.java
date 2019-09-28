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

@Component("FTP")
public class PropertiesHandlerStrategyFTP extends PropertiesHandlerStrategy {

    private static final String PROPERTIES_FTP_LOCAL_FILE_PATH = "PROPERTIES_FTP_LOCAL_FILE_PATH";
    private static final String PROPERTIES_FTP_REMOTE_FILE_PATH = "PROPERTIES_FTP_REMOTE_FILE_PATH";
    private static final String PROPERTIES_FTP_SERVER_DOMAIN = "PROPERTIES_FTP_SERVER_DOMAIN";
    private static final String PROPERTIES_FTP_SERVER_DOMAIN_LOGIN = "PROPERTIES_FTP_SERVER_DOMAIN_LOGIN";
    private static final String PROPERTIES_FTP_SERVER_DOMAIN_PASSWORD = "PROPERTIES_FTP_SERVER_DOMAIN_PASSWORD";
    private static final String PROPERTIES_HANDLER_STRATEGY_NAME = PropertiesHandlerStrategy
            .PROPERTIES_HANDLER_STRATEGY_FTP;
    @Value("${ftp.local.file.path}")
    private final String ftpLocalFilePath = StringUtils.EMPTY;
    @Value("${ftp.remote.file.path}")
    private final String ftpRemoteFilePath = StringUtils.EMPTY;
    @Value("${ftp.server.domain}")
    private final String ftpServerDomain = StringUtils.EMPTY;
    @Value("${ftp.server.domain.login}")
    private final String ftpServerDomainLogin = StringUtils.EMPTY;
    @Value("${ftp.server.domain.password}")
    private final String ftpServerDomainPassword = StringUtils.EMPTY;
    private PropertiesHandlerStrategyInterface next;

    public PropertiesHandlerStrategyInterface getNext() {
        return next;
    }

    public void setNext(PropertiesHandlerStrategyInterface next) {
        this.next = next;
    }

    public Map<String, String> getOriginProperties() {
        Map<String, String> originProperties = new HashMap<>();
        originProperties.put(PROPERTIES_FTP_REMOTE_FILE_PATH, ftpRemoteFilePath);
        originProperties.put(PROPERTIES_FTP_LOCAL_FILE_PATH, ftpLocalFilePath);
        originProperties.put(PROPERTIES_FTP_SERVER_DOMAIN, ftpServerDomain);
        originProperties.put(PROPERTIES_FTP_SERVER_DOMAIN_LOGIN, ftpServerDomainLogin);
        originProperties.put(PROPERTIES_FTP_SERVER_DOMAIN_PASSWORD, ftpServerDomainPassword);
        return originProperties;
    }

    public Properties getProperties()
            throws PropertiesUtilException {
        File file = fileUtils.getFileFromFTP(ftpLocalFilePath,
                ftpRemoteFilePath,
                ftpServerDomain,
                ftpServerDomainLogin,
                ftpServerDomainPassword);
        if (file != null) {
            properties = propertiesHandlerUtils.getHandler(file.getPath(), true).getProperties();
        }

        return super.getProperties();
    }

    public String getPropertiesHandlerStrategyName() {
        return PROPERTIES_HANDLER_STRATEGY_NAME;
    }

}