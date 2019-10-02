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

package com.araguacaima.commons.utils;

import com.araguacaima.commons.exception.core.ApplicationException;
import com.araguacaima.commons.exception.core.Exceptions;
import com.araguacaima.commons.exception.core.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Clase utilitaria para ayudar al desarrollador a manejar las excepciones
 * Clase: ExceptionUtil.java <br>
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */
@SuppressWarnings("WeakerAccess")

public class ExceptionUtils {

    private static final Logger log = LoggerFactory.getLogger(ExceptionUtils.class);

    private static final ExceptionUtils INSTANCE = ExceptionUtils.getInstance();
    ;

    private ExceptionUtils() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static ExceptionUtils getInstance() {
        return INSTANCE;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cannot clone instance of this class");
    }

    /**
     * Elimina las excepciones anidadas, hasta llegar a la inicial
     * Excepciones especiales que quizas se deban manejar:
     * - UnexpectedException
     * - RemoteException
     * - SecurityException
     * - EJBException
     *
     * @param e Exception a limpiar
     * @return Exception limpia
     */
    public Exception cleanException(Exception e) {
        if (null == e) {
            return new TechnicalException(Exceptions.UNKNOWN_ERROR, null);
        }
        // log.debug("Cleaning exception of class '" + e.getClass().getName() + "': " + cleanMessage(e));
        if (e instanceof ApplicationException) {
            // Si es una excepcion manejada, la relanzo
            return e;
        } else if (e.getCause() == null) {
            // Si no esta manejada, y no tiene causa, la manejo
            return new TechnicalException(Exceptions.UNKNOWN_ERROR, e);
        } else {
            // Si no esta manejada y tiene una causa, sigo limpiando
            return cleanException((Exception) e.getCause());
        }
    }

    /**
     * Metodo que limpia el mensaje de una excepcion, mostrando un subString del mismo.
     *
     * @param e Exception con el mensaje a limpiar
     * @return String con el mensaje truncado
     */
    public String cleanMessage(Exception e) {
        return cleanMessage((null == e) ? "" : e.getMessage());
    }

    /**
     * Metodo que limpia el mensaje de una excepcion, mostrando un subString del mismo.
     *
     * @param message String con el mensaje a limpiar
     * @return String con el mensaje truncado
     */
    public String cleanMessage(String message) {
        if ((StringUtils.isEmpty(message)) || (!message.contains("\n"))) {
            return message;
        } else {
            return message.substring(0, message.indexOf("\n"));
        }
    }

    /**
     * Metodo que limpia el mensaje de una excepcion, mostrando un subString del mismo.
     *
     * @param message   String con el mensaje a limpiar
     * @param finalLine int con la linea final a mostrar
     * @return String con el mensaje truncado
     */
    public String cleanMessage(String message, int finalLine) {
        return cleanMessage(message, 0, finalLine);
    }

    /**
     * Metodo que limpia el mensaje de una excepcion, mostrando un subString del mismo.
     *
     * @param message     String con el mensaje a limpiar
     * @param initialLine int con la linea inicial a mostrar
     * @param finalLine   int con la linea final a mostrar
     * @return String con el mensaje truncado
     */
    public String cleanMessage(String message, int initialLine, int finalLine) {
        if (StringUtils.isBlank(message)) {
            return "Error Unknown (null Exception)";
        }
        StringBuilder result = new StringBuilder((finalLine - initialLine) * 100);
        int i = 0;
        String line;
        while (message.contains("\n")) {
            line = message.substring(0, message.indexOf("\n"));
            message = message.substring(message.indexOf("\n") + 1);
            if ((initialLine <= i) && (finalLine >= i)) {
                result.append(line);
            }
            i++;
        }
        return result.toString();
    }

    /**
     * Metodo que limpia el mensaje de una excepcion, mostrando un subString del mismo.
     *
     * @param e         Exception con el mensaje a limpiar
     * @param finalLine int con la linea final a mostrar
     * @return String con el mensaje truncado
     */
    public String cleanMessage(Exception e, int finalLine) {
        return cleanMessage(e, 0, finalLine);
    }

    /**
     * Metodo que limpia el mensaje de una excepcion, mostrando un subString del mismo.
     *
     * @param e           Exception con el mensaje a limpiar
     * @param initialLine int con la linea inicial a mostrar
     * @param finalLine   int con la linea final a mostrar
     * @return String con el mensaje truncado
     */
    public String cleanMessage(Exception e, int initialLine, int finalLine) {
        if (e == null || initialLine >= finalLine) {
            return null;
        } else if (e.getMessage() == null) {
            StringBuilder result = new StringBuilder((finalLine - initialLine) * 100);
            for (int ii = initialLine; ii <= finalLine; ii++) {
                result.append(e.getStackTrace()[ii]).append("\n");
            }
            return result.toString().substring(0, result.toString().length() - 1);
        } else {
            return cleanMessage(e.getMessage(), initialLine, finalLine);
        }
    }

    /**
     * Obtiene el mensaje de una excepcion, con el Locale dado, sin trazas.
     *
     * @param exception Exception de la que saldra el mensaje.
     * @return String con el mensaje localizado.
     */
    public String getMessage(Exception exception) {
        return getMessage(exception, Locale.getDefault());
    }

    /**
     * Obtiene el mensaje de una excepcion, con el Locale dado, sin trazas.
     *
     * @param exception Exception de la que saldra el mensaje.
     * @param locale    Locale a usar para obtener el mensaje.
     * @return String con el mensaje localizado.
     */
    public String getMessage(Exception exception, Locale locale) {
        if (exception instanceof ApplicationException) {
            ApplicationException ie = (ApplicationException) exception;
            return Exceptions.getMessage(ie.getKeyCode(), locale) + ie.getExtendedMessage();
        } else {
            // TODO: Analizar si debemos "limpiar" esta Exception
            return exception.getMessage();
        }
    }

    /**
     * Procesa una excepcion.
     * La idea de este metodo es que sea lo existente dentro de
     * la mayoria de los catchs de la aplicacion.
     *
     * @param e Exception a ser manejada
     * @throws ApplicationException (TechnicalException, ApplicationException)
     *                              de acuerdo a la excepcion procesada.
     */
    public void handleException(Exception e) {
        handleException(Exceptions.NESTED_EXCEPTION, e);
    }

    /**
     * Procesa una excepcion.
     * La idea de este metodo es que sea lo existente dentro de
     * la mayoria de los catchs de la aplicacion.
     *
     * @param messageKey String con el codigo del error a ser enviado.
     * @param e          Exception a ser manejada
     * @throws ApplicationException (TechnicalException, ApplicationException)
     *                              de acuerdo a la excepcion procesada.
     * @see Exceptions
     */
    @SuppressWarnings("ThrowableNotThrown")
    public void handleException(String messageKey, Exception e) {
        e = cleanException(e);
        if (e instanceof ApplicationException) {
            new ApplicationException(messageKey, e);
        } else {
            new TechnicalException(messageKey, e);
        }
    }

}
