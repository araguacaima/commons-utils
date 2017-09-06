package org.araguacaima.commons.exception.core;

/**
 * Representa los problemas de negocio, o aquellos a los que el usuario final
 * puede corregir, como validaciones y demas.
 * Dado que es una excepcion Runtime, no debe ser lanzada dentro de un EJB.
 * Title: ApplicationException.java
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
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