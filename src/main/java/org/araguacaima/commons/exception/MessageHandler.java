package org.araguacaima.commons.exception;

import org.araguacaima.commons.utils.FileUtils;
import org.araguacaima.commons.utils.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

/**
 * Sobreescribe ResourceBundle para brindar algunas facilidades de uso.
 * Es similar en funcionalidad a SystemInfo para poder cargar etiquetas de BD.
 * <br>
 * Title: MessageHandler.java <br>
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */

@Component
public class MessageHandler {
    public static final String ERRORS = "errors";
    public static final String EXCEPTIONS = "exceptions";
    public static final String LABELS = "labels";
    // private  final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    public static final String POST = "]]";
    // Prefijo y sufijo a usar para los mensajes por defecto
    public static final String PRE = "[[";
    // Constantes para comodidad de uso, para identificar los bundles mas usados
    public static final String PROPERTIES = ".properties";
    private static final String DEFAULT_ORIGIN = "default";
    // private ResourceBundle labels; // Contenedor de las etiquetas leidas.
    private static final Hashtable<String, Hashtable<String, String>> labels = new Hashtable<>();
    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);
    private static Locale forcedLocale;
    private final FileUtils fileUtils;
    private String defaultFile = null;

    @Autowired
    public MessageHandler(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    } // Contenedor de las etiquetas leidas.

    /**
     * Obtiene un mensaje/etiqueta de acuerdo al key solicitado
     * ej: <code>getValue("A {0} B {1}", { "C", "D" }) -&gt; "A C B D"</code>
     * TODO: Recibir los params de otra forma? Parametros separados?
     *
     * @param key    String con el identificador del mensaje/etiqueta a mostrar
     * @param params Object[] con los parametros a substituir
     * @param origin String con el nombre del archivo o tabla de donde se lee la etiqueta
     * @return String con el mensaje/etiqueta a mostrar
     */
    public static String get(String key, Object[] params, String origin) {
        return get(key, params, origin, getLocale());
    }

    /**
     * Obtiene un mensaje/etiqueta de acuerdo al key solicitado
     * ej: <code>getValue("A {0} B {1}", { "C", "D" }) -&gt; "A C B D"</code>
     * TODO: Recibir los params de otra forma? Parametros separados?
     *
     * @param key    String con el identificador del mensaje/etiqueta a mostrar
     * @param params Object[] con los parametros a substituir
     * @param origin String con el nombre del archivo o tabla de donde se lee la etiqueta
     * @param locale Locale a usar
     * @return String con el mensaje/etiqueta a mostrar
     */
    public static String get(String key, Object[] params, String origin, Locale locale) {
        if (null == params) {
            final String message = "No se suministraron parametros para la etiqueta '" + key + "'.";
            System.err.println(message);
            log.error(message);
            return get(key, origin, locale);
        }
        try {
            String message = get(key, origin, locale);
            if (null == message) {
                message = "Etiqueta '" + key + "' no encontrada.  Devolviendo valor por defecto...";
                System.err.println(message);
                log.error(message);
                return getDefaultMessage(key); // TODO: Mostrar los params?
            } else {
                return MessageFormat.format(message, params);
            }
        } catch (Exception e) {
            final String message = "Error buscando etiqueta '" + key + "'.  Devolviendo valor por defecto...";
            System.err.println(message);
            log.error(message);
            return getDefaultMessage(key); // TODO: Mostrar los params?
        }
    }

    private static Locale getLocale() {
        Locale innerLocale = forcedLocale;
        if (null == innerLocale) {
            return SystemInfo.getLocale();
            /*
              TODO: Estamos usando el Locale default para el sistema.
              Vale la pena usar aqui el Locale seleccionado por el usuario?
              locale = UserInfo.getLocale();
             */
        } else {
            return innerLocale;
        }
    }

    /**
     * Obtiene el valor de la etiqueta propertyName
     *
     * @param propertyName String con el identificador del mensaje/etiqueta a mostrar
     * @param origin       String con el nombre del archivo o tabla de donde se lee la etiqueta
     * @param locale       Locale a usar
     * @return String con el valor de la etiqueta propertyName a mostrar
     */
    public static String get(String propertyName, String origin, Locale locale) {
        try {
            if (origin != null) {
                // Buscamos por idioma y pais
                // labels_es_ve.properties
                String key = buildNameAndLocale(origin, locale);
                Hashtable innerMap = labels.get(key);
                if ((null != innerMap) && (null != innerMap.get(propertyName))) {
                    return (String) innerMap.get(propertyName);
                }

                // Si no lo conseguimos, buscamos por idioma
                // labels_es.properties
                key = buildNameAndLocale(origin, locale.getLanguage());
                innerMap = labels.get(key);
                if ((null != innerMap) && (null != innerMap.get(propertyName))) {
                    return (String) innerMap.get(propertyName);
                }

                // Si no lo conseguimos, buscamos en el por defecto
                // labels.properties ?
                key = buildNameAndLocale(origin, SystemInfo.getLocale());
                innerMap = labels.get(key);
                if ((null != innerMap) && (null != innerMap.get(propertyName))) {
                    return (String) innerMap.get(propertyName);
                }
            }

            // Si no lo conseguimos, buscamos en el mapa completo
            // Nota: las etiquetas con la misma clave en distintos archivos se sobreescriben
            String value = labels.get(DEFAULT_ORIGIN).get(propertyName);
            if (null != value) {
                return value;
            }

            // Si no lo conseguimos, devolvemos el valor por defecto
            return getDefaultMessage(propertyName);

        } catch (Exception e) {
            final String message = "Error buscando etiqueta '" + propertyName + "'.  Devolviendo valor por defecto...";
            System.err.println(message);
            log.error(message);
            return getDefaultMessage(propertyName);
        }
    }

    /**
     * Rodea el key a buscar con un prefijo y un sufijo.
     * El String retornado se usa cuando no se consiguio el mensaje buscado.
     *
     * @param key String con el identificador del mensaje/etiqueta a mostrar
     * @return String con el mensaje/etiqueta a mostrar por defecto
     */
    private static String getDefaultMessage(String key) {
        return MessageHandler.PRE + key + MessageHandler.POST;
    }

    private static String buildNameAndLocale(String origin, Locale locale) {
        return buildNameAndLocale(origin, getLocaleId(locale));
    }

    private static String buildNameAndLocale(String origin, String locale) {
        int index = origin.indexOf(".");
        if (index == -1) {
            return origin + "_" + locale;
        } else {
            return origin.substring(0, index) + "_" + locale + origin.substring(index);
        }
    }

    /**
     * Metodo de conveniencia a usar para obtener el id del Locale
     *
     * @param locale Locale a usar
     * @return String con el nombre del Locale
     */
    private static String getLocaleId(Locale locale) {
        // return getDefaultLocaleIfNull(locale).getDisplayName();
        locale = getDefaultLocaleIfNull(locale);
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    /**
     * Metodo de conveniencia a usar para obtener el id del Locale
     *
     * @param locale Locale a usar
     * @return Locale
     */
    private static Locale getDefaultLocaleIfNull(Locale locale) {
        if (null == locale) {
            locale = getLocale();
        }
        return locale;
    }

    /**
     * Agrega informacion al SystemInfo desde el archivo indicado por fileName.
     *
     * @param bundleId String con el nombre del archivo
     * @return int con el conteo de las etiquetas agregadas
     */
    public int addDataFromFile(String bundleId) {
        log.debug("_I MessageHandler inicia lectura de archivo: " + bundleId);
        return addDataFromFile(bundleId, getLocale());
    }

    /**
     * Agrega informacion al SystemInfo desde el archivo indicado por fileName.
     *
     * @param bundleId String con el nombre del archivo
     * @param locale   Locale a usar
     * @return int con el conteo de las etiquetas agregadas
     */
    public int addDataFromFile(String bundleId, Locale locale) {
        // NOTA: Esto no funciona si leemos distintos archivos con distintos locales
        forcedLocale = locale;

        int count = 0;
        locale = getDefaultLocaleIfNull(locale);
        String localeId = getLocaleId(locale);
        String bundleKey = buildNameAndLocale(bundleId, localeId);

        try {
            Properties bundle = loadProperties(bundleId, locale, bundleKey);
            count = propertyToMap(count, bundleKey, bundle);
        } catch (Exception e) {
            log.error("Error adding data from file '" + bundleId + "', locale '" + localeId + "'.  No data will be "
                            + "added from this file.",
                    e);
            // Se podria hacer return -1 aqui si queremos un codigo de error.
        }

        log.debug("171* Se cargaron " + count + " etiquetas del archivo '" + bundleId + "'.");
        return count;
    }

    // TODO: Pasar a PropertyUtil o FileUtil algo asi
    public Properties loadProperties(String bundleId, Locale locale, String bundleKey) {
        // ResourceBundle bundle = ResourceBundle.getBundle(bundleId, locale);
        Properties bundle = new Properties();

        // Carga 1
        loadProperties(bundleId, bundle);

        // Carga 2
        String fileLanguage = buildNameAndLocale(bundleId, locale.getLanguage());
        loadProperties(fileLanguage, bundle);

        // Carga 3
        if (null != bundleKey) {
            loadProperties(bundleKey, bundle);
        }

        // bundle.putAll(bundle);
        return bundle;
    }

    // TODO: Pasar a PropertyUtil o algo asi
    private int propertyToMap(int count, String bundleKey, Properties bundle) {
        // Enumeration keys = bundle.getKeys();
        Enumeration keys = bundle.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            try {
                // log.debug(key + " = " + bundle.getString(key));
                // log.debug(key + " = " + bundle.getProperty(key));
                // put(key, bundle.getString(key), bundleKey);
                put(key, bundle.getProperty(key), bundleKey);
                count++;
            } catch (Exception e) {
                log.error("Error adding data for label '" + key + "'.  No data will be added for this label.", e);
            }
        }
        return count;
    }

    private void loadProperties(String bundleId, Properties bundle) {
        FileInputStream fis = null;
        try {
            log.debug("Leyendo '" + bundleId + "'...");
            String path = fileUtils.findFilePath(bundleId);
            fis = new FileInputStream(path);
            bundle.load(fis);
        } catch (FileNotFoundException e) {
            try {
                fileUtils.loadBundleAsResource(bundleId, bundle);
            } catch (Exception e1) {
                log.error("Error leyendo archivo '" + bundleId + "'");
                e1.printStackTrace();
            }
        } catch (Exception e) {
            log.error("Error leyendo '" + bundleId + "'...");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Agrega el valor propertyValue para la etiqueta propertyName
     *
     * @param propertyName  String con el nombre de la etiqueta
     * @param propertyValue String con el valor de la etiqueta a agregar
     * @param origin        String con el nombre del archivo o tabla de donde
     *                      se lee la etiqueta, y el locale.  Esta armado de
     *                      forma <code>&lt;fileName&gt;_&lt;localeId&gt; o &lt;tableName&gt;_&lt;localeId&gt;</code>
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
        // return (String) labels.get(propertyName);
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
