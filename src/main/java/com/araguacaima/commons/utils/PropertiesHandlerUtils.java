package com.araguacaima.commons.utils;

import com.araguacaima.commons.exception.MessageHandler;
import com.araguacaima.commons.exception.core.PropertiesUtilException;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

@Component
public class PropertiesHandlerUtils {

    private static final Map<String, PropertiesHandler> instancesMap = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(PropertiesHandler.class);
    private final FileUtils fileUtils;
    private final MapUtils mapUtils;
    private final NotNullOrEmptyStringObjectPredicate notNullOrEmptyStringObjectPredicate;

    @Autowired
    public PropertiesHandlerUtils(MapUtils mapUtils,
                                  FileUtils fileUtils,
                                  NotNullOrEmptyStringObjectPredicate notNullOrEmptyStringObjectPredicate) {
        this.mapUtils = mapUtils;
        this.fileUtils = fileUtils;
        this.notNullOrEmptyStringObjectPredicate = notNullOrEmptyStringObjectPredicate;
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




