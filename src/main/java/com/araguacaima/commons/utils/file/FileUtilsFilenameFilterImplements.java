package com.araguacaima.commons.utils.file;

import com.araguacaima.commons.utils.StringUtils;
import jreversepro.parser.ClassParserException;
import jreversepro.reflect.JClassInfo;
import jreversepro.revengine.JSerializer;
import org.apache.commons.collections4.IterableUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FileUtilsFilenameFilterImplements extends FileUtilsFilenameFilterImpl {

    public static final int DEFAULT_FILTER_TYPE = FileUtilsFilenameFilter.RESOURCE_DIR_OR_FILE_FILTER_EQUALS;
    private final ClassLoader classLoader;
    private Class interfaceCriteria;

    public FileUtilsFilenameFilterImplements() {
        super();
        this.filterType = DEFAULT_FILTER_TYPE;
        this.classLoader = FileUtilsFilenameFilterImplements.class.getClassLoader();
    }

    public FileUtilsFilenameFilterImplements(Class interfaceCriteria, ClassLoader classLoader) {
        this.interfaceCriteria = interfaceCriteria;
        this.classLoader = classLoader;
        this.filterType = DEFAULT_FILTER_TYPE;
    }

    public FileUtilsFilenameFilterImplements(Class interfaceCriteria, ClassLoader classLoader, int filterType) {
        this.interfaceCriteria = interfaceCriteria;
        this.classLoader = classLoader;
        this.filterType = filterType;
    }

    public boolean accept(File dir, String name) {

        if (dir == null || interfaceCriteria == null) {
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
            } catch (ClassNotFoundException | IOException | ClassParserException ignored) {
            }

            if (clazz == Object.class)
                try {
                    String[] jarTokens = name.split(".jar");
                    String classFile = jarTokens[1].replaceFirst("\\.class",
                            StringUtils.EMPTY).replaceAll(StringUtils.DOUBLEBACKSLASH,
                            ".").replaceAll(StringUtils.SLASH, ".");
                    classFile = classFile.indexOf(".") == 0 ? classFile.replaceFirst(".",
                            StringUtils.EMPTY) : classFile;
                    clazz = classLoader.loadClass(classFile);

                } catch (ClassNotFoundException | NoClassDefFoundError | StringIndexOutOfBoundsException |
                        NullPointerException ignored) {
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    String classname = stringUtils.replaceLast(name.replaceAll(StringUtils.BACKSLASH + StringUtils
                                    .BACKSLASH,
                            ".").replaceAll(StringUtils.SLASH, "."), ".class", StringUtils.EMPTY);
                    try {
                        clazz = classLoader.loadClass(classname);
                    } catch (NoClassDefFoundError ignored1) {
                    } catch (ClassNotFoundException ignored2) {
                        try {
                            List folders = Arrays.asList(dir.getPath().split(File.separator.equals(StringUtils.SLASH)
                                    ? StringUtils.SLASH : StringUtils.BACKSLASH + StringUtils.BACKSLASH));
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
                        } catch (Exception ignored3) {

                        }
                    }
                }
            result = checkWhetherOrNotSuperclassesImplementsCriteria(clazz);
        } else {
            result = false;
        }
        return result;
    }

    private boolean applyFilterToInterface(Class clazz) {
        if (clazz != null && clazz.isInterface()) {
            String clazzName = clazz.getName();
            switch (filterType) {
                case RESOURCE_FILE_FILTER_EQUALS:
                    return clazzName.equals(interfaceCriteria.getName());

                case RESOURCE_FILE_FILTER_MATCHES:
                    return clazzName.matches(interfaceCriteria.getName());

                case RESOURCE_FILE_FILTER_STARTS:
                    return clazzName.startsWith(interfaceCriteria.getName());

                case RESOURCE_FILE_FILTER_ENDS:
                    return clazzName.endsWith(interfaceCriteria.getName());

                case RESOURCE_FILE_FILTER_CONTAINS:
                    return clazzName.contains(interfaceCriteria.getName());

                case RESOURCE_FILE_FILTER_NOT_EQUAL:
                    return !clazzName.equals(interfaceCriteria.getName());

                case RESOURCE_FILE_FILTER_NOT_MATCHES:
                    return !clazzName.matches(interfaceCriteria.getName());

                case RESOURCE_FILE_FILTER_NOT_STARTS:
                    return !clazzName.startsWith(interfaceCriteria.getName());

                case RESOURCE_FILE_FILTER_NOT_ENDS:
                    return !clazzName.endsWith(interfaceCriteria.getName());

                case RESOURCE_FILE_FILTER_NOT_CONTAINS:
                    return !clazzName.contains(interfaceCriteria.getName());

                case RESOURCE_DIR_OR_FILE_FILTER_EQUALS:
                    return clazzName.equals(interfaceCriteria.getName());

                case RESOURCE_DIR_OR_FILE_FILTER_MATCHES:
                    return clazzName.matches(interfaceCriteria.getName());

                case RESOURCE_DIR_OR_FILE_FILTER_STARTS:
                    return clazzName.startsWith(interfaceCriteria.getName());

                case RESOURCE_DIR_OR_FILE_FILTER_ENDS:
                    return clazzName.endsWith(interfaceCriteria.getName());

                case RESOURCE_DIR_OR_FILE_FILTER_CONTAINS:
                    return clazzName.contains(interfaceCriteria.getName());

                case RESOURCE_DIR_OR_FILE_FILTER_NOT_EQUAL:
                    return !clazzName.equals(interfaceCriteria.getName());

                case RESOURCE_DIR_OR_FILE_FILTER_NOT_MATCHES:
                    return !clazzName.matches(interfaceCriteria.getName());

                case RESOURCE_DIR_OR_FILE_FILTER_NOT_STARTS:
                    return !clazzName.startsWith(interfaceCriteria.getName());

                case RESOURCE_DIR_OR_FILE_FILTER_NOT_ENDS:
                    return !clazzName.endsWith(interfaceCriteria.getName());

                case RESOURCE_DIR_OR_FILE_FILTER_NOT_CONTAINS:
                    return !clazzName.contains(interfaceCriteria.getName());

                default:
                    return false;

            }
        }
        return false;
    }

    private boolean checkWhetherOrNotSuperclassesImplementsCriteria(Class clazz) {
        boolean result;
        if (clazz != null && clazz != Object.class) {
            Collection<Class> incomingInterfaces = Arrays.asList(clazz.getInterfaces());
            if (incomingInterfaces.isEmpty()) {
                Class superClass = clazz.getSuperclass();
                return checkWhetherOrNotSuperclassesImplementsCriteria(superClass);
            }
            result = IterableUtils.find(incomingInterfaces, this::applyFilterToInterface) != null;
            if (!result) {
                Class superClass = clazz.getSuperclass();
                return checkWhetherOrNotSuperclassesImplementsCriteria(superClass);
            }
            return true;
        }
        return false;
    }

    public int getFilterType() {
        return filterType;
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
    }

    public Class getInterfaceCriteria() {
        return interfaceCriteria;
    }


    @Override
    public Collection<URL> getResources(final ClassLoader classLoader)
            throws IOException {
        String interfaceCriteriaTransformed = interfaceCriteria.getName().contains(".") ? interfaceCriteria.getName()
                .substring(
                        0,
                        interfaceCriteria.getName().lastIndexOf(".")) : StringUtils.EMPTY;
        interfaceCriteriaTransformed = interfaceCriteriaTransformed.replaceAll("\\.", StringUtils.SLASH).replaceAll(
                "\\\\\\*",
                StringUtils.EMPTY);
        return Collections.list(classLoader.getResources(interfaceCriteriaTransformed));
    }

    @Override
    public Collection<String> getResourcePaths(final ClassLoader classLoader)
            throws IOException {
        return transformURLIntoStringPaths(getResources(classLoader));
    }

    public Collection<URL> getResources() {
        String interfaceCriteriaTransformed = interfaceCriteria.getName().contains(".") ? interfaceCriteria
                .getName().substring(
                0,
                interfaceCriteria.getName().lastIndexOf(".")) : StringUtils.EMPTY;
        interfaceCriteriaTransformed = interfaceCriteriaTransformed.replaceAll("\\.", StringUtils.SLASH).replaceAll(
                "\\\\\\*",
                StringUtils.EMPTY);
        return classLoaderUtils.getResources(interfaceCriteriaTransformed);
    }

    public String printCriterias() {
        return "[" + FileUtilsFilenameFilterImplements.class.getName() + "]" + "interfaceCriteria: " +
                interfaceCriteria.getName() + " | classLoader: " + classLoader.getClass().getName() + " | " +
                "filterType:" + " " + filterType;
    }

}