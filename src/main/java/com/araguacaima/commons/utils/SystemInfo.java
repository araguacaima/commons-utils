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

import com.araguacaima.commons.exception.MessageHandler;
import com.araguacaima.commons.exception.core.Exceptions;
import com.araguacaima.commons.exception.core.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

/**
 * Clase para manejar la informacion (parametros, valores de configuracion) de
 * una aplicacion.
 * - Aqui van propiedades de carga del sistema.
 * - Pueden cargarse de un .properties, de BD, etc.
 * - Las etiquetas cargadas de archivo o BD seran manejadas con MessageHandler.
 * Por hacer:
 * - Cargar informacion de archivos XML
 * - Definir un Servlet que recargue dichas propiedades.
 * <br>
 * Clase: SystemInfo.java <br>
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */

// TODO: Sacar un padre comun a esta clase y MessageHandler
@Component
public class SystemInfo {

    public static final String APPLICATION_FOLDER_ROOT = "application.folder.root";
    public static final String CURRENCY = "currency";
    public static final int DEFAULT_NUMBER = 0;
    public static final String DIRECTORY = "directory"; // folder?
    public static final String LOCALE = "locale";
    public static final int OS_MAC = 3;
    public static final int OS_UNIX = 2;
    public static final int OS_WINDOWS = 1;
    // Propiedades bien conocidas
    public static final String PRECISION = "precision";
    /**
     * Contenedor de las propiedades leidas.
     * - Si uniqueName == false, se usara el mapa directamente, siendo el key y
     * value de cada par el key y value de la propiedad en cuestion.
     * - Si uniqueName == true, el key de cada par sera el nombre del origen de
     * datos (nombre del archivo, nombre de la tabla), y el value sera un mapa
     * con key y value iguales al key y value de la propiedad en cuestion.
     */
    public static final Hashtable<String, Hashtable<String, String>> properties = new Hashtable<>();
    private static final String DEFAULT_ORIGIN = "default";
    private final FileUtils fileUtils;
    private final Logger log = LoggerFactory.getLogger(SystemInfo.class);
    private final NumberUtils numberUtils;
    // Si es true, agrega al nombre el origen de la propiedad para que no se repita
    private boolean uniqueName = false;

    @Autowired
    private SystemInfo(NumberUtils numberUtils, FileUtils fileUtils) {
        this.numberUtils = numberUtils;
        this.fileUtils = fileUtils;
    }

    public static Locale getLocale() {
        try {
            String localeCode = get(LOCALE);
            if (null == localeCode) {
                return Locale.getDefault();
            } else {
                // TODO: No recargar.  Definir como un singleton
                return new Locale(localeCode);
            }
        } catch (Exception e) {
            return Locale.getDefault();
        }
    }

    /**
     * Obtiene el valor de la propiedad propertyName
     *
     * @param propertyName String con el nombre de la propiedad
     * @return String con el valor de la propiedad propertyName
     */
    public static String get(String propertyName) {
        // if (propertyName.equals("p_NumeroMaxRegistrosPorBusqueda")) return "3";
        Hashtable<String, String> hashtable = properties.computeIfAbsent(DEFAULT_ORIGIN, s -> new Hashtable<>());
        return hashtable.get(propertyName);
    }

    /* Agrega informacion al SystemInfo desde el archivo indicado por fileName.
     *
     * @param fileName String con el nombre del archivo
     * @return int con el conteo de las propiedades agregadas
     */
    public int addDataFromExternalFile(String fileName) {
        int count = 0;
        try {
            Properties bundle = new Properties();
            FileInputStream fis = new FileInputStream(fileName);
            bundle.load(fis);
            fis.close();
            Enumeration keys = bundle.keys();
            count = getCount(count, bundle, keys);
        } catch (Exception e) {
            log.error("Error adding data from file '" + fileName + "'.  No data will be added from this file.", e);

            log.error("Error adding data from file '" + fileName + "'.  No data will be added from this " + "file" +
                    ".");
            log.error(e.getMessage());
            // Se podria hacer return -1 aqui si queremos un codigo de error.
        }
        // log.debug("count = " + count);
        return count;
    }

