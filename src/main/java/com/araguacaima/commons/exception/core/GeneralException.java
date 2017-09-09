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

package com.araguacaima.commons.exception.core;

import com.araguacaima.commons.exception.MessageHandler;

/**
 * Exception handling module main class. All specialized exceptions inherit from this class. This class should not be
 * used directly, but one of its subclasses.
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA) araguacaima@gmail.com
 * <br>
 * Changes:<br>
 * <ul>
 * <li> 2017-09-07 | (AMMA) | Class creation. </li>
 * </ul>
 */

public abstract class GeneralException extends RuntimeException {

    private static final long serialVersionUID = 3172918925590622749L;

    /**
     * Message code to display, for use in the associated bundle
     */
    private final String code;
    protected Object magicValue;
    /**
     * Severity of the exception
     */
    protected Severity severity;
    /**
     * Message to be displayed "as is" as part of the final message
     */
    private String extendedMessage;

    /**
     * Create an {@link GeneralException} of a given {@link Severity} with a message
     * detailing it
     *
     * @param code     String
     * @param severity Severity
     */
    protected GeneralException(String code, Severity severity) {
        super(Exceptions.getMessage(code));
        this.code = code;
        this.severity = severity;
    }

    protected GeneralException(String code, Exception e) {
        super(Exceptions.getMessage(code));
        this.code = code;
        this.initCause(e);
    }

    protected GeneralException(String code, Severity severity, Object[] params) {
        // super(Exceptions.getMessage(code));
        super(MessageHandler.get(code, params, Exceptions.getBundleName()));
        this.code = code;
        this.severity = severity;
    }

    protected GeneralException(String code, Severity severity, String extendedMessage) {
        super(Exceptions.getMessage(code) + ((extendedMessage == null || extendedMessage.equals("")) ? "" : " " +
                extendedMessage));
        this.severity = severity;
        this.code = code;
        this.extendedMessage = extendedMessage;
    }

    public String getExtendedMessage() {
        // return extendedMessage;
        return (null == extendedMessage) ? "" : extendedMessage;
    }

    public void setExtendedMessage(String extendedMessage) {
        this.extendedMessage = extendedMessage;
    }

    /**
     * Returns the KeyCode for this exception
     *
     * @return String
     */
    public String getKeyCode() {
        return code;
    }

    public Object getMagicValue() {
        return magicValue;
    }

    /**
     * Returns the severity of this exception
     *
     * @return Severity
     */
    public Severity getSeverity() {
        return this.severity;
    }

}