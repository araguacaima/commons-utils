package com.araguacaima.commons.utils;

import com.araguacaima.commons.exception.core.PropertiesUtilException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Properties;

public class PropertiesHandler {
    private static final Logger log = LoggerFactory.getLogger(PropertiesHandler.class);
    private String absolutePropertiesFilePath;
    private ClassLoader classLoader;
    private String logFileSourceName = StringUtils.EMPTY;
    private Properties properties = new Properties();

    PropertiesHandler() {
        this.classLoader = PropertiesHandler.class.getClassLoader();
    }

    public PropertiesHandler(String logFileSourceName, ClassLoader classLoader)
            throws PropertiesUtilException {
        init(logFileSourceName, classLoader);
    }

    private void init(String logFileSourceName, ClassLoader classLoader)
            throws PropertiesUtilException {
        this.logFileSourceName = logFileSourceName;
        try {
            this.classLoader = classLoader;
            this.properties = this.loadConfig(logFileSourceName, classLoader);
        } catch (PropertiesUtilException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    /**
     * loadConfig metodo encargado de devolver un property determinado dado su nombre
     *
     * @param logFileSourceName String con el nombre del property a ser cargado
     * @param cl                ClassLoader objeto responsable por la carga de clases
     * @return Properties con la informacion solicitada
     * @throws PropertiesUtilException en caso de no conseguir el archivo o de ocurrir otro error
     */
    private Properties loadConfig(String logFileSourceName, ClassLoader cl)
            throws PropertiesUtilException {
        Properties properties = new Properties();
        URL resource;

        if (StringUtils.isBlank(logFileSourceName)) {
            throw new PropertiesUtilException(PropertiesUtilException.CONFIG_FILE_NAME_EMPTY);
        }
        try {
            log.info("Attempting to load properties for file: " + logFileSourceName);
            log.info("\tSearching thru classloader (1): " + cl.toString());
            InputStream inputstream = cl.getResourceAsStream(logFileSourceName);
            resource = cl.getResource(logFileSourceName);
            if (resource != null) {
                absolutePropertiesFilePath = resource.getPath();
            }
            if (inputstream == null) {
                log.info("\tSearching thru classloader (2): " + getClassLoader().toString());
                inputstream = getClassLoader().getResourceAsStream(logFileSourceName);
                resource = getClassLoader().getResource(logFileSourceName);
                if (resource != null) {
                    absolutePropertiesFilePath = resource.getPath();
                }
                if (inputstream == null) {
                    log.info("\tSearching thru classloader (3): " + getClassLoader().getParent().toString());
                    inputstream = getClassLoader().getParent().getResourceAsStream(logFileSourceName);
                    resource = getClassLoader().getParent().getResource(logFileSourceName);
                    if (resource != null) {
                        absolutePropertiesFilePath = resource.getPath();
                    }
                    if (inputstream == null) {
                        log.info("\tSearching thru classloader (4): " + ClassLoader.getSystemClassLoader().toString());
                        inputstream = ClassLoader.getSystemClassLoader().getResourceAsStream(logFileSourceName);
                        resource = ClassLoader.getSystemClassLoader().getResource(logFileSourceName);
                        if (resource != null) {
                            absolutePropertiesFilePath = resource.getPath();
                        }
                        if (inputstream == null) {
                            log.info("\tSearching directly from absolute path (5): " + logFileSourceName);
                            inputstream = new java.io.FileInputStream((new File(logFileSourceName)));
                            resource = (new File(logFileSourceName)).toURI().toURL();
                            absolutePropertiesFilePath = resource.getPath();
                        }
                    }
                }
            }
            log.info("\tFile: " + logFileSourceName + " found on classpath");
            properties.load(inputstream);
            inputstream.close();
        } catch (IOException ioe) {
            throw new PropertiesUtilException(PropertiesUtilException.IOEXCEPTION_ERROR + ioe);
        } catch (Exception e) {
            throw new PropertiesUtilException(PropertiesUtilException.CONFIG_FILE_ERROR + e);
        } catch (Throwable t) {
            throw new PropertiesUtilException(PropertiesUtilException.ERROR + t);
        }
        return properties;
    }

    private ClassLoader getClassLoader() {
        return classLoader;
    }

    private PropertiesHandler(String logFileSourceName, Class clazz)
            throws PropertiesUtilException {
        init(logFileSourceName, clazz.getClassLoader());
    }

    public PropertiesHandler(String logFileSourceName) {
        init(logFileSourceName);
    }

    private void init(String logFileSourceName) {
        this.logFileSourceName = logFileSourceName;
        try {
            this.properties = this.loadConfig(logFileSourceName);
        } catch (PropertiesUtilException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * loadConfig metodo encargado de devolver un property determinado dado su nombre
     *
     * @param logFileSourceName String con el nombre del property a ser cargado
     * @return Properties con la informacion solicitada
     * @throws PropertiesUtilException en caso de no conseguir el archivo o de ocurrir otro error
     */
    private Properties loadConfig(String logFileSourceName)
            throws PropertiesUtilException {
        Properties properties = new Properties();
        URL resource;
        try {
            log.info("Attempting to load properties for file: " + logFileSourceName);
            InputStream inputstream;
            logFileSourceName = URLDecoder.decode(logFileSourceName, "UTF-8");
            log.info("\tSearching directly from absolute path: " + logFileSourceName);
            inputstream = new java.io.FileInputStream((new File(logFileSourceName)));
            resource = (new File(logFileSourceName)).toURI().toURL();
            absolutePropertiesFilePath = resource.getPath();
            log.info("\tFile: " + logFileSourceName + " found on classpath");
            properties.load(inputstream);
            inputstream.close();
        } catch (IOException ioe) {
            log.info("\tFile: " + logFileSourceName + " NOT found on classpath");
            throw new PropertiesUtilException(PropertiesUtilException.IOEXCEPTION_ERROR + ioe);
        } catch (Exception e) {
            log.info("\tFile: " + logFileSourceName + " NOT found on classpath");
            throw new PropertiesUtilException(PropertiesUtilException.CONFIG_FILE_ERROR + e);
        } catch (Throwable t) {
            log.info("\tFile: " + logFileSourceName + " NOT found on classpath");
            throw new PropertiesUtilException(PropertiesUtilException.ERROR + t);
        }
        return properties;
    }

    private PropertiesHandler(File propertiesFile, Class clazz)
            throws PropertiesUtilException {
        classLoader = clazz.getClassLoader();
        logFileSourceName = propertiesFile.getName();
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
            throw new PropertiesUtilException(PropertiesUtilException.CONFIG_FILE_ERROR + e);
        }
    }

    public String getAbsolutePropertiesFilePath() {
        return absolutePropertiesFilePath;
    }

    public String getLogFileSourceName() {
        return logFileSourceName;
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * loadConfig metodo encargado de devolver un property determinado dado su nombre
     *
     * @param logFileSourceName String con el nombre del property a ser cargado
     * @param clazz             Clase contenida por el ClassLoader responsable por la carga de clases
     * @return Properties con la informacion solicitada
     * @throws PropertiesUtilException en caso de no conseguir el archivo o de ocurrir otro error
     */
    public Properties loadConfig(String logFileSourceName, Class clazz)
            throws PropertiesUtilException {
        return loadConfig(logFileSourceName, clazz.getClassLoader());
    }

}
