package com.araguacaima.commons.utils.file;

import com.araguacaima.commons.utils.NotNullsLinkedHashSet;
import org.apache.commons.collections4.Predicate;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

public interface FileUtilsFilenameFilter<T> extends FilenameFilter {

     int RESOURCE_FILTER_UNKNOWN = -1;

     int RESOURCE_FILE_FILTER_EQUALS = 0;
     int RESOURCE_FILE_FILTER_MATCHES = 1;
     int RESOURCE_FILE_FILTER_STARTS = 2;
     int RESOURCE_FILE_FILTER_ENDS = 3;
     int RESOURCE_FILE_FILTER_CONTAINS = 4;
     int RESOURCE_FILE_FILTER_NOT_EQUAL = 5;
     int RESOURCE_FILE_FILTER_NOT_MATCHES = 6;
     int RESOURCE_FILE_FILTER_NOT_STARTS = 7;
     int RESOURCE_FILE_FILTER_NOT_ENDS = 8;
     int RESOURCE_FILE_FILTER_NOT_CONTAINS = 9;

     int RESOURCE_DIR_FILTER_EQUALS = 100;
     int RESOURCE_DIR_FILTER_MATCHES = 101;
     int RESOURCE_DIR_FILTER_STARTS = 102;
     int RESOURCE_DIR_FILTER_ENDS = 103;
     int RESOURCE_DIR_FILTER_CONTAINS = 104;
     int RESOURCE_DIR_FILTER_NOT_EQUAL = 105;
     int RESOURCE_DIR_FILTER_NOT_MATCHES = 106;
     int RESOURCE_DIR_FILTER_NOT_STARTS = 107;
     int RESOURCE_DIR_FILTER_NOT_ENDS = 108;
     int RESOURCE_DIR_FILTER_NOT_CONTAINS = 109;

     int RESOURCE_DIR_OR_FILE_FILTER_EQUALS = 200;
     int RESOURCE_DIR_OR_FILE_FILTER_MATCHES = 201;
     int RESOURCE_DIR_OR_FILE_FILTER_STARTS = 202;
     int RESOURCE_DIR_OR_FILE_FILTER_ENDS = 203;
     int RESOURCE_DIR_OR_FILE_FILTER_CONTAINS = 204;
     int RESOURCE_DIR_OR_FILE_FILTER_NOT_EQUAL = 205;
     int RESOURCE_DIR_OR_FILE_FILTER_NOT_MATCHES = 206;
     int RESOURCE_DIR_OR_FILE_FILTER_NOT_STARTS = 207;
     int RESOURCE_DIR_OR_FILE_FILTER_NOT_ENDS = 208;
     int RESOURCE_DIR_OR_FILE_FILTER_NOT_CONTAINS = 209;

     int FILTER_TYPE_PACKAGE = 300;
     int FILTER_TYPE_IMPLEMETNS = 301;
     int FILTER_TYPE_EXTENDS = 302;
     int FILTER_TYPE_RESOURCE = 303;
     int FILTER_TYPE_JAR = 304;

    boolean accept(File dir, String name);

    boolean accept(File dir, String name, NotNullsLinkedHashSet<T> filters);

    File filter(File dir, String name, Predicate<T> predicate);

    /**
     *
     * @param classLoader The classLoader on witch find resources
     * @return A collection of URL with requeried resources
     * @throws IOException If any I/O Exception occurs
     * @deprecated use Collection getResources() throws IOException; instead
     */
    Collection<URL> getResources(ClassLoader classLoader) throws IOException;

    /**
     *
     * @param classLoader The classLoader on witch find resources
     * @return A collection of URL with requeried resources
     * @throws IOException If any I/O Exception occurs
     * @deprecated use Collection getResources() throws IOException; instead
     */
    Collection <String> getResourcePaths(ClassLoader classLoader) throws IOException;

    Collection <URL> getResources() throws IOException;

    String printCriterias();
}
