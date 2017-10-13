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

import com.araguacaima.commons.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;

@SuppressWarnings("UnusedReturnValue")
@Component
public class PropertiesHandlerBuilder {

    public static final String PROPERTIES_HANDLER_STRATEGY_POLICY = "PROPERTIES_HANDLER_STRATEGY_POLICY";
    private static final Logger log = LoggerFactory.getLogger(PropertiesHandlerBuilder.class);
    private String defaultFileName;
    private PropertiesHandlerStrategyInterface propertiesHandlerStrategy;
    private String propertiesHandlerStrategyPolicy = StringUtils.EMPTY;

    private PropertiesHandlerBuilder() {

    }

    public static PropertiesHandlerStrategyInterface buildChainOfResponsibility(String policyString,
                                                                                String defaultFileName) {
        PropertiesHandlerStrategyInterface propertiesHandlerStrategy = new PropertiesHandlerBuilder()
                .createPropertiesHandlerStrategyDefault();
        return getPropertiesHandlerStrategy(policyString, defaultFileName, propertiesHandlerStrategy);
    }

    private static PropertiesHandlerStrategyInterface createPropertiesHandlerStrategyWithoutPolicies(String label,
                                                                                                     String defaultFileName) {
        log.info("Creating a PropertiesHandlerStrategy without Policies based on label '" + label

                + "' and default file name '" + defaultFileName + "'");
        PropertiesHandlerStrategyInterface propertiesHandlerStrategy;
        if (PropertiesHandlerStrategy.PROPERTIES_HANDLER_STRATEGY_DB.equals(label)) {
            propertiesHandlerStrategy = new PropertiesHandlerStrategyDB();
        } else if (PropertiesHandlerStrategy.PROPERTIES_HANDLER_STRATEGY_CLASSPATH.equals(label)) {
            propertiesHandlerStrategy = new PropertiesHandlerStrategyClassPath();
            ((PropertiesHandlerStrategyClassPath) propertiesHandlerStrategy).setFileWithinClasspath(defaultFileName);
        } else if (PropertiesHandlerStrategy.PROPERTIES_HANDLER_STRATEGY_PATH.equals(label)) {
            propertiesHandlerStrategy = new PropertiesHandlerStrategyPath();
            ((PropertiesHandlerStrategyPath) propertiesHandlerStrategy).setFileInPath(defaultFileName);
        } else if (PropertiesHandlerStrategy.PROPERTIES_HANDLER_STRATEGY_FTP.equals(label)) {
            propertiesHandlerStrategy = new PropertiesHandlerStrategyFTP();
        } else if (PropertiesHandlerStrategy.PROPERTIES_HANDLER_STRATEGY_URL.equals(label)) {
            propertiesHandlerStrategy = new PropertiesHandlerStrategyURL();
        } else {
            log.warn("Is not possible to create a PropertiesHandlerStrategy based on label '" + label + "'. The " +
                    "default one will be used");
            propertiesHandlerStrategy = new PropertiesHandlerStrategyDefault();
            ((PropertiesHandlerStrategyDefault) propertiesHandlerStrategy).setDefaultPath(defaultFileName);
        }
        log.info("A PropertiesHandlerStrategy of type '" + label + "' (" + propertiesHandlerStrategy.getClass()
                .getName() + ") has been created!");

        return propertiesHandlerStrategy;
    }

    public static PropertiesHandlerStrategyInterface createPropertiesHandlerStrategyWithoutPoliciesNorFiles() {
        log.warn("The default PropertiesHandlerStrategy will be created with no policies nor properties files");
        PropertiesHandlerStrategyInterface propertiesHandlerStrategy;
        propertiesHandlerStrategy = new PropertiesHandlerStrategyDefault();
        return propertiesHandlerStrategy;
    }

