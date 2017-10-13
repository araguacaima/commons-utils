package com.araguacaima.commons.utils;

import com.araguacaima.commons.utils.file.FileUtilsFilenameFilter;
import jreversepro.parser.ClassParserException;
import jreversepro.reflect.JClassInfo;
import jreversepro.revengine.JSerializer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Component
public class ClassLoaderUtils {

    private static final Logger log = LoggerFactory.getLogger(ClassLoaderUtils.class);
    public static Comparator<? super Class> CLASS_COMPARATOR = new ClassNameCompare();
    private final ClassNameCompare CLASS_NAME_COMPARE = new ClassNameCompare();
    private final ClassNameCompareIgnoreCase CLASS_NAME_COMPARE_IGNORE_CASE = new ClassNameCompareIgnoreCase();
    private final Class[] parameters = new Class[]{URL.class};
    private final Class[] parametersAddURL = new Class[]{URL.class};
    private final Class[] parametersFindClass = new Class[]{String.class};
    private final Map<String, NotNullsLinkedHashSet<URL>> resourcesAndPaths = new HashMap<>();
    private final Class<URLClassLoader> sysclass = URLClassLoader.class;
    private String classPath;
    private Collection<String> classPathCollection = new NotNullsLinkedHashSet<>();
    private FileUtils fileUtils;
    private boolean isClassPathLoaded = false;
    private MapUtils mapUtils;
    private Method methodAddURL;
    private StringUtils stringUtils;
    private URLClassLoader sysloader;
    private boolean sysloaderDirty;

    private ClassLoaderUtils() {
    }

    @Autowired
    public ClassLoaderUtils(MapUtils mapUtils, StringUtils stringUtils) {
        this.mapUtils = mapUtils;
        this.stringUtils = stringUtils;
    }

    public ClassLoaderUtils(ClassLoader classLoader) {
        setClassLoader(classLoader);
    }

    public ClassLoaderUtils(URLClassLoader sysloader) {
        this.sysloader = sysloader;
    }

    /**
     * Add file to classPath
     *
     * @param s File name
     * @throws IOException IOException
     */
    public void addFile(String s)
            throws IOException {
        File f = new File(s);
        addFile(f);
    }

    /**
     * Add file to classPath
     *
     * @param f File object
     * @throws IOException IOException
     */
    public void addFile(File f)
            throws IOException {
        URL url;
        try {
            url = new URL(f.getPath());
        } catch (MalformedURLException murle) {
            url = f.toURL();
        }
        log.debug("Attempting to add the URL: " + url.getPath() + " to the classpath");
        addURL(url);
    }

    /**
     * Add URL to classPath
     *
     * @param u URL
     * @throws IOException IOException
     */
    public void addURL(URL u)
            throws IOException {
        if (!isClassPathLoaded) {
            loadClassPath();
        }
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL urls[] = sysLoader.getURLs();
        for (URL url : urls) {
            if (url.getPath().equals(u.getPath())) {
                log.debug("URL " + u + " is already in the classPath");
                return;
            }
        }
        Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysLoader, u);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

