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

package com.araguacaima.commons.exception.aop;

import com.araguacaima.commons.exception.core.GeneralException;
import org.aspectj.lang.annotation.Aspect;


/**
 * Provides a way to intercept thrown {@link Throwable} in such a way that it can be wrapped into a new
 * {@link GeneralException}
 * after determining how should be handled such exception using a chain of responsibility pattern.
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA) araguacaima@gmail.com
 * <br>
 * Changes:<br>
 * <ul>
 * <li> 2017-09-07 | (AMMA) | Class creation. </li>
 * </ul>
 * <br>
 * @see com.araguacaima.commons.exception.MessageHandler
 */
@Aspect
public class ExceptionsAspect {

}