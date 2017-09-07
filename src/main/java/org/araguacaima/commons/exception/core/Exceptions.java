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

package org.araguacaima.commons.exception.core;

import org.araguacaima.commons.exception.MessageHandler;

import java.util.Hashtable;
import java.util.Locale;

/**
 * Commonly used error codes in applications. Each application can define extra codes loaded thru MessageHandler from
 * a bundle named <code>MessageHandler.EXCEPTIONS + MessageHandler.PROPERTIES (bundleName)</code>.
 *
 * @see org.araguacaima.commons.exception.MessageHandler
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA) araguacaima@gmail.com
 * <br>
 * Changes:<br>
 * <ul>
 * <li> 2017-09-07 | (AMMA) | Class creation. </li>
 * </ul>
 */

public class Exceptions {

    public static final String INVALID_PARAMETERS = "INVALID_PARAMETERS";
    public static final String INVALID_SO = "INVALID_SO";
    public static final String NESTED_EXCEPTION = "NESTED";
    public static final String PARAMETERS_NOT_FOUND = "PARAMETERS_NOT_FOUND";
    public static final String UNKNOWN_ERROR = "UNKNOWN";
    private static final Hashtable exceptions = new Hashtable();
    private static String bundleName = MessageHandler.EXCEPTIONS + MessageHandler.PROPERTIES;

    /**
     * Gets an error message using its code taking in account user locale from <code>MessageHandler.EXCEPTIONS +
     * MessageHandler.PROPERTIES (bundleName)</code>
     *
     * @param code Message code to be thrown
     * @return The message related to the provided code
     */
    public static String getMessage(String code) {
        return MessageHandler.get(code, getBundleName());
    }

    /**
     * Gets the exceptions bundle name
     *
     * @return The exceptions bundle name
     */
    public static String getBundleName() {
        return bundleName;
    }

    /**
     * Sets the exceptions bundle name for all exceptions instances.
     *
     * @param bundleName The exceptions bundle name
     */
    public static void setBundleName(String bundleName) {
        Exceptions.bundleName = bundleName;
    }

    /**
     * Gets an error message using its code and some locale
     *
     * @param code   Message code to be thrown
     * @param locale Locale for customize message language and format
     * @return The message related to the incoming code according to the provided locale
     */
    public static String getMessage(String code, Locale locale) {
        return MessageHandler.get(code, getBundleName());
    }

}