    private void loadClassPath() {
        final URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        if (!StringUtils.isBlank(classPath) && !isClassPathLoaded) {
            Collection<String> classPath = Arrays.asList(this.classPath.split(StringUtils.SEMICOLON_SYMBOL));
            IterableUtils.forEach(classPath, classPathEntry -> {
                try {
                    URL u = new URL("file://" + classPathEntry);
                    log.debug("Appending resource '" + u + "' to the classpath");
                    Class<?> sysclass = URLClassLoader.class;
                    try {
                        Method method = sysclass.getDeclaredMethod("addURL", parameters);
                        method.setAccessible(true);
                        method.invoke(sysLoader, u);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                } catch (MalformedURLException ignored) {
                }
            });
            isClassPathLoaded = true;
        }
    }

    protected String asClassResourceName(String resource) {
        if (!resource.startsWith("/")) {
            resource = "/" + resource;
        }
        resource = resource.replace('.', '/');
        resource = resource + ".class";
        return resource;
    }

    /**
     * Returns the URL of the resource denoted by the specified * class name, as prescribed by the class path.
     *
     * @param className Name of the class.
     * @return Class URL, or null of the class was not found.
     */
    public URL findClass(final String className) {
        return ClassLoaderUtils.class.getResource(asClassResourceName(className));
    }

    /**
     * Searches the classpath for all classes matching a specified search criteria, returning them in a Collection.
     * The search criteria is specified via incoming filters parameters of the type FileUtilsFilenameFilterImpl
     * <br>
     *
     * @param filters A set of the filters to be applied to the current ClassLoader reacheable's files
     * @return A new Map that contains a set of classes for each on interface parckage or inheritance incoming filter
     * @throws ClassNotFoundException               if the current thread's classloader cannot load
     *                                              a requested class for any reason
     * @throws java.io.UnsupportedEncodingException If any path constains an invalid character
     */

    public Collection<Class<?>> findClasses(NotNullsLinkedHashSet<FileUtilsFilenameFilter> filters)
            throws ClassNotFoundException, IOException {
        Collection<File> classpath = findResources(filters, new NotNullsLinkedHashSet<>());
        return getClassesFromClasspath(classpath);
    }

    /**
     * Searches the classpath for all resources matching a specified search criteria, excluding fileExclusions Set
     * criterias
     * from classpath before searching for, returning them in a Collection.
     * The search criteria is specified via incoming filters parameters of the type FileUtilsFilenameFilterImpl
     * <br>
     *
     * @param filters         A set of the filters to be applied to the current ClassLoader reacheable's files
     * @param filesExclusions A Collection of files names that will be excluded from the classpath
     * @return A new Map that contains a set of classes for each on interface parckage or inheritance incoming filter
     * @throws ClassNotFoundException               if the current thread's classloader cannot load
     *                                              a requested class for any reason
     * @throws java.io.UnsupportedEncodingException If any path constains an invalid character
     */
    public Collection<File> findResources(NotNullsLinkedHashSet<FileUtilsFilenameFilter> filters,
                                          final NotNullsLinkedHashSet<FileUtilsFilenameFilter> filesExclusions)
            throws ClassNotFoundException, IOException {
        return findResources(filters, filesExclusions, null);
    }

    private Collection<Class<?>> getClassesFromClasspath(Collection<File> classpath) {
        NotNullsLinkedHashSet<Class<?>> result = new NotNullsLinkedHashSet<>();

        for (File fileClassPath : classpath) {
            String name = fileClassPath.getPath();

            boolean isValidClass = name.endsWith(".class");

            if (isValidClass) {
                Class<?> clazz = Object.class;
                JSerializer jSerializer = new JSerializer();
                JClassInfo infoClass;
                try {
                    log.debug("Attempting to load class identified by path: " + name);
                    File file = new File(name);
                    infoClass = jSerializer.loadClass(file);
                    log.debug("Bytecode for Class '" + infoClass.getThisClass(true) + "' reached!");
                    clazz = Class.forName(infoClass.getThisClass(true).replaceAll(StringUtils.SLASH, "."));
                    log.debug("Class '" + clazz.getName() + "' loaded!");
                } catch (ClassNotFoundException ignored) {
                    log.debug("ClassNotFoundException: " + ignored.getMessage());
                } catch (FileNotFoundException ignored) {
                    log.debug("FileNotFoundException: " + ignored.getMessage());
                } catch (StreamCorruptedException ignored) {
                    log.debug("StreamCorruptedException: " + ignored.getMessage());
                } catch (IOException ignored) {
                    log.debug("IOException: " + ignored.getMessage());
                } catch (ClassParserException ignored) {
                    log.debug("ClassParserException: " + ignored.getMessage());
                } catch (NoClassDefFoundError ignored) {
                    log.debug("NoClassDefFoundError: " + ignored.getMessage());
                } catch (NullPointerException ignored) {
                    log.debug("NullPointerException: " + ignored.getMessage());
                } catch (Exception ignored) {
                    log.debug("Exception: " + ignored.getMessage());
                } catch (Throwable ignored) {
                    log.debug("Throwable: " + ignored.getMessage());
                }
                if (clazz == Object.class) {
                    ClassLoaderUtils classLoaderUtils = new ClassLoaderUtils();

                    try {
                        String[] jarTokens = name.split(".jar");
                        String classFile = jarTokens[1].replaceFirst("\\.class", StringUtils.EMPTY).replaceAll(
                                StringUtils.DOUBLEBACKSLASH,
                                ".").replaceAll(StringUtils.SLASH, ".");
                        log.debug("classFile (1): " + classFile);
                        classFile = classFile.indexOf(".") == 0 ? classFile.replaceFirst(".",
                                StringUtils.EMPTY) : classFile;
                        log.debug("classFile (2): " + classFile);
                        clazz = classLoaderUtils.getClassLoader().loadClass(classFile);
                        log.debug("clazz: " + clazz.getName());
                    } catch (ClassNotFoundException ignored) {
                        log.debug("ClassNotFoundException: " + ignored.getMessage());
                    } catch (NoClassDefFoundError ignored) {
                        log.debug("NoClassDefFoundError: " + ignored.getMessage());
                    } catch (StringIndexOutOfBoundsException ignored) {
                        log.debug("StringIndexOutOfBoundsException: " + ignored.getMessage());
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                        log.debug("ArrayIndexOutOfBoundsException: " + ignored.getMessage());
                        String classname = stringUtils.replaceLast(fileClassPath.getName().replaceAll(StringUtils
                                        .BACKSLASH + StringUtils.BACKSLASH,
                                ".").replaceAll(StringUtils.SLASH, "."), ".class", StringUtils.EMPTY);
                        log.debug("Classname: " + classname);
                        String path = stringUtils.replaceLast(name, ".class", StringUtils.EMPTY);
                        log.debug("Path: " + classname);
                        try {
                            clazz = classLoaderUtils.getClassLoader().loadClass(classname);
                            log.debug("Class '" + clazz.getName() + "' loaded thru ClassLoader: " + classLoaderUtils
                                    .getClassLoader().getClass().getName());
                        } catch (ClassNotFoundException ignored2) {
                            try {
                                List folders = Arrays.asList(path.split(File.separator.equals(StringUtils.SLASH) ?
                                        StringUtils.SLASH : StringUtils.DOUBLEBACKSLASH));
                                Collections.reverse(folders);
                                log.debug("Folders reversed: '" + folders);
                                String classPackage = StringUtils.EMPTY;
                                for (Object folder : folders) {
                                    try {
                                        classPackage = folder + "." + classPackage;
                                        log.debug("classPackage + classname: '" + classPackage + classname);
                                        clazz = classLoaderUtils.getClassLoader().loadClass(classPackage + classname);
                                        break;
                                    } catch (ClassNotFoundException ignored4) {
                                        log.debug("ClassNotFoundException: " + classname + ". " + ignored4.getMessage
                                                ());
                                    }
                                }
                                if (clazz == Object.class) {
                                    throw new Exception("The related classname '" + name + "' is not found");
                                }
                            } catch (ClassNotFoundException ignored3) {
                                log.debug("ClassNotFoundException: " + classname + ". " + ignored3.getMessage());
                            } catch (Exception ignored3) {
                                log.debug("Exception: " + classname + ". " + ignored3.getMessage());
                            }
                        }
                    } catch (NullPointerException ignored) {
                    }
                }
                if (clazz != Object.class) {
                    log.debug("Class: '" + clazz.getName() + " added!");
                    result.add(clazz);
                }
            }
        }
        List<Class<?>> classesOrdered = new ArrayList<>(result);
        classesOrdered.sort(getClassNameCompare());

        return classesOrdered;
    }

    /**
     * Searches the classpath for all resources matching a specified search criteria, excluding fileExclusions Set
     * criterias
     * from classpath before searching for, returning them in a Collection.
     * The search criteria is specified via incoming filters parameters of the type FileUtilsFilenameFilterImpl
     * <br>
     *
     * @param filters         A set of the filters to be applied to the current ClassLoader reacheable's files
     * @param filesExclusions A Collection of files names that will be excluded from the classpath
     * @param classLoader     The classLoader to find resources
     * @return A new Map that contains a set of classes for each on interface parckage or inheritance incoming filter
     */
    public Collection<File> findResources(NotNullsLinkedHashSet<FileUtilsFilenameFilter> filters,
                                          final NotNullsLinkedHashSet<FileUtilsFilenameFilter> filesExclusions,
                                          final ClassLoader classLoader) {

        final NotNullsLinkedHashSet<URL> classPaths = new NotNullsLinkedHashSet<>();

        if (filters != null) {
            IterableUtils.forEach(filters, o -> {
                try {
                    if (classLoader == null) {
                        classPaths.addAll(o.getResources());
                    } else {
                        classPaths.addAll(o.getResources(classLoader));
                    }
                } catch (NullPointerException | IOException npe) {
                    log.info("class '" + o + "' could not be found");
                }
            });
        }

        if (filesExclusions != null) {
            if (filters != null) {
                filters.addAll(filesExclusions);
            }
        }

        Collection<File> classPath_ = CollectionUtils.collect(classPaths, url -> {
            File transformedClassPath = null;
            try {

                if (url != null) {
                    transformedClassPath = new File((URL.class).isInstance(url) ? url.getFile() : url.toString());
                    String fileNameDecoded = URLDecoder.decode(transformedClassPath.getPath(), "UTF-8");
                    fileNameDecoded = fileNameDecoded.replaceFirst("file:\\\\", StringUtils.EMPTY);
                    transformedClassPath = new File(fileNameDecoded);
                }
            } catch (IOException | ClassCastException ignored) {

            }
            return transformedClassPath;

        });

        NotNullsLinkedHashSet<Class> filesPaths = new NotNullsLinkedHashSet<>();

        fileUtils.addFilters(filters);
        final StringBuffer strbuff = new StringBuffer();
        strbuff.append("\n");
        IterableUtils.forEach(filters, filter -> strbuff.append(filter.printCriterias()).append("\n"));
        log.debug("Searching the following criterias: " + strbuff.toString() + ". on classpaths: '" + classPaths);
        List<File> filesPathsOrdered = new ArrayList<>(fileUtils.listFiles(classPath_));
        log.debug("filesPaths before sorting: " + filesPathsOrdered);
        filesPathsOrdered.sort(FileUtils.getFileUtilsFilenameFilterCompare());
        log.debug("filesPathsOrdered after sorting: " + filesPathsOrdered);
        return filesPathsOrdered;

    }

    public ClassLoader getClassLoader() {
        return sysloader == null ? this.getClass().getClassLoader() : sysloader;
    }

    public ClassNameCompare getClassNameCompare() {
        return CLASS_NAME_COMPARE;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.sysloader = URLClassLoader.newInstance(new URL[]{classLoader.getResource(".")});
    }

    /**
     * Searches the classpath for all classes matching a specified search criteria excluding fileExclusions Set
     * criterias
     * from classpath before searching for, returning them in a Collection.
     * The search criteria is specified via incoming filters parameters of the type FileUtilsFilenameFilterImpl
     * <br>
     *
     * @param filters         A set of the filters to be applied to the current ClassLoader reacheable's files
     * @param filesExclusions A Collection of files names that will be excluded from the classpath
     * @return A new Map that contains a set of classes for each on interface parckage or inheritance incoming filter
     * @throws ClassNotFoundException               if the current thread's classloader cannot load
     *                                              a requested class for any reason
     * @throws java.io.UnsupportedEncodingException If any path constains an invalid character
     */
    public Collection<Class<?>> findClasses(NotNullsLinkedHashSet<FileUtilsFilenameFilter> filters,
                                            final NotNullsLinkedHashSet<FileUtilsFilenameFilter> filesExclusions)
            throws ClassNotFoundException, IOException {
        Collection<File> classpath = findResources(filters, filesExclusions);
        return getClassesFromClasspath(classpath);
    }

    /**
     * Returns the URL of the resource denoted by the specified * class name, as prescribed by the class path.
     *
     * @param resourceName Name of the class.
     * @return Class URL, or null of the class was not found.
     */
    public URL findResource(final String resourceName) {
        return ClassLoaderUtils.class.getResource(asResourceName(resourceName));
    }

    protected String asResourceName(String resource) {
        if (!resource.startsWith("/")) {
            resource = "/" + resource;
        }
        resource = resource.replace('.', '/');
        return resource;
    }

    /**
     * Searches the classpath for all resources matching a specified search criteria, returning them in a Collection.
     * The search criteria is specified via incoming filters parameters of the type FileUtilsFilenameFilterImpl
     * <br>
     *
     * @param filters A set of the filters to be applied to the current ClassLoader reacheable's files
     * @return A new Map that contains a set of classes for each on interface parckage or inheritance incoming filter
     * @throws ClassNotFoundException               if the current thread's classloader cannot load
     *                                              a requested class for any reason
     * @throws java.io.UnsupportedEncodingException If any path constains an invalid character
     */

    public Collection<File> findResources(NotNullsLinkedHashSet<FileUtilsFilenameFilter> filters)
            throws ClassNotFoundException, IOException {

        return findResources(filters, new NotNullsLinkedHashSet());
    }

    /**
     * Searches the classpath for all resources matching a specified search criteria, excluding fileExclusions Set
     * criterias
     * from classpath before searching for, returning them in a Collection.
     * The search criteria is specified via incoming filters parameters of the type FileUtilsFilenameFilterImpl
     * <br>
     *
     * @param filters     A set of the filters to be applied to the current ClassLoader reacheable's files
     * @param classLoader The classLoader to find resources
     * @return A new Map that contains a set of classes for each on interface parckage or inheritance incoming filter
     * @throws ClassNotFoundException               if the current thread's classloader cannot load
     *                                              a requested class for any reason
     * @throws java.io.UnsupportedEncodingException If any path constains an invalid character
     */
    public Collection<File> findResources(NotNullsLinkedHashSet<FileUtilsFilenameFilter> filters,
                                          ClassLoader classLoader)
            throws ClassNotFoundException, IOException {
        return findResources(filters, null, classLoader);
    }

    public ClassNameCompareIgnoreCase getClassNameCompareIgnoreCase() {
        return CLASS_NAME_COMPARE_IGNORE_CASE;
    }

    protected String getClasspath()
            throws MalformedURLException {
        if (classPath == null) {
            setClasspath(System.getProperty("java.class.path"));
        }
        return classPath;
    }

    public void setClasspath(String classPath)
            throws MalformedURLException {
        this.classPath = classPath;
        if (!StringUtils.isBlank(this.classPath)) {
            classPathCollection = Arrays.asList(this.classPath.split(StringUtils.SEMICOLON_SYMBOL));
        }

        if (!isClassPathLoaded) {
            loadClassPath();
        }
        fillClassPathClassLoaderMap(null, null);
    }

    public String getPathForResource(final String resource) {
        NotNullsLinkedHashSet<Object> result = new NotNullsLinkedHashSet<Object>((mapUtils.find(resourcesAndPaths,
                (Predicate) o -> {
                    String file = (String) o;
                    return file.equals(resource) || file.endsWith(resource);
                },
                null,
                MapUtils.EVALUATE_JUST_KEY)).values());
        if (result != null && result.size() > 0) {
            try {
                String file = ((URL) result.toArray()[0]).getFile();
                if (file.indexOf(StringUtils.SLASH) == 0) {
                    file = file.replaceFirst(StringUtils.SLASH, StringUtils.EMPTY);
                }
                return file;
            } catch (Throwable t) {
                return StringUtils.EMPTY;
            }
        }

        return StringUtils.EMPTY;
    }

    public Collection<URL> getResources(String path) {
        Collection<URL> resources = new ArrayList<URL>();
        try {
            resources = Collections.list(ClassLoaderUtils.class.getClassLoader().getResources(path));
        } catch (Exception ignored) {
        }
        Collection<URL> result = new NotNullsLinkedHashSet<>();
        NotNullsLinkedHashSet<URL> resourceAndPaths = resourcesAndPaths.get(path);
        if (resourceAndPaths != null) {
            result.addAll(resourceAndPaths);
        }
        if (resources != null) {
            result.addAll(resources);
        }
        return result;
    }

    public void init(String classPath)
            throws MalformedURLException {
        init(null, null, classPath);
    }

    public void init(String applicationName, String contextName, String classPath)
            throws MalformedURLException {
        this.classPath = classPath;
        if (!StringUtils.isBlank(this.classPath)) {
            Collection<String> classPathSplitted = Arrays.asList(this.classPath.split(StringUtils.SEMICOLON_SYMBOL));
            for (String aClassPathSplitted : classPathSplitted) {
                final String classPathToken = aClassPathSplitted;
                String classPathStored = IterableUtils.find(classPathCollection, o -> o.endsWith(classPathToken));
                if (StringUtils.isBlank(classPathStored)) {
                    classPathCollection.add(classPathToken);
                }
            }
        }
        if (!isClassPathLoaded) {

            loadClassPath();
        }
        fillClassPathClassLoaderMap(applicationName, contextName);
    }

    private void fillClassPathClassLoaderMap(String applicationName, String contextName)
            throws MalformedURLException {
        for (String aClassPathCollection : classPathCollection) {
            final File resource = new File(aClassPathCollection);
            String name = resource.getPath();
            String nameStored;
            if (!StringUtils.isBlank(applicationName)) {
                List<String> fileTokens = Arrays.asList(name.replaceAll(StringUtils.DOUBLEBACKSLASH,
                        StringUtils.SLASH).split(StringUtils.SLASH));
                List<String> fileTokensResult = new ArrayList<>();
                Collections.reverse(fileTokens);
                for (String token : fileTokens) {
                    if (token.equals(applicationName)) {
                        break;
                    } else {
                        fileTokensResult.add(token);
                    }
                }
                Collections.reverse(fileTokensResult);
                StringBuilder sb = new StringBuilder();
                for (String aFileTokensResult : fileTokensResult) {
                    String token = aFileTokensResult;
                    sb.append(token).append(StringUtils.SLASH);
                }
                if (sb.lastIndexOf(StringUtils.SLASH) == sb.length()) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                nameStored = StringUtils.isBlank(contextName) ? sb.toString() : contextName + StringUtils.SLASH + sb
                        .toString();
            } else {
                nameStored = resource.getName();
            }

            if (nameStored.endsWith(StringUtils.SLASH)) {
                nameStored = nameStored.substring(0, nameStored.length() - 1);
            }
            NotNullsLinkedHashSet<URL> path = resourcesAndPaths.get(nameStored);
            final URL url = resource.toURI().toURL();
            if (CollectionUtils.isEmpty(path)) {
                path = new NotNullsLinkedHashSet<>();
                path.add(url);
                resourcesAndPaths.put(nameStored, path);
            } else {
                if (StringUtils.isBlank(contextName)) {
                    path.add(url);
                }
            }
        }
    }

    public void init(String applicationName, String classPath)
            throws MalformedURLException {
        init(applicationName, null, classPath);
    }

    @PostConstruct
    public void init() {
        try {
            methodAddURL = sysclass.getDeclaredMethod("addURL", parametersAddURL);
            methodAddURL.setAccessible(true);
            Method methodFindClass = sysclass.getDeclaredMethod("findClass", parametersFindClass);
            methodFindClass.setAccessible(true);
            sysloaderDirty = false;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("Impossible to deal with ClassLoader because of the following Exception: " + e
                    .getMessage());
        }
    }

    public Collection<Class> loadClassesInsideJar(File jarFileFullPath)
            throws ClassNotFoundException, IOException, NoSuchFieldException, IllegalAccessException {
        Collection<Class> classes = new ArrayList<>();
        if (jarFileFullPath != null) {
            JarFile jarFile = new JarFile(jarFileFullPath);
            Enumeration<JarEntry> e = jarFile.entries();

            URL[] urls = {new URL("jar:file:" + jarFileFullPath.getPath() + "!/")};

            if (sysloader == null || sysloaderDirty) {
                sysloader = URLClassLoader.newInstance(urls);

                fixCurrentClassLoader();

                sysloaderDirty = false;
            }

            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                // -6 because of .class
                String classNamePackage = je.getName().substring(0, je.getName().length() - 6);
                String className = classNamePackage.replace('/', '.');
                try {
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException ignored) {
                        addURLToDependencies(new URL("jar:file:" + jarFileFullPath.getPath() + "!/" +
                                classNamePackage));
                        Class c = sysloader.loadClass(className);
                        classes.add(c);
                    }
                } catch (NoClassDefFoundError | ClassNotFoundException notFoundException) {
                    throw notFoundException;
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        return classes;
    }

    public void fixCurrentClassLoader() {
        if (!sysloaderDirty && sysloader != null) {
            final ClassLoader cl = this.getClass().getClassLoader();

            org.springframework.util.ReflectionUtils.doWithFields(sysloader.getClass(), field -> {

                log.debug("Found field '" + field + "' in type " + field.getDeclaringClass() + ". The " + "recently "
                        + "created sysloader has been adapted to ensure that its parent " + "corresponds with the " +
                        "current classloader '" + cl.getClass().getName() + "' ");
                field.setAccessible(true);
                field.set(sysloader, cl);

            }, field -> field.getName().equals("parent"));
        }
    }

    public void addURLToDependencies(URL urlToBeAddedToDependencies)
            throws IOException, NoSuchFieldException, IllegalAccessException {
        if (sysloader == null || sysloaderDirty) {
            URL[] urls = {urlToBeAddedToDependencies};
            sysloader = URLClassLoader.newInstance(urls);
            fixCurrentClassLoader();
            sysloaderDirty = false;
        }

        try {
            Object[] args = {urlToBeAddedToDependencies};
            methodAddURL.invoke(sysloader, args);
        } catch (Throwable t) {
            t.printStackTrace();
            throw new IOException("Error, could not add URL to system classloader");
        }

    }

    public void loadResourcesIntoClassLoader(Set<String> resources)
            throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        for (String resource : resources) {
            log.debug("Adding resource '" + resource + "' into main ClassLoader");
            addResourceToDependencies(resource);
        }

    }

    public void addResourceToDependencies(String resourceToBeAddedToDependencies)
            throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        addURLToDependencies(new File(resourceToBeAddedToDependencies).toURI().toURL());
    }

