package com.araguacaima.commons.utils.file;

import com.araguacaima.commons.utils.FileUtils;
import com.araguacaima.commons.utils.NotNullsLinkedHashSet;
import com.araguacaima.commons.utils.StringUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class FileUtilsFilenameFilterPackage extends FileUtilsFilenameFilterImpl {

    public static final int DEFAULT_FILTER_TYPE = FileUtilsFilenameFilter.RESOURCE_DIR_OR_FILE_FILTER_CONTAINS;
    private String packageCriteria;
    private NotNullsLinkedHashSet<String> packageCriteriaCollection = new NotNullsLinkedHashSet<>();

    public FileUtilsFilenameFilterPackage() {
        super();
        this.filterType = DEFAULT_FILTER_TYPE;
    }

    public FileUtilsFilenameFilterPackage(String packageCriteria) {
        this.packageCriteria = packageCriteria.replaceAll("\\.",
                StringUtils.SLASH).replaceAll(StringUtils.DOUBLEBACKSLASH, StringUtils.SLASH);
        this.filterType = DEFAULT_FILTER_TYPE;
    }

    public FileUtilsFilenameFilterPackage(String packageCriteria, int filterType) {
        this.packageCriteria = packageCriteria.replaceAll("\\.",
                StringUtils.SLASH).replaceAll(StringUtils.DOUBLEBACKSLASH, StringUtils.SLASH);
        this.filterType = filterType;
    }

    public FileUtilsFilenameFilterPackage(NotNullsLinkedHashSet<String> packageCriteriaCollection) {
        this.packageCriteriaCollection = packageCriteriaCollection;
        this.filterType = DEFAULT_FILTER_TYPE;
    }

    public FileUtilsFilenameFilterPackage(NotNullsLinkedHashSet<String> packageCriteriaCollection, int filterType) {
        CollectionUtils.transform(packageCriteriaCollection,
                packageCriteria -> packageCriteria.replaceAll("\\.",
                        StringUtils.SLASH).replaceAll(StringUtils.DOUBLEBACKSLASH, StringUtils.SLASH));
        this.packageCriteriaCollection = packageCriteriaCollection;
        this.filterType = filterType;
    }

    public boolean accept(File dir, String name) {

        packageCriteriaCollection.add(packageCriteria);

        if (dir == null || packageCriteriaCollection.isEmpty()) {
            return true;
        }

        boolean result = true;
        for (String aPackageCriteriaCollection : packageCriteriaCollection) {
            String packageCriteria = (aPackageCriteriaCollection).replaceAll("\\*", StringUtils.EMPTY);
            if (packageCriteria.lastIndexOf(StringUtils.SLASH) == packageCriteria.length() - 1) {
                packageCriteria = packageCriteria.substring(0, packageCriteria.length() - 1);
            }
            if (!result) {
                break;
            }
            String dirTransformed = dir.getPath().replaceAll(StringUtils.DOUBLEBACKSLASH, StringUtils.SLASH);

            switch (filterType) {
                case RESOURCE_FILE_FILTER_EQUALS:
                    result = name.equals(packageCriteria);
                    break;
                case RESOURCE_FILE_FILTER_MATCHES:
                    result = name.matches(packageCriteria);
                    break;
                case RESOURCE_FILE_FILTER_STARTS:
                    result = name.startsWith(packageCriteria);
                    break;
                case RESOURCE_FILE_FILTER_ENDS:
                    result = name.endsWith(packageCriteria);
                    break;
                case RESOURCE_FILE_FILTER_CONTAINS:
                    result = name.contains(packageCriteria);
                    break;
                case RESOURCE_FILE_FILTER_NOT_EQUAL:
                    result = !name.equals(packageCriteria);
                    break;
                case RESOURCE_FILE_FILTER_NOT_MATCHES:
                    result = !name.matches(packageCriteria);
                    break;
                case RESOURCE_FILE_FILTER_NOT_STARTS:
                    result = !name.startsWith(packageCriteria);
                    break;
                case RESOURCE_FILE_FILTER_NOT_ENDS:
                    result = !name.endsWith(packageCriteria);
                    break;
                case RESOURCE_FILE_FILTER_NOT_CONTAINS:
                    result = !name.contains(packageCriteria);
                    break;
                case RESOURCE_DIR_FILTER_EQUALS:
                    result = dirTransformed.equals(packageCriteria);
                    break;
                case RESOURCE_DIR_FILTER_MATCHES:
                    result = dirTransformed.matches(packageCriteria);
                    break;
                case RESOURCE_DIR_FILTER_STARTS:
                    result = dirTransformed.startsWith(packageCriteria);
                    break;
                case RESOURCE_DIR_FILTER_ENDS:
                    result = dirTransformed.endsWith(packageCriteria);
                    break;
                case RESOURCE_DIR_FILTER_CONTAINS:
                    result = dirTransformed.contains(packageCriteria);
                    break;
                case RESOURCE_DIR_FILTER_NOT_EQUAL:
                    result = !dirTransformed.equals(packageCriteria);
                    break;
                case RESOURCE_DIR_FILTER_NOT_MATCHES:
                    result = !dirTransformed.matches(packageCriteria);
                    break;
                case RESOURCE_DIR_FILTER_NOT_STARTS:
                    result = !dirTransformed.startsWith(packageCriteria);
                    break;
                case RESOURCE_DIR_FILTER_NOT_ENDS:
                    result = !dirTransformed.endsWith(packageCriteria);
                    break;
                case RESOURCE_DIR_FILTER_NOT_CONTAINS:
                    result = !dirTransformed.contains(packageCriteria);
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_EQUALS:
                    result = (name.equals(packageCriteria) || dirTransformed.equals(packageCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_MATCHES:
                    result = (name.matches(packageCriteria) || dirTransformed.matches(packageCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_STARTS:
                    result = (name.startsWith(packageCriteria) || dirTransformed.startsWith(packageCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_ENDS:
                    result = (name.endsWith(packageCriteria) || dirTransformed.endsWith(packageCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_CONTAINS:
                    result = (name.contains(packageCriteria) || dirTransformed.contains(packageCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_EQUAL:
                    result = (!name.equals(packageCriteria) || !dirTransformed.equals(packageCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_MATCHES:
                    result = !name.matches(packageCriteria) || !dirTransformed.matches(packageCriteria);
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_STARTS:
                    result = (!name.startsWith(packageCriteria) || !dirTransformed.startsWith(packageCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_ENDS:
                    result = (!name.endsWith(packageCriteria) || !dirTransformed.endsWith(packageCriteria));
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_CONTAINS:
                    result = (!name.contains(packageCriteria) || !dirTransformed.contains(packageCriteria));
                    break;
                default:
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

    public String getPackageCriteria() {
        return packageCriteria;
    }

    public NotNullsLinkedHashSet<String> getPackageCriteriaCollection() {
        return packageCriteriaCollection;
    }

    @Override
    public Collection<String> getResourcePaths(final ClassLoader classLoader)
            throws IOException {
        return transformURLIntoStringPaths(getResources(classLoader));
    }

    @Override
    public Collection<URL> getResources(final ClassLoader classLoader)
            throws IOException {
        final Collection<URL> result = new ArrayList<>();
        packageCriteriaCollection.add(packageCriteria);
        IterableUtils.forEach(packageCriteriaCollection, packageCriteria -> {
            fileUtils.addFilter(new FileUtilsFilenameFilterPackage(packageCriteria, filterType));
            String packageCriteriaTransformed = stringUtils.replaceLast(packageCriteria, "/\\*", StringUtils.EMPTY);
            Collection<URL> resourcesPath;
            try {
                resourcesPath = Collections.list(classLoader.getResources(packageCriteriaTransformed));
                IterableUtils.forEach(resourcesPath, url -> {
                    File transformedClassPath = null;
                    try {
                        if (url != null) {
                            transformedClassPath = new File((URL.class).isInstance(url) ? url.getFile() : url
                                    .toString());
                            String fileNameDecoded = URLDecoder.decode(transformedClassPath.getPath(), "UTF-8");
                            transformedClassPath = new File(fileNameDecoded);
                        }
                    } catch (IOException ignored) {
                    }
                    final NotNullsLinkedHashSet<File> files = fileUtils.listFiles(transformedClassPath,
                            FileUtils.DEFAULT_SEARCH_TYPE);
                    result.addAll(transformFilesIntoURLs(files));
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return result;
    }

    public Collection<URL> getResources() {
        final Collection<URL> result = new ArrayList<>();

        packageCriteriaCollection.add(packageCriteria);

        IterableUtils.forEach(packageCriteriaCollection, packageCriteria -> {
            try {
                classLoaderUtils.init("/");

                fileUtils.addFilter(new FileUtilsFilenameFilterPackage(packageCriteria, filterType));
                String packageCriteriaTransformed = stringUtils.replaceLast(packageCriteria, "/\\*", StringUtils.EMPTY);
                Collection<URL> resourcesPath = classLoaderUtils.getResources(packageCriteriaTransformed);
                IterableUtils.forEach(resourcesPath, url -> {
                    File transformedClassPath;
                    try {
                        transformedClassPath = new File((URL.class).isInstance(url) ? url.getFile() : url.toString());
                        String fileNameDecoded = URLDecoder.decode(transformedClassPath.getPath(), "UTF-8");
                        transformedClassPath = new File(fileNameDecoded);
                        final NotNullsLinkedHashSet<File> files = fileUtils.listFiles(transformedClassPath,
                                FileUtils.DEFAULT_SEARCH_TYPE);
                        result.addAll(transformFilesIntoURLs(files));
                    } catch (IOException ignored) {

                    }
                });
            } catch (MalformedURLException ignored) {

            }
        });
        return result;
    }

    public String printCriterias() {
        return "[" + FileUtilsFilenameFilterPackage.class.getName() + "]" + "packageCriteria: " + packageCriteria +
                "" + " | packageCriteriaCollection: " + packageCriteriaCollection + " | filterType: " + filterType;
    }
}
