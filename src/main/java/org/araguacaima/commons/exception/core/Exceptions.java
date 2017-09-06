package org.araguacaima.commons.exception.core;

import org.araguacaima.commons.exception.MessageHandler;

import java.util.Hashtable;
import java.util.Locale;

/**
 * Clase que contiene los codigos de error utilizados conmunmente en las
 * aplicaciones.  Cada aplicacion puede tener codigos extra.
 * <br>
 * En general, esta clase debe contener todos los codigos de las excepciones
 * bien conocidas que pueda tener una aplicacion.  Aqui estaran los mas
 * generales (i.e. los que no sean particulares de una aplicacion, sino comunes
 * a todas) y cada aplicacion tendra otra clase con sus codigos particulares.
 * <br>
 * En general todos los codigos (y las constantes que los representan) tendran
 * siempre el mismo estilo, descrito a continuacion:
 * - Codigo:
 * - Nombre de la constante:
 * - Sigue los estandares de nomenclatura de constante.
 * <br>
 * Title: Exceptions.java <br>
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */

public class Exceptions {

    public static final String DB_DRIVER_NOT_FOUND = "BD0012";
    public static final String DB_ERROR_CLOSING_CONNECTION = "BD0013";
    public static final String DB_ERROR_COMMITING_TX = "BD0009";
    public static final String DB_ERROR_DELETING_OBJECT = "BD0005";
    public static final String DB_ERROR_GETTING_CONNECTION = "BD0006";
    public static final String DB_ERROR_LOADING_OBJECT = "BD0002";
    public static final String DB_ERROR_ROLLING_BACK_TX = "BD0008";
    public static final String DB_ERROR_SAVING_OBJECT = "BD0004";
    public static final String DB_ERROR_SETTING_PARAMETER = "BD0010";
    public static final String DB_ERROR_STARTING_TX = "BD0007";
    public static final String DB_ERROR_UPDATING_OBJECT = "BD0003";
    public static final String DB_TABLE_NOT_FOUND = "BD0011";
    /* Mensajes de BD */
    public static final String DB_UNKNOWN_ERROR = "BD0001";
    public static final String EXTERNAL_EXCEPTION = "EXTERNAL";
    public static final String FILE_ERROR_DELETING_FILE = "FL0003";
    /* Mensajes de Archivos */
    public static final String FILE_NOT_FOUND = "FL0001";
    public static final String FILE_PARSING_ERROR = "FL0002";
    public static final String GEInvalidUserContext = "GE0004";
    /* General (can happen in any module) */
    public static final String GEParsingDate = "GE0001";
    public static final String GEParsingNumber = "GE0002";
    public static final String GERequiredDate = "GE0003";
    public static final String GEUnknownErrorSettingUserContext = "GE0005";
    public static final String HS_ERROR_CLOSING_SESSION = "HS0007";
    public static final String HS_ERROR_COMMITING_TX = "HS0012";
    public static final String HS_ERROR_DELETING_OBJECT = "HS0005";
    public static final String HS_ERROR_GETTING_CONNECTION = "HS0009";
    public static final String HS_ERROR_LOADING_LIST = "HS0006";
    public static final String HS_ERROR_LOADING_OBJECT = "HS0002";
    public static final String HS_ERROR_LOADING_QUERY = "HS0008";
    /* Hibernate & Spring */
    public static final String HS_ERROR_OPENING_SESSION = "HS0001";
    public static final String HS_ERROR_ROLLING_BACK_TX = "HS0011";
    public static final String HS_ERROR_SAVING_OBJECT = "HS0004";
    public static final String HS_ERROR_SETTING_PARAMETER = "HS0013";
    public static final String HS_ERROR_STARTING_TX = "HS0010";
    public static final String HS_ERROR_UPDATING_OBJECT = "HS0003";
    /* Mensajes Genericos */
    public static final String INVALID_PARAMETERS = "MG0001";
    public static final String INVALID_SO = "MG0003";
    public static final String NESTED_EXCEPTION = "NESTED"; // Ultimo recurso
    public static final String PARAMETERS_NOT_FOUND = "MG0002";
    /* Mensajes de Seguridad */
    public static final String SE_USER_NOT_FOUND = "SE0001";
    public static final String SE_WRONG_PASSWORD = "SE0002";
    public static final String SE_WRONG_ROLE = "SE0003";
    public static final String TEST_MESSAGE = "TESTMESSAGE"; // Para pruebas
    /**
     * NOTA:
     * Todos estos codigos son para mensajes comunes a cualquier aplicacion.
     * Para los mensajes particulares a cada aplicacion, se deben manejar
     * constantes en alguna clase interna a la aplicacion, siguiendo la misma
     * nomenclatura que en esta clase, o siguiendo aquella definida para la
     * aplicacion en cuestion.
     */

    /* Codigos Internos */
    public static final String UNKNOWN_ERROR = "UNKNOWN"; // Ultimo recurso
    private static final Hashtable exceptions = new Hashtable();
    private static String bundleName = MessageHandler.EXCEPTIONS + MessageHandler.PROPERTIES;

    /**
     * Gets an error message using its code and the UserInfo's locale
     *
     * @param code String
     * @return String
     */
    public static String getMessage(String code) {
        // NOTA: Agregar a la inicializacion de Spring o a un bloque estatico lo siguiente:
        // MessageHandler.addDataFromFile(getBundleName());
        return MessageHandler.get(code, getBundleName());
        // return getMessageFromBundle(code, getBundle());
    }

    /**
     * Metodo de conveniencia para el caso que el archivo de los mensajes de
     * error no se llame exceptions.properties
     *
     * @return String con el nombre del bundle
     */
    public static String getBundleName() {
        return bundleName;
    }

    /**
     * Metodo de conveniencia para el caso que el archivo de los mensajes de
     * error no se llame exceptions.properties
     *
     * @param _bundleName String con el nombre del bundle
     */
    public static void setBundleName(String _bundleName) {
        bundleName = _bundleName;
    }

    /**
     * Gets an error message using its code and some locale
     *
     * @param code   String
     * @param locale Locale
     * @return String
     */
    public static String getMessage(String code, Locale locale) {
        // NOTA: Agregar a la inicializacion de Spring o a un bloque estatico lo siguiente:
        // MessageHandler.addDataFromFile(getBundleName(), locale);
        return MessageHandler.get(code, getBundleName());
    }

    /**
     * Atrapa la excepcion lanzada y la envuelve en una excepcion
     * Uso: throw GeneralException.handleException(claveDelError, e);
     * No lanzamos la GeneralException directamente porque los IDEs detectan error.
     *
     * @param e           Exception detectada a manejar
     * @param messageCode String con el codigo a usar en la nueva excepcion
     * @return GeneralException a lanzar
     */
    public static GeneralException handleException(Exception e, String messageCode) {
        if (e instanceof GeneralException) {
            throw (GeneralException) e;
        } else {
            throw new TechnicalException((null == messageCode) ? UNKNOWN_ERROR : messageCode, e);
        }
    }

}