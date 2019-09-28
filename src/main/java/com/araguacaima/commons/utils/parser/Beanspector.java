package com.araguacaima.commons.utils.parser;

/*
  Created by Alejandro on 20/11/2014.
 */

import com.araguacaima.commons.utils.EnumsUtils;
import com.araguacaima.commons.utils.ReflectionUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.utils.InjectionUtils;
import org.reflections.Reflections;

import javax.xml.datatype.DatatypeFactory;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Bean introspection utility.
 */
class Beanspector<T> {

    private static final ReflectionUtils reflectionUtils = new ReflectionUtils(null);
    private static final EnumsUtils enumsUtils = new EnumsUtils();
    private final Map<String, Method> getters = new HashMap<>();
    private final Map<String, Method> setters = new HashMap<>();
    private final ClassLoader tclassloader;
    private Class<T> tclass;
    private T tobj;
    private String packageBase;

    public Beanspector(final Class<T> tclass, String packageBase) {
        this(tclass, tclass.getClassLoader(), packageBase);
    }

    public Beanspector(final T tobj, String packageBase) {
        this(tobj, tobj.getClass().getClassLoader(), packageBase);
    }

    public Beanspector(final T tobj, ClassLoader classLoader, String packageBase) {
        if (tobj == null) {
            throw new IllegalArgumentException("tobj is null");
        }
        this.tobj = tobj;
        this.tclassloader = classLoader;
        this.packageBase = packageBase;
        init();
    }

    public Beanspector(Class<T> tclass, ClassLoader classLoader, String packageBase) {
        if (tclass == null) {
            throw new IllegalArgumentException("tclass is null");
        }
        this.tclass = tclass;
        this.tclassloader = classLoader;
        this.packageBase = packageBase;
        init();
    }

    private void init() {
        fill(tclass, tobj, getters, setters);
    }

