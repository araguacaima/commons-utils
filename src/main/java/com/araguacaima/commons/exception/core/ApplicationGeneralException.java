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

import java.util.Calendar;
import java.util.Date;

/**
 * Embed all types of consumer application problems.
 *
 * @see com.araguacaima.commons.exception.core.ApplicationGeneralExceptionImpl
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA) araguacaima@gmail.com
 * <br>
 * Changes:<br>
 * <ul>
 * <li> 2017-09-07 | (AMMA) | Class creation. </li>
 * </ul>
 */
public abstract class ApplicationGeneralException extends Throwable {

    public static final String PRINT_STACK_TRACE = "PRINT_STACK_TRACE";
    private static final long serialVersionUID = -4176197311797600886L;
    protected EnvironmentThrowableInfo environmentThrowableInfo;

    protected Date occurenceDateAndTime;

    protected ApplicationGeneralException() {
        super();
        occurenceDateAndTime = Calendar.getInstance().getTime();
    }

    protected ApplicationGeneralException(Throwable throwable) {
        super(throwable);
        occurenceDateAndTime = Calendar.getInstance().getTime();
    }

    protected ApplicationGeneralException(String message) {
        super(message);
        environmentThrowableInfo = new EnvironmentThrowableInfo(new Throwable(), this.getClass().getName());
    }

    protected ApplicationGeneralException(String message, Throwable throwable) {
        super(message, throwable);
        environmentThrowableInfo = new EnvironmentThrowableInfo(new Throwable(), this.getClass().getName());
    }

    public EnvironmentThrowableInfo getEnvironmentThrowableInfo() {
        return environmentThrowableInfo;
    }

    public Date getOccurrenceDateAndTime() {
        return occurenceDateAndTime;
    }
}
