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

package com.araguacaima.commons.exception;

import com.araguacaima.commons.utils.PropertiesHandlerUtils;
import com.araguacaima.commons.utils.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

/**
 * Alternative to Resource Bundle in order to load custom messages that going to be used typically in exceptions.
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 * @see java.util.ResourceBundle
 */

@Component
public class MessageHandler {
    public static final String ERRORS = "errors";
    public static final String EXCEPTIONS = "exceptions";
    public static final String LABELS = "labels";
    public static final String POST = "]]";
    public static final String PRE = "[[";
    public static final String PROPERTIES = ".properties";
    private static final String DEFAULT_ORIGIN = "default";
    private static final Hashtable<String, Hashtable<String, String>> labels = new Hashtable<>();
    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    private static Locale forcedLocale;
    private final PropertiesHandlerUtils propertiesHandlerUtils;
    private String defaultFile = null;

    @Autowired
    public MessageHandler(PropertiesHandlerUtils propertiesHandlerUtils) {
        this.propertiesHandlerUtils = propertiesHandlerUtils;
    }

    /**
     * Gets a message/tag according to the requested key
     * ie: <code>getValue("A {0} B {1}", { "C", "D" }) -&gt; "A C B D"</code>
     *
     * @param key    String with the tag/label identifier to display
     * @param params Objects tu substitute
     * @param origin String with the name of the file or table where the label is read
     * @return String with the tag/tag to show
     */
    public static String get(String key, Object[] params, String origin) {
        return get(key, params, origin, getLocale());
    }

    /**
     * Gets a message/tag according to the requested key
     * ej: <code>getValue("A {0} B {1}", { "C", "D" }) -&gt; "A C B D"</code>
     *
     * @param key    String with the tag/label identifier to display
     * @param params Objects tu substitute
     * @param origin String with the name of the file or table where the label is read
     * @param locale User locale for text formatting
     * @return String with the tag/tag to show
     */
    public static String get(String key, Object[] params, String origin, Locale locale) {
        if (null == params) {
            final String message = "No parameters were provided for the tag '" + key + "'.";
            System.err.println(message);
            log.error(message);
            return get(key, origin, locale);
        }
        try {
            String message = get(key, origin, locale);
            if (null == message) {
                message = "Tag '" + key + "' not found. Returning default value...";
                System.err.println(message);
                log.error(message);
                return getDefaultMessage(key);
            } else {
                return MessageFormat.format(message, params);
            }
        } catch (Exception e) {
            final String message = "Error searching for tag '" + key + "'. Returning default value...";
            System.err.println(message);
            log.error(message);
            return getDefaultMessage(key);
        }
    }

    private static Locale getLocale() {
        Locale innerLocale = forcedLocale;
        if (null == innerLocale) {
            return SystemInfo.getLocale();
            //TODO: We are using the default locale for the system. Is it worth using here the Locale selected by the
            // user?
        } else {
            return innerLocale;
        }
    }

    /**
     * Gets a message/tag according to the requested key
     *
     * @param key    String with the tag/label identifier to display
     * @param origin String with the name of the file or table where the label is read
     * @param locale User locale for text formatting
     * @return String with the tag/tag to show
     */
    public static String get(String key, String origin, Locale locale) {
        try {
            if (origin != null) {
                // We search by language and country
                key = buildNameAndLocale(origin, locale);
                Hashtable innerMap = labels.get(key);
                if ((null != innerMap) && (null != innerMap.get(key))) {
                    return (String) innerMap.get(key);
                }

                // If we do not, we search by language
                key = buildNameAndLocale(origin, locale.getLanguage());
                innerMap = labels.get(key);
                if ((null != innerMap) && (null != innerMap.get(key))) {
                    return (String) innerMap.get(key);
                }

                // If we do not, we look at the default
                key = buildNameAndLocale(origin, SystemInfo.getLocale());
                innerMap = labels.get(key);
                if ((null != innerMap) && (null != innerMap.get(key))) {
                    return (String) innerMap.get(key);
                }
            }

            // If we do not get it, we look at the complete map
            // Note: Tags with the same key in different files are overwritten
            String value = labels.get(DEFAULT_ORIGIN).get(key);
            if (null != value) {
                return value;
            }

            // If we do not, we return the default value
            return getDefaultMessage(key);

        } catch (Exception e) {
            final String message = "Error searching for tag '" + key + "'. Returning default value...";
            System.err.println(message);
            log.error(message);
            return getDefaultMessage(key);
        }
    }

    /**
     * It surrounds the key to search with a prefix and a suffix. The returned string is used when the searched
     * message was not fetched.
     *
     * @param key String with the tag/label identifier to display
     * @return String with the default tag/tag to show
     */
    private static String getDefaultMessage(String key) {
        return MessageHandler.PRE + key + MessageHandler.POST;
    }

    public static String buildNameAndLocale(String origin, Locale locale) {
        return buildNameAndLocale(origin, getLocaleId(locale));
    }

    public static String buildNameAndLocale(String origin, String locale) {
        int index = origin.indexOf(".");
        if (index == -1) {
            return origin + "_" + locale;
        } else {
            return origin.substring(0, index) + "_" + locale + origin.substring(index);
        }
    }

