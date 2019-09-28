package com.araguacaima.commons.utils.json.parser;

import com.araguacaima.commons.utils.ReflectionUtils;
import com.araguacaima.commons.utils.StringUtils;
import org.apache.commons.collections4.IterableUtils;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Bean introspection utility.
 */
public class Beanspector<T> {

    private final Map<String, Field> fields = new HashMap<>();
    private final Map<String, Method> getters = new HashMap<>();
    private final Map<String, Method> setters = new HashMap<>();
    private final ClassLoader tclassloader;
    private Field lastTokenField;
    private Class<T> tclass;
    private T tobj;

    public Beanspector(final Class<T> tclass) {
        this(tclass, tclass.getClassLoader());
    }

    public Beanspector(Class<T> tclass, ClassLoader classLoader) {
        if (tclass == null) {
            throw new IllegalArgumentException("tclass is null");
        }
        this.tclass = tclass;
        this.tclassloader = classLoader;
        init();
    }

    public Beanspector(final T tobj) {
        this(tobj, tobj.getClass().getClassLoader());
    }

    public Beanspector(final T tobj, ClassLoader classLoader) {
        if (tobj == null) {
            throw new IllegalArgumentException("tobj is null");
        }
        this.tobj = tobj;
        this.tclassloader = classLoader;
        init();
    }

    private void init() {
        fill(tclass, tobj, getters, setters, fields);
    }

    private void fill(Class tclass,
                      final Object tobj,
                      final Map<String, Method> getters,
                      final Map<String, Method> setters,
                      final Map<String, Field> fields) {
        if (tclass == null) {
            if (tobj != null) {
                tclass = tobj.getClass();
            }
        }
        if (tclass != null) {
            for (final Method m : tclass.getMethods()) {
                if (isGetter(m)) {
                    getters.put(getterName(m), m);
                } else if (isSetter(m)) {
                    setters.put(setterName(m), m);
                }
            }
            // check type equality for getter-setter pairs
            final Set<String> pairs = new HashSet<>(getters.keySet());
            pairs.retainAll(setters.keySet());
            for (final String accessor : pairs) {
                final Class<?> getterClass = getters.get(accessor).getReturnType();
                final Class<?> setterClass = setters.get(accessor).getParameterTypes()[0];
                if (!getterClass.equals(setterClass)) {
                    throw new IllegalArgumentException(String.format(
                            "Accessor '%s' type mismatch, getter type is %s while setter type is %s",
                            accessor,
                            getterClass.getName(),
                            setterClass.getName()));
                }
            }
            for (final Field field : ReflectionUtils.getAllFieldsIncludingParents(tclass)) {
                field.setAccessible(true);
                fields.put(field.getName(), field);
            }
        } else {
            throw new IllegalArgumentException("Class and Object can not both be null");
        }
    }

    private boolean isGetter(final Method m) {
        return m.getParameterTypes().length == 0 && (m.getName().startsWith("get") || m.getName().startsWith("is"));
    }

    private String getterName(final Method m) {
        return StringUtils.uncapitalize(m.getName().startsWith("is") ? m.getName().substring(2) : m.getName()
                .startsWith(
                        "get") ? m.getName().substring(3) : m.getName());
    }

    private boolean isSetter(final Method m) {
        return m.getReturnType().equals(void.class) && m.getParameterTypes().length == 1 && (m.getName().startsWith(
                "set") || m.getName().startsWith("is"));
    }

    private String setterName(final Method m) {
        return StringUtils.uncapitalize(m.getName().replace("is", "").replace("set", ""));
    }