    public static PropertiesHandlerStrategyInterface getPropertiesHandlerStrategy(String policyString,
                                                                                  String defaultFileName,
                                                                                  PropertiesHandlerStrategyInterface
                                                                                          propertiesHandlerStrategy) {
        if (!StringUtils.isBlank(policyString)) {
            String[] policy = policyString.split(";");
            if (policy.length == 1) {
                policy = policyString.split(",");
            }
            setNext(propertiesHandlerStrategy, new ArrayList<>(Arrays.asList(policy)), defaultFileName);
            return propertiesHandlerStrategy.getNext();
        } else {
            return new PropertiesHandlerBuilder().createPropertiesHandlerStrategyDefault();
        }
    }

    private static PropertiesHandlerStrategyInterface setNext(PropertiesHandlerStrategyInterface
                                                                      propertiesHandlerStrategy,
                                                              ArrayList<String> propertiesHandlerStrategyPolicies,
                                                              String defaultFileName) {
        if (propertiesHandlerStrategy != null) {
            if (propertiesHandlerStrategyPolicies != null && propertiesHandlerStrategyPolicies.size() > 0) {
                PropertiesHandlerStrategyInterface next = PropertiesHandlerBuilder
                        .createPropertiesHandlerStrategyWithoutPolicies(

                        (propertiesHandlerStrategyPolicies.get(0)).trim(), defaultFileName);
                propertiesHandlerStrategy.setNext(next);
                propertiesHandlerStrategyPolicies.remove(0);
                setNext(next, propertiesHandlerStrategyPolicies, defaultFileName);
            }
        }
        return propertiesHandlerStrategy;
    }

    private PropertiesHandlerStrategyInterface buildChainOfResponsibility() {
        return getPropertiesHandlerStrategy(propertiesHandlerStrategyPolicy,
                defaultFileName,
                propertiesHandlerStrategy);
    }

    public PropertiesHandlerStrategyInterface buildPropertiesHandlerStrategyPolicies(String label) {
        log.info("Building a PropertiesHandlerStrategy Policy based on label '" + label + "'");
        PropertiesHandlerStrategyInterface propertiesHandlerStrategy;
        if (PropertiesHandlerStrategy.PROPERTIES_HANDLER_STRATEGY_DB.equals(label)) {
            propertiesHandlerStrategy = createPropertiesHandlerStrategyDB();
        } else if (PropertiesHandlerStrategy.PROPERTIES_HANDLER_STRATEGY_CLASSPATH.equals(label)) {
            propertiesHandlerStrategy = createPropertiesHandlerStrategyClassPath();
        } else if (PropertiesHandlerStrategy.PROPERTIES_HANDLER_STRATEGY_PATH.equals(label)) {
            propertiesHandlerStrategy = createPropertiesHandlerStrategyPath();
        } else if (PropertiesHandlerStrategy.PROPERTIES_HANDLER_STRATEGY_FTP.equals(label)) {
            propertiesHandlerStrategy = createPropertiesHandlerStrategyFTP();
        } else if (PropertiesHandlerStrategy.PROPERTIES_HANDLER_STRATEGY_URL.equals(label)) {
            propertiesHandlerStrategy = createPropertiesHandlerStrategyURL();
        } else {
            log.warn("Is not possible to create a PropertiesHandlerStrategy based on label '" + label + "'. The " +
                    "default one will be used");
            propertiesHandlerStrategy = createPropertiesHandlerStrategyDefault();
        }
        log.info("A PropertiesHandlerStrategy of type '" + label + "' (" + propertiesHandlerStrategy.getClass()
                .getName() + ") has been created!");
        return propertiesHandlerStrategy;
    }

