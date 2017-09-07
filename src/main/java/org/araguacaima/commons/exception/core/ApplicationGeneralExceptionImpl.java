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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Single implementation that embed all types of consumer application problems.
 *
 * @see org.araguacaima.commons.exception.core.ApplicationGeneralExceptionImpl
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA) araguacaima@gmail.com
 * <br>
 * Changes:<br>
 * <ul>
 * <li> 2017-09-07 | (AMMA) | Class creation. </li>
 * </ul>
 */
public class ApplicationGeneralExceptionImpl extends ApplicationGeneralException {
    private static final long serialVersionUID = -5229786083297322790L;

    public ApplicationGeneralExceptionImpl(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ApplicationGeneralExceptionImpl(String message) {
        super(message);
    }

    public Throwable fillInStackTrace() {
        return null;
    }

    public Throwable getCause() {
        return null;
    }

    public String getLocalizedMessage() {
        return null;
    }

    public String getMessage() {
        return null;
    }

    public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
    }

    public void setStackTrace(StackTraceElement[] stackTraceElements) {

    }

    public Throwable initCause(Throwable throwable) {
        return null;
    }

    public void printStackTrace() {

    }

    public void printStackTrace(PrintStream printStream) {

    }

    public void printStackTrace(PrintWriter printWriter) {

    }

    public String toString() {
        return null;
    }

}
