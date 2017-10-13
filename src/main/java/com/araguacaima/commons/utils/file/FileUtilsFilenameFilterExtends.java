package com.araguacaima.commons.utils.file;

import com.araguacaima.commons.utils.ClassLoaderUtils;
import com.araguacaima.commons.utils.StringUtils;
import jreversepro.parser.ClassParserException;
import jreversepro.reflect.JClassInfo;
import jreversepro.revengine.JSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

import java.net.URL;
import java.util.*;

@Component
public class FileUtilsFilenameFilterExtends extends FileUtilsFilenameFilterImpl {

    private Class superClassCriteria;
    private final ClassLoader classLoader;
    private StringUtils stringUtils;
    public static final int DEFAULT_FILTER_TYPE = FileUtilsFilenameFilter.RESOURCE_DIR_OR_FILE_FILTER_EQUALS;

    @Autowired
    public FileUtilsFilenameFilterExtends(ClassLoaderUtils classLoaderUtils, StringUtils stringUtils) {
        super();
        this.classLoaderUtils = classLoaderUtils;
        this.filterType = DEFAULT_FILTER_TYPE;
        this.classLoader = FileUtilsFilenameFilterExtends.class.getClassLoader();
        this.stringUtils = stringUtils;
    }

    public FileUtilsFilenameFilterExtends(Class superClassCriteria, ClassLoader classLoader) {
        this.superClassCriteria = superClassCriteria;
        this.classLoader = classLoader;
        this.filterType = DEFAULT_FILTER_TYPE;
    }

