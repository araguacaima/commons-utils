package com.araguacaima.commons.utils;

import com.araguacaima.commons.exception.MessageHandler;
import com.araguacaima.commons.exception.core.PropertiesUtilException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;


public class PropertiesHandlerUtils {

    private static final Map<String, PropertiesHandler> instancesMap = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(PropertiesHandler.class);
    private final FileUtils fileUtils = new FileUtils();
    private final MapUtils mapUtils = MapUtils.getInstance();
    private final NotNullOrEmptyStringObjectPredicate notNullOrEmptyStringObjectPredicate = new NotNullOrEmptyStringObjectPredicate();

    private static final PropertiesHandlerUtils INSTANCE = new PropertiesHandlerUtils();

    private PropertiesHandlerUtils() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static PropertiesHandlerUtils getInstance() {
        return INSTANCE;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cannot clone instance of this class");
    }

    public PropertiesHandler getHandler(String logFileSourceName) {
        return getHandler(logFileSourceName, false);
    }

    public PropertiesHandler getHandler(String logFileSourceName, boolean forceRenew) {
        PropertiesHandler instance;
        if (forceRenew || instancesMap.get(logFileSourceName) == null) {
            instancesMap.remove(logFileSourceName);
            instance = new PropertiesHandler(logFileSourceName);
            if (instance.getProperties() != null && instance.getProperties().size() > 0) {
                instancesMap.put(logFileSourceName, instance);
            } else {
                instance = new PropertiesHandler();
            }
        } else {
            instance = instancesMap.get(logFileSourceName);
        }
        return instance;
    }

    public PropertiesHandler getHandler(File propertiesFile, ClassLoader classLoader)
            throws PropertiesUtilException {
        return getHandler(propertiesFile, classLoader, false);
    }

    private PropertiesHandler getHandler(File propertiesFile, ClassLoader classLoader, boolean forceRenew)
            throws PropertiesUtilException {
        PropertiesHandler instance;
        String logFileSourceName = propertiesFile.getPath();
        if (forceRenew || instancesMap.get(logFileSourceName) == null) {
            instancesMap.remove(logFileSourceName);
            instance = new PropertiesHandler(logFileSourceName, classLoader);
            if (instance.getProperties() != null && instance.getProperties().size() > 0) {
                instancesMap.put(logFileSourceName, instance);
            } else {
                instance = new PropertiesHandler();
            }
        } else {
            instance = instancesMap.get(logFileSourceName);
        }
        return instance;
    }

    public Map<String, String> loadConfig(String logFileSourceName,
                                          Class clazz,
                                          String propertyName,
                                          String tokenSeparator,
                                          String valueSeparator)
            throws PropertiesUtilException {
        Map<String, String> result = new Hashtable<>();
        Collection properties = loadConfig(logFileSourceName, clazz, propertyName, tokenSeparator);
        String key;
        String value;
        String property;
        String[] keyValue;
        for (Object property1 : properties) {
            property = (String) property1;
            key = null;
            value = StringUtils.EMPTY;
            if (property.contains(valueSeparator)) {
                keyValue = property.split(valueSeparator);
                try {
                    key = keyValue[0];
                } catch (Exception ignored) {

                }
                try {
                    value = keyValue[1];
                } catch (Exception ignored) {

                }
            }
            if (StringUtils.isNotBlank(key)) {
                result.put(key.trim(), value.trim());
            }
        }
        return result;
    }

    private Collection loadConfig(String logFileSourceName,
                                  Class clazz,
                                  final String propertyName,
                                  final String tokenSeparator)
            throws PropertiesUtilException {

        final Properties properties = getHandler(logFileSourceName, clazz.getClassLoader()).getProperties();
        final Object[] propertyValues = new Object[1];
        Map propertiesStartedWith = mapUtils.find(properties,
                o -> ((String) o).equalsIgnoreCase(propertyName),
                notNullOrEmptyStringObjectPredicate,
                MapUtils.EVALUATE_BOTH_KEY_AND_VALUE);

        IterableUtils.forEach(propertiesStartedWith.values(),
                o -> propertyValues[0] = ((String) o).split(tokenSeparator));
        Collection result;
        try {
            result = Arrays.asList((Object[]) propertyValues[0]);
        } catch (Exception e) {
            result = new ArrayList();
        }
        CollectionUtils.transform(result, o -> ((String) o).trim());
        return result;
    }

    public PropertiesHandler getHandler(String logFileSourceName, ClassLoader classLoader)
            throws PropertiesUtilException {
        return getHandler(logFileSourceName, classLoader, false);
    }

    public PropertiesHandler getHandler(String logFileSourceName, ClassLoader classLoader, boolean forceRenew)
            throws PropertiesUtilException {
        PropertiesHandler instance;
        if (forceRenew || instancesMap.get(logFileSourceName) == null) {
            instancesMap.remove(logFileSourceName);
            instance = new PropertiesHandler(logFileSourceName, classLoader);
            if (instance.getProperties() != null && instance.getProperties().size() > 0) {
                instancesMap.put(logFileSourceName, instance);
            } else {
                instance = new PropertiesHandler();
            }
        } else {
            instance = instancesMap.get(logFileSourceName);
        }
        return instance;
    }

    public Collection loadConfig(String logFileSourceName, Class clazz, String propertyName)
            throws PropertiesUtilException {
        return loadConfig(logFileSourceName, clazz, propertyName, StringUtils.COMMA_SYMBOL);
    }

    public Properties loadProperties(String bundleId, Locale locale, String bundleKey) {
        Properties bundle = new Properties();
        loadProperties(bundleId, bundle);
        String fileLanguage = MessageHandler.buildNameAndLocale(bundleId, locale.getLanguage());
        loadProperties(fileLanguage, bundle);
        if (null != bundleKey) {
            loadProperties(bundleKey, bundle);
        }
        return bundle;
    }

    private void loadProperties(String bundleId, Properties bundle) {
        FileInputStream fis = null;
        try {
            log.debug("Reading '" + bundleId + "'...");
            String path = fileUtils.findFilePath(bundleId);
            fis = new FileInputStream(path);
            bundle.load(fis);
        } catch (FileNotFoundException e) {
            try {
                fileUtils.loadBundleAsResource(bundleId, bundle);
            } catch (Exception e1) {
                log.error("Error reading file '" + bundleId + "'");
                e1.printStackTrace();
            }
        } catch (Exception e) {
            log.error("Error reading '" + bundleId + "'...");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

}




