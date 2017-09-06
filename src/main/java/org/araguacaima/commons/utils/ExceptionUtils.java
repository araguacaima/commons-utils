package org.araguacaima.commons.utils;

import org.araguacaima.commons.exception.core.ApplicationException;
import org.araguacaima.commons.exception.core.Exceptions;
import org.araguacaima.commons.exception.core.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
public class ExceptionUtils {

    private static Logger log = LoggerFactory.getLogger(ExceptionUtils.class);
    private final StringUtils stringUtils;

    @Autowired
    public ExceptionUtils(StringUtils stringUtils) {
        this.stringUtils = stringUtils;
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
        if (org.apache.commons.lang3.StringUtils.isBlank(message)) {
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
     * @param messageKey String con el codigo del error a ser enviado.
     * @param e          Exception a ser manejada
     * @throws ApplicationException (TechnicalException, ApplicationException)
     *                              de acuerdo a la excepcion procesada.
     * @see Exceptions
     */
    public void handleException(String messageKey, Exception e) {
        e = cleanException(e);
        if (e instanceof ApplicationException) {
            new ApplicationException(messageKey, e).perform();
        } else {
            new TechnicalException(messageKey, e).perform();
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

    public void main(String[] args) {
        System.out.println("cleanMessage(new Exception(), 3) = " + cleanMessage(new Exception(), 3));
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
        if (e == null) {
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

}