    public FileUtilsFilenameFilterExtends(Class superClassCriteria, ClassLoader classLoader, int filterType) {
        this.superClassCriteria = superClassCriteria;
        this.classLoader = classLoader;
        this.filterType = filterType;
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    public Class getSuperClassCriteria() {
        return superClassCriteria;
    }

    public int getFilterType() {
        return filterType;
    }

    public boolean accept(File dir, String name) {

        if (dir == null || superClassCriteria == null) {
            return true;
        }
        boolean isValidClass = name.endsWith(".class");
        boolean result;
        if (isValidClass) {
            Class clazz = Object.class;
            JSerializer jSerializer = new JSerializer();
            JClassInfo infoClass;
            try {
                File file = new File(name);
                infoClass = jSerializer.loadClass(file);
                clazz = Class.forName(infoClass.getThisClass(true).replaceAll(StringUtils.SLASH, "."));
            } catch (ClassNotFoundException | IOException | ClassParserException | NoClassDefFoundError ignored) {
            }
            if (clazz == Object.class) {
                try {
                    String[] jarTokens = name.split(".jar");
                    String classFile = jarTokens[1].replaceFirst("\\.class", StringUtils.EMPTY)
                            .replaceAll(StringUtils.DOUBLEBACKSLASH, ".");
                    classFile = classFile.indexOf(".") == 0
                                ? classFile.replaceFirst(".", StringUtils.EMPTY)
                                : classFile;
                    clazz = classLoader.loadClass(classFile).getSuperclass();

                } catch (ClassNotFoundException | NullPointerException | NoClassDefFoundError | StringIndexOutOfBoundsException ignored) {
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    String classname = stringUtils.replaceLast(name.replaceAll(StringUtils.BACKSLASH
+ StringUtils.BACKSLASH, ".")
                                                                      .replaceAll(StringUtils.SLASH, "."),
                                                              ".class",
                                                              StringUtils.EMPTY);
                    try {
                        clazz = classLoader.loadClass(classname).getSuperclass();
                    } catch (NoClassDefFoundError ignored1) {
                    } catch (ClassNotFoundException ignored2) {
                        try {
                            List folders = Arrays.asList(dir.getPath()
                                                                 .split(File.separator.equals(StringUtils.SLASH)
                                                                        ? StringUtils.SLASH
                                                                        : StringUtils.BACKSLASH + StringUtils.BACKSLASH));
                            Collections.reverse(folders);
                            String classPackage = StringUtils.EMPTY;
                            for (Object folder : folders) {
                                try {
                                    classPackage = folder + "." + classPackage;
                                    clazz = classLoader.loadClass(classPackage + classname);
                                    break;
                                } catch (ClassNotFoundException ignored4) {
                                }
                            }
                            if (clazz == Object.class) {
                                throw new Exception("The related classname '" + name + "' is not found");
                            }
                            clazz = clazz.getSuperclass();
                        } catch (Exception ignored3) {

                        }
                    }
                }
            }
            result = checkWhetherOrNotSuperclassesExtendsCriteria(clazz);
        } else {
            result = false;
        }
        return result;
    }

    public Collection<URL> getResources(ClassLoader classLoader) throws IOException {
        return getResources(classLoader, false);
    }

    public Collection<URL> getResources(ClassLoader classLoader, boolean onlyPath) throws IOException {
        String superClassCriteriaTransformed = superClassCriteria.getName().contains(".")
                                               ? superClassCriteria.getName()
                .substring(0, superClassCriteria.getName().lastIndexOf("."))
                                               : StringUtils.EMPTY;
        superClassCriteriaTransformed = superClassCriteriaTransformed.replaceAll("\\.", StringUtils.SLASH)
                .replaceAll("\\\\\\*", StringUtils.EMPTY);
        return Collections.list(classLoader.getResources(superClassCriteriaTransformed));
    }

    public Collection<URL> getResources() {
        String superClassCriteriaTransformed = superClassCriteria.getName().contains(".")
                                               ? superClassCriteria.getName()
                .substring(0, superClassCriteria.getName().lastIndexOf("."))
                                               : StringUtils.EMPTY;
        superClassCriteriaTransformed = superClassCriteriaTransformed.replaceAll("\\.", StringUtils.SLASH)
                .replaceAll("\\\\\\*", StringUtils.EMPTY);
        return classLoaderUtils.getResources(superClassCriteriaTransformed);
    }

    public String printCriterias() {
        return "["
               + FileUtilsFilenameFilterExtends.class.getName()
               + "]"
               + "superClassCriteria: "
               + superClassCriteria.getName()
               + " | classLoader: "
               + classLoader.getClass().getName()
               + " | filterType: "
               + filterType;
    }

    private boolean checkWhetherOrNotSuperclassesExtendsCriteria(Class clazz) {
        boolean result;
        if (clazz != null && clazz != Object.class) {
            String clazzName = clazz.getName();
            switch (filterType) {
                case RESOURCE_FILE_FILTER_EQUALS:
                    result = clazzName.equals(superClassCriteria.getName());
                    break;
                case RESOURCE_FILE_FILTER_MATCHES:
                    result = clazzName.matches(superClassCriteria.getName());
                    break;
                case RESOURCE_FILE_FILTER_STARTS:
                    result = clazzName.startsWith(superClassCriteria.getName());
                    break;
                case RESOURCE_FILE_FILTER_ENDS:
                    result = clazzName.endsWith(superClassCriteria.getName());
                    break;
                case RESOURCE_FILE_FILTER_CONTAINS:
                    result = clazzName.contains(superClassCriteria.getName());
                    break;
                case RESOURCE_FILE_FILTER_NOT_EQUAL:
                    result = !clazzName.equals(superClassCriteria.getName());
                    break;
                case RESOURCE_FILE_FILTER_NOT_MATCHES:
                    result = !clazzName.matches(superClassCriteria.getName());
                    break;
                case RESOURCE_FILE_FILTER_NOT_STARTS:
                    result = !clazzName.startsWith(superClassCriteria.getName());
                    break;
                case RESOURCE_FILE_FILTER_NOT_ENDS:
                    result = !clazzName.endsWith(superClassCriteria.getName());
                    break;
                case RESOURCE_FILE_FILTER_NOT_CONTAINS:
                    result = !clazzName.contains(superClassCriteria.getName());
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_EQUALS:
                    result = clazzName.equals(superClassCriteria.getName());
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_MATCHES:
                    result = clazzName.matches(superClassCriteria.getName());
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_STARTS:
                    result = clazzName.startsWith(superClassCriteria.getName());
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_ENDS:
                    result = clazzName.endsWith(superClassCriteria.getName());
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_CONTAINS:
                    result = clazzName.contains(superClassCriteria.getName());
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_EQUAL:
                    result = !clazzName.equals(superClassCriteria.getName());
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_MATCHES:
                    result = !clazzName.matches(superClassCriteria.getName());
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_STARTS:
                    result = !clazzName.startsWith(superClassCriteria.getName());
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_ENDS:
                    result = !clazzName.endsWith(superClassCriteria.getName());
                    break;
                case RESOURCE_DIR_OR_FILE_FILTER_NOT_CONTAINS:
                    result = !clazzName.contains(superClassCriteria.getName());
                    break;

                default:
                    result = false;
                    break;
            }
            if (!result) {
                Class superClass = clazz.getSuperclass();
                return checkWhetherOrNotSuperclassesExtendsCriteria(superClass);
            }
            return true;
        }

        return false;
    }

}