    private PropertiesHandlerStrategyInterface createPropertiesHandlerStrategyClassPath() {
        log.info("Creating a CLASSPATH PropertiesHandlerStrategy");
        propertiesHandlerStrategy = new PropertiesHandlerStrategyClassPath();
        if (StringUtils.isBlank(propertiesHandlerStrategyPolicy)) {
            PropertiesHandlerStrategyInterface nextPath = new PropertiesHandlerStrategyPath();
            PropertiesHandlerStrategyInterface nextURL = new PropertiesHandlerStrategyURL();
            PropertiesHandlerStrategyInterface nextFTP = new PropertiesHandlerStrategyFTP();
            nextPath.setNext(nextURL);
            nextURL.setNext(nextFTP);
            propertiesHandlerStrategy.setNext(nextPath);
            log.info("The default chain of responsibilities has loaded because of no strategy policy was found. The "
                    + "current chain of properties' handlers for CLASSPATH strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        } else {
            propertiesHandlerStrategy = buildChainOfResponsibility();
            log.info("A configured chain of responsibilities has been found. The current chain of properties' " +
                    "handlers" + " for CLASSPATH strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        }
        return propertiesHandlerStrategy;
    }

    private PropertiesHandlerStrategyInterface createPropertiesHandlerStrategyDB() {
        log.info("Creating a DB PropertiesHandlerStrategy");
        propertiesHandlerStrategy = new PropertiesHandlerStrategyDB();
        if (StringUtils.isBlank(propertiesHandlerStrategyPolicy)) {

            PropertiesHandlerStrategyInterface nextClassPath = new PropertiesHandlerStrategyClassPath();
            PropertiesHandlerStrategyInterface nextPath = new PropertiesHandlerStrategyPath();
            PropertiesHandlerStrategyInterface nextURL = new PropertiesHandlerStrategyURL();
            PropertiesHandlerStrategyInterface nextFTP = new PropertiesHandlerStrategyFTP();
            nextClassPath.setNext(nextPath);
            nextPath.setNext(nextURL);
            nextURL.setNext(nextFTP);
            propertiesHandlerStrategy.setNext(nextClassPath);
            log.info("The default chain of responsibilities has loaded because of no strategy policy was found. The "
                    + "current chain of properties' handlers for DB strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        } else {
            propertiesHandlerStrategy = buildChainOfResponsibility();
            log.info("A configured chain of responsibilities has been found. The current chain of properties' " +
                    "handlers" + " for DB strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        }
        return propertiesHandlerStrategy;
    }

    private PropertiesHandlerStrategyInterface createPropertiesHandlerStrategyDefault() {
        log.info("Creating a DEFAULT PropertiesHandlerStrategy");
        propertiesHandlerStrategy = new PropertiesHandlerStrategyDefault();
        ((PropertiesHandlerStrategyDefault) propertiesHandlerStrategy).setDefaultPath(defaultFileName);
        if (StringUtils.isBlank(propertiesHandlerStrategyPolicy)) {
            PropertiesHandlerStrategyInterface nextDB = new PropertiesHandlerStrategyDB();
            PropertiesHandlerStrategyInterface nextClassPath = new PropertiesHandlerStrategyClassPath();
            ((PropertiesHandlerStrategyClassPath) nextClassPath).setFileWithinClasspath(defaultFileName);
            PropertiesHandlerStrategyInterface nextPath = new PropertiesHandlerStrategyPath();
            ((PropertiesHandlerStrategyPath) nextPath).setFileInPath(defaultFileName);
            PropertiesHandlerStrategyInterface nextURL = new PropertiesHandlerStrategyURL();
            PropertiesHandlerStrategyInterface nextFTP = new PropertiesHandlerStrategyFTP();
            nextDB.setNext(nextClassPath);
            nextClassPath.setNext(nextPath);
            nextPath.setNext(nextURL);
            nextURL.setNext(nextFTP);
            propertiesHandlerStrategy.setNext(nextDB);
            log.info("The default chain of responsibilities has loaded because of no strategy policy was found. The "
                    + "current chain of properties' handlers for DEFAULT strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        } else {
            propertiesHandlerStrategy = buildChainOfResponsibility();
            log.info("A configured chain of responsibilities has been found. The current chain of properties' " +
                    "handlers" + " for DEFAULT strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        }
        return propertiesHandlerStrategy;
    }

    private PropertiesHandlerStrategyInterface createPropertiesHandlerStrategyFTP() {
        log.info("Creating a FTP PropertiesHandlerStrategy");
        propertiesHandlerStrategy = new PropertiesHandlerStrategyFTP();
        if (!StringUtils.isBlank(propertiesHandlerStrategyPolicy)) {
            propertiesHandlerStrategy = buildChainOfResponsibility();
            log.info("A configured chain of responsibilities has been found. The current chain of properties' " +
                    "handlers" + " for FTP strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        } else {
            log.info("The default chain of responsibilities has loaded because of no strategy policy was found. The "
                    + "current chain of properties' handlers for FTP strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        }
        return propertiesHandlerStrategy;
    }

    private PropertiesHandlerStrategyInterface createPropertiesHandlerStrategyPath() {
        log.info("Creating a PATH PropertiesHandlerStrategy");
        propertiesHandlerStrategy = new PropertiesHandlerStrategyPath();
        if (StringUtils.isBlank(propertiesHandlerStrategyPolicy)) {
            PropertiesHandlerStrategyInterface nextURL = new PropertiesHandlerStrategyURL();
            PropertiesHandlerStrategyInterface nextFTP = new PropertiesHandlerStrategyFTP();
            nextURL.setNext(nextFTP);
            propertiesHandlerStrategy.setNext(nextURL);
            log.info("The default chain of responsibilities has loaded because of no strategy policy was found. The "
                    + "current chain of properties' handlers for PATH strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        } else {
            propertiesHandlerStrategy = buildChainOfResponsibility();
            log.info("A configured chain of responsibilities has been found. The current chain of properties' " +
                    "handlers" + " for PATH strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        }
        return propertiesHandlerStrategy;
    }

    private PropertiesHandlerStrategyInterface createPropertiesHandlerStrategyURL() {
        log.info("Creating an URL PropertiesHandlerStrategy");
        propertiesHandlerStrategy = new PropertiesHandlerStrategyURL();
        if (StringUtils.isBlank(propertiesHandlerStrategyPolicy)) {
            PropertiesHandlerStrategyInterface nextFTP = new PropertiesHandlerStrategyFTP();
            propertiesHandlerStrategy.setNext(nextFTP);
            log.info("The default chain of responsibilities has loaded because of no strategy policy was found. The "
                    + "current chain of properties' handlers for URL strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        } else {
            propertiesHandlerStrategy = buildChainOfResponsibility();
            log.info("A configured chain of responsibilities has been found. The current chain of properties' " +
                    "handlers" + " for URL strategy is: " + getStrategyPolicyChain(
                    propertiesHandlerStrategy));
        }
        return propertiesHandlerStrategy;

    }

    public String getDefaultFileName() {
        return defaultFileName;
    }

    public void setDefaultFileName(String defaultFileName) {
        this.defaultFileName = defaultFileName;
    }

    public String getPropertesHandlerStrategyPolicy() {
        return propertiesHandlerStrategyPolicy;
    }

    public void setPropertesHandlerStrategyPolicy(String propertiesHandlerStrategyPolicy) {
        log.warn("Current properties handler strategy policy was change from: " + this
                .propertiesHandlerStrategyPolicy + " to " + propertiesHandlerStrategyPolicy);
        this.propertiesHandlerStrategyPolicy = propertiesHandlerStrategyPolicy;
    }

    private String getStrategyPolicyChain(PropertiesHandlerStrategyInterface nextSegment) {
        if (nextSegment != null) {
            return nextSegment.getPropertiesHandlerStrategyName().concat(getStrategyPolicyChain(nextSegment.getNext()
            ).concat(
                    ";"));
        } else {
            return StringUtils.EMPTY;
        }
    }

}