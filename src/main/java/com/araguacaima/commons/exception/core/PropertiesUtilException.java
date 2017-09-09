package com.araguacaima.commons.exception.core;

public class PropertiesUtilException extends Exception {

    public static final String CONFIG_FILE_ERROR
            = "No se pudo cargar el archivo de configuracion, error asociado a la excepcion: ";
    public static final String CONFIG_FILE_NAME_EMPTY = "No se ha suministardo ningún nombre de archivo ";
    public static final String CONFIG_KEY_ERROR
            = "No se pudo encontrar la clave (key) en el archivo de configuracion, error asociado a la excepcion: ";
    public static final String IOEXCEPTION_ERROR = "Error asociado a la excepcion IOEXCEPTION: ";
    public static final String INPUT_STREAM_NULL = "No fue posible cargar el archivo como imputStream, null";
    public static final String ERROR = "Error asociado a la excepcion: ";

    public PropertiesUtilException(String errorMsg) {
        super(errorMsg);
    }
}