    private int getCount(int count, Properties bundle, Enumeration keys) {
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            try {
                // log.debug(key + " = " + bundle.getProperty(key));
                // put(key, bundle.getString(key));
                put(key, bundle.getProperty(key));
                count++;
            } catch (Exception e) {
                log.error("Error adding data for property '" + key + "'.  No data will be added for this property.", e);
            }
        }
        return count;
    }

    /**
     * Agrega el valor propertyValue para la propiedad propertyName
     *
     * @param propertyName  String con el nombre de la propiedad
     * @param propertyValue String con el valor de la propiedad a agregar
     * @return String con el valor de la propiedad propertyName
     */
    public String put(String propertyName, String propertyValue) {
        Hashtable<String, String> hashtable = properties.computeIfAbsent(DEFAULT_ORIGIN, s -> new Hashtable<>());
        return hashtable.put(propertyName, propertyValue);
    }

    /**
     * Metodo que limpia la data de SystemInfo
     * Solo debe ser usado para tratar de recargar la data, o para las pruebas.
     */
    public void clearData() {
        // log.debug("Limpiando data...");
        properties.clear();
    }

    /**
     * Obtiene el valor de la propiedad propertyName
     *
     * @param propertyName String con el nombre de la propiedad
     * @param origin       String con el nombre del archivo o tabla de donde se lee la propiedad
     * @return String con el valor de la propiedad propertyName
     */
    public String get(String propertyName, String origin) {
        // if (propertyName.equals("p_NumeroMaxRegistrosPorBusqueda")) return "3";
        if (uniqueName) {
            Hashtable innerMap = properties.get(origin);
            return (null == innerMap) ? null : (String) innerMap.get(propertyName);
        } else {
            return get(propertyName);
        }
    }

    /**
     * Devuelve el valor de una propiedad como un boolean.
     * La propiedad puede tener valores de la forma true/false o 1/0
     * Por estandard, true == 1 y false == 0
     *
     * @param propertyName String con el nombre de la propiedad a buscar
     * @return boolean true si la propiedad vale true o 1, false si no.
     */
    public boolean getAsBoolean(String propertyName) {
        String value = null;
        try {
            value = get(propertyName);
            if (numberUtils.isAnInteger(value)) {
                return (Integer.parseInt(value) == 1);
            } else {
                //return (null != value) && Boolean.parseBoolean(value);
                return (null != value) && Boolean.parseBoolean(value);
            }
        } catch (Exception e) {
            log.error("Error getting property '" + propertyName + "' value as boolean (" + value + ")");
            return false;
        }
    }

    public double getAsDouble(String propertyName) {
        String value = null;
        try {
            value = get(propertyName);
            return (null == value) ? DEFAULT_NUMBER : Double.parseDouble(value);
        } catch (Exception e) {
            log.error("Error getting property '" + propertyName + "' value as double (" + value + ")");
            return DEFAULT_NUMBER;
        }
    }

    public SystemInfo getInstance(String fileName) {
        addDataFromFile(fileName);
        return this;
    }

    /**
     * Agrega informacion al SystemInfo desde el archivo indicado por fileName.
     *
     * @param fileName String con el nombre del archivo
     * @return int con el conteo de las propiedades agregadas
     */
    @SuppressWarnings("UnusedReturnValue")
    public int addDataFromFile(String fileName) {
        int count = 0;
        try {
            // ResourceBundle bundle = ResourceBundle.getBundle(fileName);
            String path;
            try {
                path = fileUtils.findFilePath(fileName);
            } catch (Exception e) {
                log.info("No se ubico el archivo llamado:  " + fileName + " en el contexto del dominio");
                //YJA Se intentara cargar el archivo asumiendo que la ruta fileName esta correcta.
                log.info(" Cargardo archivo " + fileName + " fuera del contexto");
                path = fileName;
            }

            log.debug("_I SystemInfo inicia lectura de archivo: " + fileName);
            Properties bundle = new Properties();
            try {
                FileInputStream fis = new FileInputStream(path);
                bundle.load(fis);
                fis.close();
            } catch (FileNotFoundException e) {
                try {
                    fileUtils.loadBundleAsResource(fileName, bundle);
                } catch (Exception e1) {
                    log.error("Error leyendo archivo '" + fileName + "'");
                    log.error("Error leyendo archivo '" + fileName + "'");
                    e1.printStackTrace();
                }
            }

            // Enumeration keys = bundle.getKeys();
            Enumeration keys = bundle.keys();
            count = getCount(count, bundle, keys);
        } catch (Exception e) {
            log.error("Error adding data from file '" + fileName + "'.  No data will be added from this file.", e);

            log.error("Error adding data from file '" + fileName + "'.  No data will be added from this " + "file" +
                    ".");
            log.error(e.getMessage());
            // Se podria hacer return -1 aqui si queremos un codigo de error.
        }
        return count;
    }

    public String getLabel(String propertyName, String country) {
        // TODO: Soportar el Locale para el bundle
        return getLabel(propertyName);
    }

    /**
     * Obtiene el valor de la etiqueta propertyName
     *
     * @param propertyName String con el nombre de la etiqueta
     * @return String con el valor de la etiqueta propertyName
     */
    public String getLabel(String propertyName) {
        String value = get(propertyName);
        return (null == value) ? MessageHandler.PRE + propertyName + MessageHandler.POST : value;
    }

    /**
     * Obtiene la precision del sistema
     * Si la precision en codigo es de menos de 0 digitos, busca en el bundle.
     * Esto es para el caso en el que se necesite sobrecargarla de alguna forma,
     * o ahorrarnos el archivo de propiedades.
     *
     * @return int con la precision a usar en el sistema
     */
    public int getPrecision() {
        return getAsInt(PRECISION);
    }

    /**
     * Obtiene el valor de la propiedad propertyName como un number entero.
     * NOTA: Si una propiedad un valor superior a 2.147.483.647 (inclusive) o
     * menor a -2.147.483.648 (inclusive) no se podra usar este metodo; sera
     * necesario hacer un getAsLong(...) o usar el get(...) normal y parsear el
     * resultado.
     *
     * @param propertyName String con el nombre de la propiedad a usar
     * @return int con el valor de la propiedad propertyName
     */
    public int getAsInt(String propertyName) {
        String value = null;
        try {
            value = get(propertyName);
            return (null == value) ? DEFAULT_NUMBER : Integer.parseInt(value);
        } catch (Exception e) {
            log.error("Error getting property '" + propertyName + "' value as int (" + value + ")");
            return DEFAULT_NUMBER;
        }
    }

    public String getSystemCharset() {
        // not crossplateform safe
        return System.getProperty("file.encoding");
        // jdk1.4
        // return new java.io.OutputStreamWriter(new java.io.ByteArrayOutputStream()).getEncoding();
        // jdk1.5
        // return log.debug(java.nio.charset.Charset.defaultCharset().name());
    }

    public String getSystemPath() {
        return get(APPLICATION_FOLDER_ROOT);
    }

    public boolean isWindows() {
        return findOperatingSystem() == OS_WINDOWS;
    }

    public int findOperatingSystem() {
        String osName = System.getProperty("os.name").toUpperCase();
        // log.debug("Operating System: '" + osName + "'");
        if (osName.contains("WINDOWS")) {
            // Win 3.X
            return OS_WINDOWS;
            //        } else if (osName.indexOf("95") != -1) {
            //            // Win 95
            //            return OS_WINDOWS;
        } else if (osName.contains("LINUX")) {
            // Linux
            return OS_UNIX;
        } else if (osName.contains("SUNOS")) {
            // Linux
            return OS_UNIX;
        } else if (osName.contains("SOLARIS")) {
            // Linux
            return OS_UNIX;
        } else if (osName.contains("MAC")) {
            // Linux
            return OS_MAC;
        } else {
            // ???
            throw new TechnicalException(Exceptions.INVALID_SO, new Exception(), osName);
        }
    }

    /**
     * Agrega el valor propertyValue para la propiedad propertyName
     *
     * @param propertyName  String con el nombre de la propiedad
     * @param propertyValue String con el valor de la propiedad a agregar
     * @param origin        String con el nombre del archivo o tabla de donde se lee la propiedad
     * @return String con el valor de la propiedad propertyName
     */
    public String put(String propertyName, String propertyValue, String origin) {
        if (uniqueName) {
            Hashtable<String, String> innerMap = properties.computeIfAbsent(origin, k -> new Hashtable<>());
            // TODO: Analizar si siempre se colocara la propiadad en el mapa "plano"
            // properties.put(propertyName, propertyValue);
            return innerMap.put(propertyName, propertyValue);
        } else {
            return put(propertyName, propertyValue);
        }
    }

    public void setUniqueName(boolean uniqueName) {
        this.uniqueName = uniqueName;
    }
}