    private void fill(Class tclass, final Object tobj, final Map<String, Method> getters, final Map<String, Method> setters) {
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
                            "Accessor '%s' type mismatch, getter type is %s while setter type is %s", accessor, getterClass.getName(),
                            setterClass.getName()));
                }
            }
        } else {
            throw new IllegalArgumentException("Class and Object can not both be null");
        }
    }

    public T getBean() {
        return tobj;
    }

    public Set<String> getGettersNames() {
        return Collections.unmodifiableSet(getters.keySet());
    }

    public Set<String> getSettersNames() {
        return Collections.unmodifiableSet(setters.keySet());
    }

    public Class<?> getAccessorType(final String getterOrSetterName) throws Exception {
        final String[] tokens = getterOrSetterName.split("\\.");
        if (tokens.length > 1) {
            final String token = tokens[0];
            Class clazz = ReflectionUtils.extractGenerics(getAccessorType(token));
            return getAccessorType(clazz, getterOrSetterName.replaceFirst(token + "\\.", StringUtils.EMPTY));
        } else {
            return getTopLevelAccesorType(getterOrSetterName, getters, setters);
        }
    }

    private Class<?> getAccessorType(final Class clazz, final String getterOrSetterName) throws Exception {
        final Map<String, Method> getters = new HashMap<>();
        final Map<String, Method> setters = new HashMap<>();
        fill(clazz, null, getters, setters);
        final String[] tokens = getterOrSetterName.split("\\.");
        if (tokens.length > 1) {
            final String token = tokens[0];
            Class<?> accessorType = getAccessorType(clazz, token);
            if (ReflectionUtils.isCollectionImplementation(accessorType)) {
                return getAccessorType(ReflectionUtils.extractGenerics(accessorType), getterOrSetterName.replaceFirst(token + "\\.", StringUtils.EMPTY));
            } else {
                return getAccessorType(accessorType, getterOrSetterName.replaceFirst(token + "\\.", StringUtils.EMPTY));
            }
        } else {
            return getTopLevelAccesorType(getterOrSetterName, getters, setters);
        }
    }

    private Class<?> getTopLevelAccesorType(final String getterOrSetterName, final Map<String, Method> getters,
                                            final Map<String, Method> setters) throws IntrospectionException {

        String[] splittedGetterOrSetterName = getterOrSetterName.split("<");
        String property = splittedGetterOrSetterName[0];
        if (splittedGetterOrSetterName.length > 1) {
            String[] genericTokens = splittedGetterOrSetterName[1].split(">");
            final String generic = genericTokens[0];
            Class<?> clazz = getTopLevelAccesorType(property, getters, setters);
            Reflections reflections = new Reflections(clazz.getPackage().getName(), tclassloader);
            Set<? extends Class<?>> classes = reflections.getSubTypesOf(clazz);
            return (Class) IterableUtils.find(classes, (Predicate) object -> ((Class) object).getSimpleName().equals(StringUtils.capitalize(generic)));
        }

        Method m = getters.get(property);
        if (m == null) {
            m = setters.get(property);
        }
        if (m == null) {
            final String msg = String.format("Accessor '%s' not found, " + "known setters are: %s, known getters are: %s",
                    property, setters.keySet(), getters.keySet());
            throw new IntrospectionException(msg);
        }
        Class<?> returnType = m.getReturnType();

        if (ReflectionUtils.isCollectionImplementation(returnType)) {
            Type genericReturnType = m.getGenericReturnType();
            try {
                return (Class) (((ParameterizedType) genericReturnType).getActualTypeArguments()[0]);
            } catch (Throwable ignored) {
                assert genericReturnType instanceof Class;
                return ReflectionUtils.extractGenerics((Class) genericReturnType);
            }
        } else {
            return returnType;
        }
    }

    public Beanspector<T> swap(final T newobject) {
        if (newobject == null) {
            throw new IllegalArgumentException("newobject is null");
        }
        tobj = newobject;
        return this;
    }

    public Beanspector<T> instantiate(boolean resetObject) throws Exception {
        if (tobj == null) {
            tobj = tclass.newInstance();
        }
        return this;
    }

    public void setValue(String setterName, Object value) throws Throwable {
        Map<String, Object> fixedExpressionObject = instantiateNestedProperties(getBean(), setterName);
        setterName = fixedExpressionObject.keySet().iterator().next();
        Class type;
        if (value == null) {
            type = Object.class;
        } else {
            type = value.getClass();
        }
        if (ReflectionUtils.isCollectionImplementation(type)) {
            Object value1 = reflectionUtils.getValueFromCollectionImplementation(value);
            PropertyUtils.setProperty(getBean(), setterName.split("\\.")[0], value1);
        } else {
            PropertyUtils.setProperty(getBean(), setterName, value);
        }
    }

    public Object getValue(final String getterName) throws Throwable {
        return getValue(getters.get(getterName));
    }

    public Object getValue(final Method getter) throws Throwable {
        try {
            return getter.invoke(tobj);
        } catch (final InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private boolean isGetter(final Method m) {
        return m.getParameterTypes().length == 0 && (m.getName().startsWith("get") || m.getName().startsWith("is"));
    }

    private String getterName(final Method m) {
        return StringUtils.uncapitalize(m.getName().startsWith("is") ? m.getName().substring(2) : m.getName().startsWith("get") ? m
                .getName().substring(3) : m.getName());
    }

    private boolean isSetter(final Method m) {
        return m.getReturnType().equals(void.class) && m.getParameterTypes().length == 1
                && (m.getName().startsWith("set") || m.getName().startsWith("is"));
    }

    private String setterName(final Method m) {
        return StringUtils.uncapitalize(m.getName().replace("is", "").replace("set", ""));
    }

    private Map<String, Object> instantiateNestedProperties(final Object obj, String fieldName) {
        Map<String, Object> newObject = new HashMap<>();
        Object newInstance = new Object();
        Object nestedObject;
        try {
            final String[] fieldNames = fieldName.split("\\.");
            if (fieldNames.length > 1) {
                String consumedProperty;

                consumedProperty = fieldNames[0];
                String property = consumedProperty.replaceAll("<.*?>", "");
                String generic = null;
                try {
                    generic = consumedProperty.substring(consumedProperty.indexOf("<") + 1, consumedProperty.indexOf(">"));
                } catch (Throwable ignored) {
                }

                PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(obj, property);
                Class<?> originalPropertyType = propertyDescriptor.getPropertyType();
                Class<?> propertyType = getAccessorType(obj.getClass(), property);
                String expression = property;

                if (ReflectionUtils.isCollectionImplementation(originalPropertyType)) {
                    boolean anInterface = propertyType.isInterface();
                    if (anInterface) {

                        Reflections reflections = new Reflections(packageBase, tclassloader);
                        Set<? extends Class<?>> classes = reflections.getSubTypesOf(propertyType);
                        final String finalGeneric = generic;
                        Class<?> propertyType_ = (Class) IterableUtils.find(classes, (Predicate) object -> ((Class) object).getSimpleName().equals(StringUtils.capitalize(finalGeneric)));
                        if (propertyType_ != null) {
                            propertyType = propertyType_;
                            newInstance = propertyType_.newInstance();
                        } else {
                            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(propertyType).getPropertyDescriptors();
                            if (propertyDescriptors != null && propertyDescriptors.length > 0) {
                                List<Method> readMethods = new ArrayList<>();
                                List<Method> writeMethods = new ArrayList<>();
                                Map<String, Object> properties = new HashMap<>();
                                for (PropertyDescriptor pd : propertyDescriptors) {
                                    String attributeName = pd.getName();
                                    properties.put(attributeName, pd.getValue(attributeName));
                                    readMethods.add(pd.getReadMethod());
                                    writeMethods.add(pd.getWriteMethod());
                                }
                                InvocationHandler handler = new FiqlJsonSerializerInvocationHandler(readMethods, writeMethods, properties, propertyType);
                                newInstance = Proxy.newProxyInstance(propertyType.getClassLoader(),
                                        new Class[]{propertyType},
                                        handler);
                            } else {
                                InvocationHandler handler = new FiqlJsonSerializerInvocationHandler();
                                newInstance = Proxy.newProxyInstance(propertyType.getClassLoader(),
                                        new Class[]{propertyType},
                                        handler);
                            }
                        }
                    } else {
                        if (StringUtils.isNotBlank(generic) && !propertyType.getSimpleName().equals(StringUtils.capitalize(generic))) {
                            Reflections reflections = new Reflections(propertyType.getPackage().getName(), tclassloader);
                            Set<? extends Class<?>> classes = reflections.getSubTypesOf(propertyType);
                            final String finalGeneric = generic;
                            propertyType = (Class) IterableUtils.find(classes, (Predicate) object -> ((Class) object).getSimpleName().equals(StringUtils.capitalize(finalGeneric)));
                        }
                        newInstance = propertyType.newInstance();
                    }

                    Map<String, Object> objectMap = instantiateNestedProperties(newInstance,
                            fieldName.replaceFirst(consumedProperty + "\\.", StringUtils.EMPTY));
                    expression = objectMap.keySet().iterator().next();
                    newInstance = objectMap.values().iterator().next();

                    String newMethodName = fieldName.replaceFirst(consumedProperty + "\\.", StringUtils.EMPTY).split("\\.")[0];
                    if (propertyType.equals(newInstance.getClass())) {
                        newInstance = reflectionUtils.createAndInitializeCollection(originalPropertyType, newInstance);
                    } else {
                        newInstance = reflectionUtils.createAndInitializeTypedCollection(propertyType, newMethodName, newInstance);
                    }
                    expression = property + "[0]." + expression;
                } else if (originalPropertyType.isInterface()) {
                    if (StringUtils.isNotBlank(generic)) {
                        Class<?> propertyType_ = ReflectionUtils.extractGenerics(propertyType);
                        if (!propertyType_.getSimpleName().equals(StringUtils.capitalize(generic))) {
                            Reflections reflections = new Reflections(propertyType_.getPackage().getName(), tclassloader);
                            Set<? extends Class<?>> classes = reflections.getSubTypesOf(propertyType_);
                            final String finalGeneric = generic;
                            propertyType = (Class) IterableUtils.find(classes, (Predicate) object -> ((Class) object).getSimpleName().equals(StringUtils.capitalize(finalGeneric)));
                            Map<String, Object> objectMap = instantiateNestedProperties(propertyType.newInstance(),
                                    fieldName.replaceFirst(consumedProperty + "\\.", StringUtils.EMPTY));
                            expression = property + "." + objectMap.keySet().iterator().next();
                            newInstance = objectMap.values().iterator().next();
                        }
                    } else if (ReflectionUtils.isCollectionImplementation(propertyType)) {
                        Map<String, Object> objectMap = instantiateNestedProperties(propertyType.newInstance(),
                                fieldName.replaceFirst(consumedProperty + "\\.", StringUtils.EMPTY));
                        expression = objectMap.keySet().iterator().next();
                        nestedObject = objectMap.values().iterator().next();

                        newInstance = ReflectionUtils.createAndInitializeTypedCollection(propertyType, nestedObject);
                        expression = expression + "[0]";
                    } else {
                        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(propertyType).getPropertyDescriptors();
                        if (propertyDescriptors != null && propertyDescriptors.length > 0) {
                            List<Method> readMethods = new ArrayList<>();
                            List<Method> writeMethods = new ArrayList<>();
                            Map<String, Object> properties = new HashMap<>();
                            for (PropertyDescriptor pd : propertyDescriptors) {
                                String attributeName = pd.getName();
                                properties.put(attributeName, pd.getValue(attributeName));
                                readMethods.add(pd.getReadMethod());
                                writeMethods.add(pd.getWriteMethod());
                            }
                            InvocationHandler handler = new FiqlJsonSerializerInvocationHandler(readMethods, writeMethods, properties, propertyType);
                            newInstance = Proxy.newProxyInstance(propertyType.getClassLoader(),
                                    new Class[]{propertyType},
                                    handler);
                        } else {
                            InvocationHandler handler = new FiqlJsonSerializerInvocationHandler();
                            newInstance = Proxy.newProxyInstance(propertyType.getClassLoader(),
                                    new Class[]{propertyType},
                                    handler);
                        }
                        Map<String, Object> objectMap = instantiateNestedProperties(newInstance,
                                fieldName.replaceFirst(consumedProperty + "\\.", StringUtils.EMPTY));
                        expression = property + "." + objectMap.keySet().iterator().next();
                    }
                } else {
                    newInstance = propertyType.newInstance();
                    try {
                        Map<String, Object> objectMap = instantiateNestedProperties(newInstance,
                                fieldName.replaceFirst(consumedProperty + "\\.", StringUtils.EMPTY));
                        String s = objectMap.keySet().iterator().next();
                        expression = expression + "." + s;
                        nestedObject = objectMap.values().iterator().next();
                        PropertyUtils.setProperty(newInstance, s.replaceAll("[0]", StringUtils.EMPTY),
                                nestedObject);
                    } catch (Throwable ignored) {
                    }
                }
                PropertyUtils.setProperty(obj, property, newInstance);
                newObject.put(expression, newInstance);

            } else {
                Class<?> originalPropertyType = PropertyUtils.getPropertyDescriptor(obj, fieldName).getPropertyType();
                Class<?> propertyType = getAccessorType(obj.getClass(), fieldName);

                if (!ReflectionUtils.isCollectionImplementation(originalPropertyType)) {
                    try {
                        newInstance = propertyType.newInstance();
                    } catch (InstantiationException ignored) {
                        try {
                            newInstance = instantiatePrimitive(propertyType);
                        } catch (IllegalArgumentException e) {
                            if (propertyType.isEnum() || Enumeration.class.isAssignableFrom(propertyType)) {
                                newInstance = enumsUtils.getAnyEnumElement(propertyType);
                            } else {
                                throw e;
                            }
                        }
                    }
                    PropertyUtils.setProperty(obj, fieldName, newInstance);
                    newObject.put(fieldName, obj);
                } else {
                    newInstance = ReflectionUtils.createAndInitializeTypedCollection(propertyType, null);
                    PropertyUtils.setProperty(obj, fieldName, newInstance);
                    newObject.put(fieldName + "[0]", obj);
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return newObject;
    }

    private Object instantiatePrimitive(Class valueType) throws Exception {
        Object castedValue;
        String value = StringUtils.EMPTY;
        if (Date.class.isAssignableFrom(valueType)) {
            value = "0000-00-00'T'00:00:00.000Z";
            DateFormat df;
            try {
                df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                // zone in XML is "+01:00" in Java is "+0100"; stripping
                // semicolon
                final int idx = value.lastIndexOf(':');
                final String v = value.substring(0, idx) + value.substring(idx + 1);
                castedValue = df.parse(v);
            } catch (final ParseException e) {
                // is that duration?

                final Date now = new Date();
                DatatypeFactory.newInstance().newDuration(value).addTo(now);
                castedValue = now;

            }
        } else if (BigDecimal.class.isAssignableFrom(valueType)) {
            value = "0";
            castedValue = new BigDecimal(value);
        } else if (valueType.isEnum() || Enumeration.class.isAssignableFrom(valueType)) {
            String valueEnum = value;
            castedValue = enumsUtils.getEnum(valueType, valueEnum);
        } else {
            try {
                castedValue = InjectionUtils.convertStringToPrimitive(value, valueType);
            } catch (Throwable ignored) {
                castedValue = InjectionUtils.convertStringToPrimitive("0", valueType);
            }
        }
        return castedValue;
    }
}
