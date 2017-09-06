package org.araguacaima.commons.exception.core;

import org.araguacaima.commons.exception.MessageHandler;

/**
 * Clase principal del modulo de manejo de excepciones (org.araguacaima.commons.exception).
 * Todas las excepciones especializadas heredan de esta clase.
 * No se ha de usar esta clase directamente, sino alguna de sus subclases.
 * Dado que es una excepcion Runtime, ninguna de sus hijas debe ser lanzada dentro de un EJB.
 * Title: GeneralException.java
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */

public abstract class GeneralException extends RuntimeException {

    private static final long serialVersionUID = 3172918925590622749L;
    // Codigo del mensaje a mostrar, para usar en el bundle asociado
    private final String code;
    protected Object magicValue;
    // Severidad de la excepcion
    protected Severity severity;
    // Mensaje a ser mostrado "as is" como parte del mensaje final
    private String extendedMessage;

    /**
     * Create an GeneralException of a given {@link org.araguacaima.commons.exception.core.Severity} with a message
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