    public void printClasspath()
            throws MalformedURLException {
        log.info("\nClasspath:");
        StringTokenizer tokenizer = new StringTokenizer(getClasspath(), File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            System.out.println(tokenizer.nextToken());
        }
    }

    private void printUsage() {

        log.info("\nSyntax: java ClassLoaderUtil [options] className");
        log.info("");
        log.info("where options include:");
        log.info("");
        log.info("\t-help Prints usage information.");
        log.info("");
        log.info("Examples:");
        log.info("\tjava ClassLoaderUtil MyClass");
        log.info("\tjava ClassLoaderUtil my.package.ClassLoaderUtil");
        System.exit(0);
    }

    public void reloadClass(Class<?> clazz)
            throws ClassNotFoundException, IOException {
        removeClass(clazz);
        loadClass(clazz);
    }

    public void removeClass(Class clazz) {
        if (clazz != null) {

            try {
                URLClassLoader cl = (URLClassLoader) clazz.getClassLoader();
                cl.close();
            } catch (Throwable t) {
                log.debug("Class '" + clazz.getName() + "' skiped from Classloader unloading step because of it is "
                        + "not loaded into it");
            } finally {
                System.gc();
                System.runFinalization();
            }
        }
    }

    public void loadClass(Class classToBeLoaded)
            throws ClassNotFoundException {
        loadClass(classToBeLoaded.getName());
    }