    public Class<?> getAccessorType(final String getterOrSetterName)
            throws Exception {
        final String[] tokens = getterOrSetterName.split("\\.");
        if (tokens.length > 1) {
            final String token = tokens[0].replaceFirst("\\[.*?]", StringUtils.EMPTY);
            Class<?> accessorType = getAccessorType(token);
            Class clazz = ReflectionUtils.extractGenerics(accessorType);
            if (accessorType != null) {
                if (ReflectionUtils.isCollectionImplementation(accessorType)) {
                    if (this.tobj == null) {
                        Class generics = ReflectionUtils.extractGenerics(accessorType);
                        this.tobj = (T) ReflectionUtils.createCollectionObject(generics);
                    }
                }
            }
            String setterName = getterOrSetterName.replaceFirst(Pattern.quote(tokens[0]) + "\\.", StringUtils.EMPTY);
            if (clazz == null) {
                return getAccessorType(setterName);
            } else {
                if (this.tobj == null) {
                    this.tobj = (T) accessorType.newInstance();
                }
                return getAccessorType(clazz, setterName);
            }
        } else {
            if (this.tobj == null) {
                this.tobj = getTclass().newInstance();
            }
            return getTopLevelAccesorType(getterOrSetterName, getters, setters, fields);
        }
    }

    private Class<?> getAccessorType(final Class clazz, final String getterOrSetterName) {
        final Map<String, Method> getters = new HashMap<>();
        final Map<String, Method> setters = new HashMap<>();
        final Map<String, Field> fields = new HashMap<>();
        fill(clazz, null, getters, setters, fields);
        final String[] tokens = getterOrSetterName.split("\\.");
        if (tokens.length > 1) {
            final String token = tokens[0];
            Class<?> accessorType = getAccessorType(clazz, token);
            if (ReflectionUtils.isCollectionImplementation(accessorType)) {
                return getAccessorType(ReflectionUtils.extractGenerics(accessorType),
                        getterOrSetterName.replaceFirst(Pattern.quote(token) + "\\.", StringUtils.EMPTY));
            } else {
                return getAccessorType(accessorType,
                        getterOrSetterName.replaceFirst(Pattern.quote(token) + "\\.", StringUtils.EMPTY));
            }
        } else {
            return getTopLevelAccesorType(getterOrSetterName, getters, setters, fields);
        }
    }

    public T getBean() {
        return tobj;
    }

    public Set<String> getGettersNames() {
        return Collections.unmodifiableSet(getters.keySet());
    }

    public Field getLastTokenField() {
        return this.lastTokenField;
    }

    public Set<String> getSettersNames() {
        return Collections.unmodifiableSet(setters.keySet());
    }

    public Class<T> getTclass() {
        return tclass;
    }

    private Class<?> getTopLevelAccesorType(final String getterOrSetterName,
                                            final Map<String, Method> getters,
                                            final Map<String, Method> setters,
                                            final Map<String, Field> fields) {

        String[] splittedGetterOrSetterName = getterOrSetterName.split("<");
        String property = splittedGetterOrSetterName[0].replaceFirst("\\[.*?]", StringUtils.EMPTY);
        if (splittedGetterOrSetterName.length > 1) {
            String[] genericTokens = splittedGetterOrSetterName[1].split(">");
            final String generic = genericTokens[0];
            Class<?> clazz = getTopLevelAccesorType(property, getters, setters, fields);
            Reflections reflections = null;
            if (clazz != null) {
                reflections = new Reflections(clazz.getPackage().getName(), tclassloader);
            }
            Set<? extends Class<?>> classes = null;
            if (reflections != null) {
                classes = reflections.getSubTypesOf(clazz);
            }

            return IterableUtils.find(classes,
                    object -> object.getSimpleName().equals(StringUtils.capitalize(generic)));
        }

        Method m = getters.get(property);
        if (m == null) {
            m = setters.get(property);
        }
        if (m == null) {
            return null;
        }
        Class<?> returnType = m.getReturnType();

        if (ReflectionUtils.isCollectionImplementation(returnType)) {
            Type genericReturnType = m.getGenericReturnType();
            try {
                returnType = (Class) (((ParameterizedType) genericReturnType).getActualTypeArguments()[0]);
            } catch (Throwable ignored) {
                return ReflectionUtils.extractGenerics((Class) genericReturnType);
            }
        }

        this.lastTokenField = fields.get(property);
        return returnType;
    }
}
