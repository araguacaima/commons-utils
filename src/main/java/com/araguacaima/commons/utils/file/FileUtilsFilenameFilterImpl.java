package com.araguacaima.commons.utils.file;

import com.araguacaima.commons.utils.ClassLoaderUtils;
import com.araguacaima.commons.utils.FileUtils;
import com.araguacaima.commons.utils.NotNullsLinkedHashSet;
import com.araguacaima.commons.utils.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public abstract class FileUtilsFilenameFilterImpl implements FileUtilsFilenameFilter<File> {

    protected static Logger log = LoggerFactory.getLogger(FileUtilsFilenameFilter.class);
    protected final FileUtils fileUtils = new FileUtils();
    protected ClassLoaderUtils classLoaderUtils;
    protected int filterType;
    protected StringUtils stringUtils;

    //TODO Implementar el filterType por cada implementación de los filtros y no únicamente por la búsqueda completa,
    //TODO es decir, que dentro de cada criterio de cada filtro sea posible especificar si aplica un "o" o un "y",
    // por ejemplo
    public FileUtilsFilenameFilterImpl() {

    }

    public boolean accept(File dir, String name, NotNullsLinkedHashSet filters) {
        return accept(dir, name);
    }

    public abstract boolean accept(File dir, String name);

    public File filter(File dir, String name, Predicate<File> predicate) {
        File file = new File(dir + File.separator + name);
        return predicate.evaluate(file) ? file : null;
    }

    protected int getFilterType() {
        return filterType;
    }

    protected void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    @Override
    public Collection<String> getResourcePaths(final ClassLoader classLoader)
            throws IOException {
        return transformURLIntoStringPaths(getResources(classLoader));
    }

    protected Collection<String> transformURLIntoStringPaths(Collection<URL> urls) {
        return CollectionUtils.collect(urls, URL::getFile);
    }

    public Collection<URL> getResources(ClassLoader classLoader)
            throws IOException {
        return getResources();
    }

    public Collection<URL> getResources() {
        return new ArrayList<>();
    }

    public String printCriterias() {
        return StringUtils.EMPTY;
    }

    protected Collection<URL> transformFilesIntoURLs(File[] files) {
        try {
            if (files != null) {
                final List<File> files_ = Arrays.asList(files);
                return transformFilesIntoURLs(files_);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    protected Collection<URL> transformFilesIntoURLs(Collection<File> files) {
        return CollectionUtils.collect(files, file -> {
            try {
                return new URL(file.getPath());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    protected Collection<URL> transformJarIntoURLs(File file) {
        try {
            final Enumeration<JarEntry> entries = new JarFile(file).entries();
            final ArrayList<JarEntry> entriesList = Collections.list(entries);
            return CollectionUtils.collect(entriesList, jarEntry -> {
                try {
                    return new URL(file.getPath() + "!" + File.separator + jarEntry.getName());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            });
        } catch (Exception ignored) {
        }
        return null;
    }
}

