package org.araguacaima.commons.exception.core;

import org.araguacaima.commons.exception.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Representa los problemas tecnicos que el usuario final no puede resolver,
 * como excepciones de BD (java.sql.SQLException) y similares.
 * Normalmente es un wrap que envuelve la excepcion original, aunque puede
 * darse el caso de que no tenga una excepcion interna.
 * Dado que es una excepcion Runtime, no debe ser lanzada dentro de un EJB.
 * Title: TechnicalException.java
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */

public class TechnicalException extends GeneralException {

    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    private static final long serialVersionUID = -8689850436632874402L;
    // Original exception
    protected final Throwable cause;

    /**
     * Convenience method that creates an exception with severity ERROR (the most commonly used severity),
     * with a short message describing it and the original generated exception.
     *
     * @param detailMessage String
     * @param cause         Exception
     */
    public TechnicalException(String detailMessage, Throwable cause) {
        super(detailMessage, Severity.ERROR, cause.getMessage());
        this.cause = cause;
    }

    /**
     * Convenience method that creates an exception with severity ERROR (the most commonly used severity),
     * with a short message describing it and the original generated exception.
     *
     * @param detailMessage String
     */
    public TechnicalException(String detailMessage) {
        super(detailMessage, Severity.ERROR);
        this.cause = new Throwable();
    }

    /**
     * Creates an exception of a given {@link org.araguacaima.commons.exception.core.Severity}, with a short message
     * describing it and the original
     * generated exception.
     *
     * @param detailMessage String
     * @param severity      Severity
     * @param cause         Exception
     */
    public TechnicalException(String detailMessage, Severity severity, Throwable cause) {
        super(detailMessage, severity, cause.getMessage());
        this.cause = cause;
    }

    /**
     * Creates a Technical Exception with some extra info
     *
     * @param detailMessage          String
     * @param cause                  Exception
     * @param additional_information String
     */
    public TechnicalException(String detailMessage, Throwable cause, String additional_information) {
        super(detailMessage, Severity.ERROR, additional_information);
        this.cause = cause;
    }

    /**
     * Creates an exception of a given {@link org.araguacaima.commons.exception.core.Severity}, with a short message
     * describing it and the original
     * generated exception.
     *
     * @param detailMessage          String
     * @param severity               Severity
     * @param cause                  Exception
     * @param additional_information String
     */
    public TechnicalException(String detailMessage, Severity severity, Throwable cause, String additional_information) {
        super(detailMessage, severity, additional_information + " " + cause.getMessage());
        this.cause = cause;
    }

    /**
     * Returns the wrapped exception
     *
     * @return Exception
     */
    public Throwable getOriginalException() {
        return this.cause;
    }

    /**
     * @param stream PrintStream
     */
    public void printStackTrace(PrintStream stream) {
        this.printStackTrace(new PrintWriter(stream));
    }

    public void printStackTrace(PrintWriter printWriter) {
        super.printStackTrace(printWriter);
        if (this.cause != null) {
            printWriter.println(Exceptions.NESTED_EXCEPTION);
            this.cause.printStackTrace(printWriter);
        }
    }

    /**
     *
     */
    public void printStackTrace() {
        super.printStackTrace();
        // Usaremos System.err.println debido a que es una clase de bajo nivel.
        // No sabemos si tenemos un log vivo en este momento.
        if (this.cause != null) {
            System.err.println(Exceptions.NESTED_EXCEPTION);
            log.error(Exceptions.NESTED_EXCEPTION);
            this.cause.printStackTrace();
        }
    }

}