    /**
     * Convenience method to use to obtain the Locale identifier
     *
     * @param locale User locale to searching for
     * @return String with the name of the Locale
     */
    private static String getLocaleId(Locale locale) {
        locale = getDefaultLocaleIfNull(locale);
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    /**
     * Convenience method to use to obtain the Locale identifier. If null it returns the default one.
     *
     * @param locale User locale to searching for
     * @return The default locale
     */
    private static Locale getDefaultLocaleIfNull(Locale locale) {
        if (null == locale) {
            locale = getLocale();
        }
        return locale;
    }

    /**
     * Adds information to {@link SystemInfo} from the incoming file name.
     *
     * @param filename File name
     * @return Counting of aggregated tags
     */
    public int addDataFromFile(String filename) {
        log.debug("MessageHandler inicia lectura de archivo: " + filename);
        return addDataFromFile(filename, getLocale());
    }

    /**
     * Adds information to {@link SystemInfo} from the incoming file name.
     *
     * @param filename File name
     * @param locale   User locale to searching for
     * @return Counting of aggregated tags
     */
    public int addDataFromFile(String filename, Locale locale) {
        // NOTE: This does not work if we read different files with different locales
        forcedLocale = locale;

        int count = 0;
        locale = getDefaultLocaleIfNull(locale);
        String localeId = getLocaleId(locale);
        String bundleKey = buildNameAndLocale(filename, localeId);

        try {
            Properties bundle = propertiesHandlerUtils.loadProperties(filename, locale, bundleKey);
            count = propertyToMap(count, bundleKey, bundle);
        } catch (Exception e) {
            log.error("Error adding data from file '" + filename + "', locale '" + localeId + "'.  No data will be "
                            + "added from this file.",
                    e);
        }

        log.debug("Uploaded " + count + " tags from file '" + filename + "'.");
        return count;
    }

    private int propertyToMap(int count, String bundleKey, Properties bundle) {
        Enumeration keys = bundle.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            try {
                put(key, bundle.getProperty(key), bundleKey);
                count++;
            } catch (Exception e) {
                log.error("Error adding data for label '" + key + "'.  No data will be added for this label.", e);
            }
        }
        return count;
    }

    /**
     * Adds the value for the tag with the specified property name
     *
     * @param propertyName  String with tag name
     * @param propertyValue String with the value of the tag to add
     * @param origin        String with the name of the file or table where the tag is read, and the locale. It is
     *                      formed as <code>&lt;fileName&gt;_&lt;localeId&gt; o &lt;tableName&gt;_&lt;localeId&gt;
     *                      </code>
     */
    public void put(String propertyName, String propertyValue, String origin) {
        Hashtable<String, String> innerMap = labels.get(origin);
        if (null == innerMap) {
            origin = DEFAULT_ORIGIN;
            innerMap = new Hashtable<>();
        }
        labels.put(origin, innerMap);
        innerMap.put(propertyName, propertyValue);
    }

    /**
     * Metodo que limpia la data de SystemInfo
     * Solo debe ser usado para tratar de recargar la data, o para las pruebas.
     */
    public void clearData() {
        labels.clear();
    }

    /**
     * Retorna el Hastable de las etiquetas especificadas en 'bundleFile'
     *
     * @param bundleFile nombre del archivo a cargar
     * @return mapa de etiquetas
     */
    public Hashtable get(String bundleFile) {
        return getWithLocale(bundleFile, null);
    }

    /**
     * Retorna el Hastable de las etiquetas especificadas en 'bundleFile' and 'locale'
     *
     * @param bundleFile nombre del archivo a cargar
     * @param locale     Locale a usar
     * @return mapa de etiquetas o mensajes
     */
    public Hashtable getWithLocale(String bundleFile, Locale locale) {
        String key = buildNameAndLocale(bundleFile, locale == null ? Locale.getDefault() : locale);
        return labels.get(key);
    }

    public String getDefaultFile() {
        return defaultFile;
    }

    public void setDefaultFile(String bundleId) {
        defaultFile = bundleId;
    }

    /**
     * @return Hashtable con todas las etiquetas
     */
    public Hashtable getLabels() {
        return labels;
    }

    /**
     * Obtiene un mensaje/etiqueta de acuerdo al key solicitado, usando el
     * Locale por defecto del sistema.  Ignora el archivo de origen.
     * WARNING: Este metodo no diferencia el origen de los datos, ni multilenguaje.
     *
     * @param propertyName String con el identificador del mensaje/etiqueta a mostrar
     * @return String con el valor de la etiqueta propertyName a mostrar
     */
    public String getValue(String propertyName) {
        return get(propertyName, defaultFile);
    }

    /**
     * Obtiene un mensaje/etiqueta de acuerdo al key solicitado, usando el
     * Locale por defecto del sistema.
     *
     * @param propertyName String con el identificador del mensaje/etiqueta a mostrar
     * @param origin       String con el nombre del archivo o tabla de donde se lee la etiqueta
     * @return String con el valor de la etiqueta propertyName a mostrar
     */
    public static String get(String propertyName, String origin) {
        return get(propertyName, origin, getLocale());
    }
}