    public Class loadClass(String classToBeLoaded)
            throws ClassNotFoundException {
        if (sysloader == null) {
            return this.getClass().getClassLoader().loadClass(classToBeLoaded);
        }
        return sysloader.loadClass(classToBeLoaded);
    }

    public void removeClass(String className) {
        if (StringUtils.isNotBlank(className)) {
            try {
                removeClass(Class.forName(className));
            } catch (Throwable t) {
                log.debug("Class '" + className + "' skiped from Classloader unloading step because of it is not " +
                        "loaded into it");
            } finally {
                System.gc();
                System.runFinalization();
            }
        }
    }

    public void removeClassesInsideJar(File jarFileFullPath)
            throws IOException, NoSuchFieldException, IllegalAccessException {
        if (jarFileFullPath != null) {
            JarFile jarFile = new JarFile(jarFileFullPath);
            Enumeration<JarEntry> e = jarFile.entries();

            URL[] urls = {new URL("jar:file:" + jarFileFullPath.getPath() + "!/")};

            if (sysloader == null || sysloaderDirty) {
                sysloader = URLClassLoader.newInstance(urls);

                fixCurrentClassLoader();

                sysloaderDirty = false;
            }

            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                // -6 because of .class
                String classNamePackage = je.getName().substring(0, je.getName().length() - 6);
                String className = classNamePackage.replace('/', '.');
                try {
                    try {
                        Class c = Class.forName(className);
                        removeClass(c);
                    } catch (ClassNotFoundException ignored) {
                        if (this.sysloader.findResource(classNamePackage) != null) {
                            System.gc();
                            System.runFinalization();
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    public void removeJar(File jarFileFullPath)
            throws IOException {
        if (jarFileFullPath != null) {
            try {
                JarFile jarFile = new JarFile(jarFileFullPath);
                Enumeration<JarEntry> e = jarFile.entries();

                while (e.hasMoreElements()) {
                    JarEntry je = e.nextElement();
                    if (je.isDirectory() || !je.getName().endsWith(".class")) {
                        continue;
                    }
                    // -6 because of .class
                    String classNamePackage = je.getName().substring(0, je.getName().length() - 6);
                    String className = classNamePackage.replace('/', '.');
                    try {
                        Class clazz = Class.forName(className, false, sysloader);
                        URLClassLoader cl = (URLClassLoader) clazz.getClassLoader();
                        cl.close();
                    } catch (Throwable t) {
                        log.debug("Class '" + className + "' skiped from Classloader unloading step because of it is " +
                                "" + "" + "" + "not loaded into it");
                    }
                }
            } catch (FileNotFoundException ignored) {
            } finally {
                System.gc();
                System.runFinalization();
                try {
                    sysloader.close();
                    sysloaderDirty = true;
                } catch (Throwable ignored) {
                }
                unloadJarFromClassLoader(jarFileFullPath);
            }
        }
    }

    private void unloadJarFromClassLoader(File jarFileFullPath) {
        try {

            // open cached connection to just created clone of test JAR
            URL jarUrl = new URL("jar", "", -1, jarFileFullPath.toURI().toString() + "!/");
            URLConnection c = jarUrl.openConnection();
            c.setUseCaches(true);

            ((JarURLConnection) c).getJarFile().close();
        } catch (Throwable e) {
            log.error(e.getMessage());
        } finally {
            try {
                FileUtils.forceDelete(jarFileFullPath);
            } catch (Throwable ignored) {
            }
        }
    }

    public void removeLoadedClasses()
            throws IOException {
        if (sysloader != null) {
            sysloader.close();
            sysloaderDirty = true;
        }
    }

    /**
     * Validates the class path and reports any non-existent or invalid class path entries.
     * <br>
     * Valid class path entries include directories, <code>.zip</code> and <code>.jar</code> files.
     *
     * @throws MalformedURLException If classpath is not valid
     */
    public void validate()
            throws MalformedURLException {

        StringTokenizer tokenizer = new StringTokenizer(getClasspath(), File.pathSeparator);
        while (tokenizer.hasMoreTokens()) {
            String element = tokenizer.nextToken();
            File f = new File(element);

            if (!f.exists()) {
                log.info("\nClasspath element '" + element + "' " + "does not exist.");
            } else if ((!f.isDirectory()) && (!element.toLowerCase().endsWith(".jar")) && (!element.toLowerCase()
                    .endsWith(
                    ".zip"))) {
                log.info("\nClasspath element '" + element + "' " + "is not a directory, .jar file, or .zip file.");
            }
        }
    }

    /**
     * Prints the absolute pathname of the class file containing the specified class name, as prescribed by the
     * class path.
     *
     * @param className Name of the class.
     * @throws MalformedURLException If classpath is not valid
     */
    public void which(String className)
            throws MalformedURLException {

        URL classUrl = findClass(className);

        if (classUrl == null) {
            log.info("\nClass '" + className + "' not found.");
        } else {
            log.info("\nClass '" + className + "' found in \n'" + classUrl.getFile() + "'");
        }
        validate();
        printClasspath();
    }

    private static class ClassNameCompare implements Comparator<Class> {
        public int compare(Class o1, Class o2) {
            String path1 = o1.getName();
            String path2 = o2.getName();
            return path1.compareTo(path2);
        }
    }

    private static class ClassNameCompareIgnoreCase implements Comparator<Class> {
        public int compare(Class o1, Class o2) {
            String path1 = o1.getName();
            String path2 = o2.getName();
            return path1.compareToIgnoreCase(path2);
        }
    }

}
