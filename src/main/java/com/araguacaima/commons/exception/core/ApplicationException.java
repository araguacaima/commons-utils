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

/**
 * It represents consumer application problems, or those to which the end user can correct, such as validations,
 * format representation and others. It is not intended to handle business rules errors nor exceptions.
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA) araguacaima@gmail.com
 * <br>
 * Changes:<br>
 * <ul>
 * <li> 2017-09-07 | (AMMA) | Class creation. </li>
 * </ul>
 * @see com.araguacaima.commons.exception.core.GeneralException
 */
public class ApplicationException extends GeneralException {

    private static final long serialVersionUID = -183494513519467642L;

    public ApplicationException(String code) {
        super(code, Severity.FATAL);
    }

    public ApplicationException(String code, Severity severity) {
        super(code, severity);
    }

    public ApplicationException(String code, Severity severity, Object[] params) {
        super(code, severity, params);
    }

    public ApplicationException(String code, Severity severity, String propertyString) {
        super(code, severity, propertyString);
    }

    public ApplicationException(String code, Severity severity, Object extraInfo) {
        super(code, severity);
        this.magicValue = extraInfo;
    }

    public ApplicationException(String code, Severity severity, String propertyString, Object extraInfo) {
        super(code, severity, propertyString);
        this.magicValue = extraInfo;
    }

    public ApplicationException(String messageKey, Exception e) {
        super(messageKey, e);
    }

}