package com.araguacaima.commons.utils.file;

import com.araguacaima.commons.utils.NotNullsLinkedHashSet;
import com.araguacaima.commons.utils.StringUtils;
import org.apache.commons.collections4.IterableUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class FileUtilsFilenameFilterJar extends FileUtilsFilenameFilterImpl {

    public static final int DEFAULT_FILTER_TYPE = FileUtilsFilenameFilter.RESOURCE_DIR_OR_FILE_FILTER_CONTAINS;
    private String jarCriteria;
    private NotNullsLinkedHashSet<String> jarCriteriaCollection = new NotNullsLinkedHashSet<>();

    public FileUtilsFilenameFilterJar() {
        super();
        this.filterType = DEFAULT_FILTER_TYPE;
    }

    public FileUtilsFilenameFilterJar(String jarCriteria) {
        this.jarCriteria = jarCriteria;
        this.filterType = DEFAULT_FILTER_TYPE;
    }

    public FileUtilsFilenameFilterJar(String jarCriteria, int filterType) {
        this.jarCriteria = jarCriteria;
        this.filterType = filterType;
    }

    public FileUtilsFilenameFilterJar(NotNullsLinkedHashSet<String> jarCriteriaCollection) {
        this.jarCriteriaCollection = jarCriteriaCollection;
        this.filterType = DEFAULT_FILTER_TYPE;
    }

    public FileUtilsFilenameFilterJar(NotNullsLinkedHashSet<String> jarCriteriaCollection, int filterType) {
        this.jarCriteriaCollection = jarCriteriaCollection;
        this.filterType = filterType;
    }

    public boolean accept(File dir, String name) {

        jarCriteriaCollection.add(jarCriteria);

        if (dir == null || jarCriteriaCollection.isEmpty()) {
            return true;
        }

        boolean result = true;
        for (String jarCriteria : jarCriteriaCollection) {
            if (!result) {
                break;
            }
            String dirTransformed = dir.getPath().replaceAll(StringUtils.DOUBLEBACKSLASH, StringUtils.SLASH);
            switch (filterType) {
                case RESOURCE_FILE_FILTER_EQUALS:
                    result = name.equals(jarCriteria);
                    break;
                case RESOURCE_FILE_FILTER_MATCHES:
                    result = name.matches(jarCriteria);
                    break;
                case RESOURCE_FILE_FILTER_STARTS:
                    result = name.startsWith(jarCriteria);
                    break;
                case RESOURCE_FILE_FILTER_ENDS:
                    result = name.endsWith(jarCriteria);
                    break;
                case RESOURCE_FILE_FILTER_CONTAINS:
                    result = name.contains(jarCriteria);
                    break;
                case RESOURCE_FILE_FILTER_NOT_EQUAL:
                    result = !name.equals(jarCriteria);
                    break;
                case RESOURCE_FILE_FILTER_NOT_MATCHES:
                    result = !name.matches(jarCriteria);
                    break;
                case RESOURCE_FILE_FILTER_NOT_STARTS:
                    result = !name.startsWith(jarCriteria);
                    break;
                case RESOURCE_FILE_FILTER_NOT_ENDS:
                    result = !name.endsWith(jarCriteria);
                    break;
                case RESOURCE_FILE_FILTER_NOT_CONTAINS:
                    result = !name.contains(jarCriteria);
                    break;
                case RESOURCE_DIR_FILTER_EQUALS:
                    result = dirTransformed.equals(jarCriteria);
                    break;
                case RESOURCE_DIR_FILTER_MATCHES:
                    result = dirTransformed.matches(jarCriteria);
                    break;
                case RESOURCE_DIR_FILTER_STARTS:
                    result = dirTransformed.startsWith(jarCriteria);
                    break;
                case RESOURCE_DIR_FILTER_ENDS:
                    result = dirTransformed.endsWith(jarCriteria);
                    break;
                case RESOURCE_DIR_FILTER_CONTAINS:
                    result = dirTransformed.contains(jarCriteria);
                    break;
                case RESOURCE_DIR_FILTER_NOT_EQUAL:
                    result = !dirTransformed.equals(jarCriteria);
                    break;
                case RESOURCE_DIR_FILTER_NOT_MATCHES:
                    result = !dirTransformed.matches(jarCriteria);
                    break;
                case RESOURCE_DIR_FILTER_NOT_STARTS:
                    result = !dirTransformed.startsWith(jarCriteria);
                    break;
                case RESOURCE_DIR_FILTER_NOT_ENDS:
                    result = !dirTransformed.endsWith(jarCriteria);
                    break;
                case RESOURCE_DIR_FILTER_NOT_CONTAINS:
                    result = !dirTransformed.contains(jarCriteria);
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_EQUALS:
                    result = (name.equals(jarCriteria) || dirTransformed.equals(jarCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_MATCHES:
                    result = (name.matches(jarCriteria) || dirTransformed.matches(jarCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_STARTS:
                    result = (name.startsWith(jarCriteria) || dirTransformed.startsWith(jarCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_ENDS:
                    result = (name.endsWith(jarCriteria) || dirTransformed.endsWith(jarCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_CONTAINS:
                    result = (name.contains(jarCriteria) || dirTransformed.contains(jarCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_EQUAL:
                    result = (!name.equals(jarCriteria) || !dirTransformed.equals(jarCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_MATCHES:
                    result = (!name.matches(jarCriteria) || !dirTransformed.matches(jarCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_STARTS:
                    result = (!name.startsWith(jarCriteria) || !dirTransformed.startsWith(jarCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_ENDS:
                    result = (!name.endsWith(jarCriteria) || !dirTransformed.endsWith(jarCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_CONTAINS:
                    result = (!name.contains(jarCriteria) || !dirTransformed.contains(jarCriteria));
                    break;

                default:
                    result = false;
                    break;
            }
        }
        return result;
    }

    public int getFilterType() {
        return filterType;
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    public String getJarCriteria() {
        return jarCriteria;
    }

    @Override
    public Collection<String> getResourcePaths(final ClassLoader classLoader)
            throws IOException {
        return transformURLIntoStringPaths(getResources(classLoader));
    }

    @Override
    public Collection<URL> getResources(final ClassLoader classLoader) {

        final Collection<URL> result = new ArrayList<>();

        jarCriteriaCollection.add(jarCriteria);
        IterableUtils.forEach(jarCriteriaCollection, jarCriteria -> {
            fileUtils.addFilter(new FileUtilsFilenameFilterJar(jarCriteria, filterType));
            final String jarCriteriaTransformed = stringUtils.replaceLast(jarCriteria, "/\\*", StringUtils.EMPTY);
            try {
                Collection<URL> urlPaths = Collections.list(classLoader.getResources(jarCriteriaTransformed));
                urlPaths.addAll(Collections.list(classLoader.getResources("/" + jarCriteriaTransformed)));
            } catch (Exception ignored) {
            }
            final File file = new File(jarCriteriaTransformed);
            result.addAll(transformJarIntoURLs(file));
            result.addAll(transformFilesIntoURLs(file.listFiles()));
        });

        return result;
    }

    public Collection<URL> getResources() {

        final Collection<URL> result = new ArrayList<>();

        jarCriteriaCollection.add(jarCriteria);
        IterableUtils.forEach(jarCriteriaCollection, jarCriteria -> {
            if (!new File(jarCriteria).exists()) {
                jarCriteria = classLoaderUtils.getPathForResource(jarCriteria);
            }
            fileUtils.addFilter(new FileUtilsFilenameFilterJar(jarCriteria, filterType));
            final String jarCriteriaTransformed = stringUtils.replaceLast(jarCriteria, "/\\*", StringUtils.EMPTY);
            final File file = new File(jarCriteriaTransformed);
            result.addAll(transformJarIntoURLs(file));
            result.addAll(transformFilesIntoURLs(file.listFiles()));
        });
        return result;
    }

    public String printCriterias() {
        return "[" + FileUtilsFilenameFilterJar.class.getName() + "]" + "jarCriteria: " + jarCriteria + " | " +
                "jarCriteriaCollection: " + jarCriteriaCollection + " | filterType: " + filterType;
    }

}