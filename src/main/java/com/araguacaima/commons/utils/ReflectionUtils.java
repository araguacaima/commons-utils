/*
 * Copyright 2017 araguacaima
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.araguacaima.commons.utils;

import com.google.common.collect.ObjectArrays;
import io.github.benas.randombeans.EnhancedRandomBuilder;
import io.github.benas.randombeans.api.EnhancedRandom;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.builder.StandardToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.ConstructorAccessor;
import sun.reflect.FieldAccessor;
import sun.reflect.ReflectionFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@SuppressWarnings({"unchecked", "UnusedReturnValue"})

public class ReflectionUtils implements Serializable {

    public static final List BASIC_CLASSES = Arrays.asList(String.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class);
    public static final Transformer CLASS_FROM_OBJECT_TRANSFORMER = Object::getClass;
    public static final Transformer<Class, String> CLASS_NAME_TRANSFORMER = Class::getName;
    public static final Collection<String> COMMONS_JAVA_TYPES_EXCLUSIONS = new ArrayList<String>() {
        {
            add("java.util.Currency");
            add("java.util.Calendar");
            add("org.joda.time.Period");
        }
    };
    public static final Collection<String> COMMONS_TYPES_PREFIXES = new ArrayList<String>() {
        {
            add("java.lang");
            add("java.util");
            add("java.math");
            add("java.io");
            add("java.sql");
            add("java.text");
            add("java.net");
            add("org.joda.time");
        }
    };
    public static final Transformer<Field, String> FIELD_NAME_TRANSFORMER = Field::getName;
    public static final Predicate<Method> METHOD_IS_GETTER_PREDICATE = method -> method.getName().matches
            ("get[A-Z]+.*") || method.getName().matches(
            "is[A-Z]+.*");
    public static final Predicate<Method> METHOD_IS_SETTER_PREDICATE = method -> method.getName().matches("set[A-Z]+.*");
    public static final Transformer<Method, String> METHOD_NAME_TRANSFORMER = Method::getName;
    public static final Map PRIMITIVE_AND_BASIC_TYPES = new HashMap();
    public static final Map PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES = new HashMap();
    public static final Map<String, Class> PRIMITIVE_NAMES_BASIC_CLASS = new HashMap<>();
    public static final Map<String, Class> PRIMITIVE_NAMES_BASIC_TYPES = new HashMap<>();
    public static final List PRIMITIVE_TYPES = Arrays.asList(Boolean.TYPE,
            Character.TYPE,
            Byte.TYPE,
            Short.TYPE,
            Integer.TYPE,
            Long.TYPE,
            Float.TYPE,
            Double.TYPE,
            Void.TYPE);
    public static final String UNKNOWN_VALUE = "UNKNOWN_VALUE";
    private static final Collection<Class> COMMONS_COLLECTIONS_IMPLEMENTATIONS = new ArrayList<Class>() {
        {
            add(ArrayList.class);
            add(TreeSet.class);
            add(HashSet.class);
            add(LinkedHashSet.class);
            add(LinkedList.class);
        }
    };
    private static final FieldCompare FIELD_COMPARE = new FieldCompare();
    private static final DataTypesConverter dataTypesConverter = new DataTypesConverter();
    private static final Logger log = LoggerFactory.getLogger(ReflectionUtils.class);
    private static final EnhancedRandomBuilder randomBuilder;
    private static final ReflectionUtils INSTANCE = new ReflectionUtils();

    static {
        final StandardToStringStyle tiesStyle = new StandardToStringStyle();
        tiesStyle.setArrayContentDetail(true);
        //Sets whether to output array content detail.
        tiesStyle.setArrayEnd("]");
        //Sets the array end text.
        tiesStyle.setArraySeparator(",");
        //Sets the array separator text.
        tiesStyle.setArrayStart("[");
        //Sets the array start text.
        tiesStyle.setContentEnd("\n");
        //Sets the content end text.
        tiesStyle.setContentStart("\n");
        //Sets the content start text.
        tiesStyle.setDefaultFullDetail(true);
        //Sets whether to use full detail when the caller doesn't specify.
        tiesStyle.setFieldNameValueSeparator(" = ");
        //Sets the field name value separator text.
        tiesStyle.setFieldSeparator("\n");
        //Sets the field separator text.
        tiesStyle.setFieldSeparatorAtEnd(false);
        //Sets whether the field separator should be added at the end of each buffer.
        tiesStyle.setFieldSeparatorAtStart(false);
        //Sets whether the field separator should be added at the start of each buffer.
        tiesStyle.setNullText("null");
        //Sets the text to output when null found.
        tiesStyle.setUseClassName(true);
        //Sets whether to use the class name.
        tiesStyle.setUseFieldNames(true);
        //Sets whether to use the field names passed in.
        tiesStyle.setUseIdentityHashCode(false);
        //Sets whether to use the identity hash code.
        tiesStyle.setUseShortClassName(true);
        //Sets whether to output short or long class names.
        tiesStyle.setArrayContentDetail(true);
        //Sets whether to output array content detail.
        tiesStyle.setDefaultFullDetail(true);
        //Sets whether to use full detail when the caller doesn't specify.

        ToStringBuilder.setDefaultStyle(tiesStyle);

        PRIMITIVE_AND_BASIC_TYPES.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE_AND_BASIC_TYPES.put(Character.TYPE, Character.class);
        PRIMITIVE_AND_BASIC_TYPES.put(Byte.TYPE, Byte.class);
        PRIMITIVE_AND_BASIC_TYPES.put(Short.TYPE, Short.class);
        PRIMITIVE_AND_BASIC_TYPES.put(Integer.TYPE, Integer.class);
        PRIMITIVE_AND_BASIC_TYPES.put(Long.TYPE, Long.class);
        PRIMITIVE_AND_BASIC_TYPES.put(Float.TYPE, Float.class);
        PRIMITIVE_AND_BASIC_TYPES.put(Double.TYPE, Double.class);
        PRIMITIVE_NAMES_BASIC_TYPES.put("int", Integer.TYPE);
        PRIMITIVE_NAMES_BASIC_TYPES.put("long", Long.TYPE);
        PRIMITIVE_NAMES_BASIC_TYPES.put("double", Double.TYPE);
        PRIMITIVE_NAMES_BASIC_TYPES.put("float", Float.TYPE);
        PRIMITIVE_NAMES_BASIC_TYPES.put("boolean", Boolean.TYPE);
        PRIMITIVE_NAMES_BASIC_TYPES.put("char", Character.TYPE);
        PRIMITIVE_NAMES_BASIC_TYPES.put("byte", Byte.TYPE);
        PRIMITIVE_NAMES_BASIC_TYPES.put("short", Short.TYPE);
        PRIMITIVE_NAMES_BASIC_CLASS.put("int", Integer.class);
        PRIMITIVE_NAMES_BASIC_CLASS.put("long", Long.class);
        PRIMITIVE_NAMES_BASIC_CLASS.put("double", Double.class);
        PRIMITIVE_NAMES_BASIC_CLASS.put("float", Float.class);
        PRIMITIVE_NAMES_BASIC_CLASS.put("boolean", Boolean.class);
        PRIMITIVE_NAMES_BASIC_CLASS.put("char", Character.class);
        PRIMITIVE_NAMES_BASIC_CLASS.put("byte", Byte.class);
        PRIMITIVE_NAMES_BASIC_CLASS.put("short", Short.class);

        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Boolean.TYPE, Boolean.FALSE);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Character.TYPE, ' ');
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Byte.TYPE, (byte) -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Short.TYPE, (short) -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Integer.TYPE, -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Long.TYPE, (long) -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Float.TYPE, (float) -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Double.TYPE, (double) -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Boolean.class, Boolean.FALSE);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Character.class, ' ');
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Byte.class, (byte) -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Short.class, (short) -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Integer.class, -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Long.class, (long) -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Float.class, (float) -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(Double.class, (double) -1);
        PRIMITIVE_AND_BASIC_TYPE_DEFAULT_VALUES.put(String.class, StringUtils.EMPTY);

        LocalTime timeLower = LocalTime.of(0, 0);
        LocalTime timeUpper = LocalTime.of(0, 0);
        LocalDate dateLower = LocalDate.of(2000, 1, 1);
        LocalDate dateUpper = LocalDate.of(2040, 12, 31);

        randomBuilder = EnhancedRandomBuilder.aNewEnhancedRandomBuilder()
                .seed(123L)
                .objectPoolSize(100)
                .charset(StandardCharsets.UTF_8)
                .timeRange(timeLower, timeUpper)
                .dateRange(dateLower, dateUpper)
                .stringLengthRange(5, 20)
                .collectionSizeRange(1, 5)
                .scanClasspathForConcreteTypes(true)
                .overrideDefaultInitialization(true)
                .objectPoolSize(3);
    }

    private final ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
    private StringUtils stringUtils;
    ;

    private ReflectionUtils() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static ReflectionUtils getInstance() {
        return INSTANCE;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cannot clone instance of this class");
    }

    public Object createCollectionObject(Class<?> clazz)
            throws IllegalAccessException, InstantiationException {
        Class generics = extractGenerics(clazz);
        Object bean = generics.newInstance();

        Object result;
        if (Collection.class.isAssignableFrom(clazz)) {
            Type genericSuperclass = clazz.getGenericSuperclass();
            if (genericSuperclass == null) {
                return new ArrayList();
            } else {
                result = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
            }
            try {
                ((Collection) result).add(bean);
            } catch (Exception ignored) {
                try {
                    return clazz.newInstance();
                } catch (Throwable ignored_) {
                    return new ArrayList();
                }
            }
        } else
            result = getObject_(clazz, bean, generics);
        return result;
    }

    public Class extractGenerics(Class clazz) {
        if (clazz == null) {
            return null;
        }
        if (!isCollectionImplementation(clazz)) {
            return clazz;
        }
        Class generics;
        if (Collection.class.isAssignableFrom(clazz)) {
            Type genericSuperclass = clazz.getGenericSuperclass();
            if (genericSuperclass == null) {
                generics = Object.class;
            } else {
                try {
                    Object type = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
                    generics = (Class) type;
                } catch (ClassCastException ignored) {
                    try {
                        generics = (Class) clazz.getGenericInterfaces()[0];
                    } catch (Throwable t) {
                        generics = Object.class;
                    }
                }
            }
        } else
            generics = getClass(clazz);

        return generics;

    }

    private Object getObject_(Class<?> clazz, Object value, Class<?> generics) {
        Object result = null;
        if (Object[].class.isAssignableFrom(clazz) || clazz.isArray()) {
            try {
                result = ObjectArrays.newArray(generics, 0);
                result = ObjectArrays.concat((Object[]) result, value);
            } catch (Exception ignored) {
            }
        } else {
            throw new IllegalArgumentException("Invalid Type. Incoming type must be a collection implementation");
        }
        return result;
    }

    public boolean isCollectionImplementation(String className) {
        Class collectionClass;
        try {
            String firstPartType = StringUtils.defaultIfBlank(returnNativeClass(className), className.split("<")[0]);
            collectionClass = Class.forName(firstPartType);
        } catch (ClassNotFoundException e) {
            try {
                collectionClass = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                return false;
            }
        }
        return isCollectionImplementation(collectionClass);
    }

    public boolean isCollectionImplementation(Class clazz) {
        return clazz != null && (Collection.class.isAssignableFrom(clazz) || Object[].class.isAssignableFrom(clazz)
                || clazz.isArray());
    }

    private Class getClass(Class clazz) {
        Class generics = null;
        if (Object[].class.isAssignableFrom(clazz)) {
            try {
                generics = Class.forName(clazz.toString().replaceFirst("class \\[L", StringUtils.EMPTY).replace(";",
                        StringUtils.EMPTY));
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage());
            }

        } else if (clazz.isArray()) {
            try {
                Method method = Class.class.getDeclaredMethod("getPrimitiveClass", String.class);
                method.setAccessible(true);
                generics = (Class) method.invoke(Class.forName("java.lang.Class"),
                        clazz.getSimpleName().replace("[]", StringUtils.EMPTY));
            } catch (InvocationTargetException | ClassNotFoundException | NoSuchMethodException |
                    IllegalAccessException | NullPointerException ignored) {
            }
        }
        return generics;
    }

    public Class extractGenerics(Field field) {
        Class clazz;
        final Type genericType = field.getGenericType();
        Type[] typeArguments = null;
        if (genericType instanceof Class) {
            clazz = (Class) genericType;
        } else {
            clazz = getType(genericType);
        }
        if (clazz == null) {
            clazz = field.getType();
        }
        return extractGenerics(clazz);
    }

    private Class getType(Type genericType) {
        Type[] typeArguments = null;
        Class clazz = null;
        try {
            Field rawTypeField = genericType.getClass().getDeclaredField("rawType");
            rawTypeField.setAccessible(true);
            Class clazz_ = (Class) rawTypeField.get(genericType);
            typeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
            if (isMapImplementation(clazz_)) {
                clazz = (Class) typeArguments[1];
            } else {
                clazz = (Class) typeArguments[0];
            }
        } catch (ClassCastException ignored) {
            Type type = Objects.requireNonNull(typeArguments)[0];
            try {
                Field rawTypeField = type.getClass().getDeclaredField("rawType");
                rawTypeField.setAccessible(true);
                clazz = (Class) rawTypeField.get(type);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException | NullPointerException | IndexOutOfBoundsException ignored) {
            typeArguments = ((TypeVariable) genericType).getBounds();
            clazz = (Class) typeArguments[0];
        } catch (Throwable ignored) {

        }
        return clazz;
    }

    private boolean fieldIsNotContainedIn(Field field, Collection<String> excludeFields) {
        return !fieldIsContainedIn(field, excludeFields);
    }

    private boolean fieldIsContainedIn(Field field, Collection<String> excludeFields) {
        return IterableUtils.find(excludeFields, fieldName -> fieldNameEqualsToPredicate(field, fieldName)) != null;
    }

    private boolean fieldNameEqualsToPredicate(Field field, String fieldName) {
        return !StringUtils.isBlank(fieldName) && field.getName().equals(fieldName);
    }

    private boolean fieldNameIsNotContainedIn(String fieldName, Collection<String> excludeFields) {
        return !fieldNameIsContainedIn(fieldName, excludeFields);
    }

    private boolean fieldNameIsContainedIn(String fieldName, Collection<String> excludeFields) {
        return excludeFields.contains(fieldName);
    }

    public Collection<String> getAllFieldNamesIncludingParents(Class clazz) {
        return CollectionUtils.collect(getAllFieldsIncludingParents(clazz,
                null,
                Modifier.VOLATILE | Modifier.NATIVE | Modifier.TRANSIENT), FIELD_NAME_TRANSFORMER);
    }

    public Collection<Field> getAllFieldsIncludingParents(Class clazz,
                                                          final Integer modifiersInclusion,
                                                          final Integer modifiersExclusion) {
        final Collection<Field> fields = new ArrayList<>();
        FieldFilter fieldFilterModifierInclusion = field -> modifiersInclusion == null || (field.getModifiers() &
                modifiersInclusion) != 0;
        FieldFilter fieldFilterModifierExclusion = field -> modifiersExclusion == null || (field.getModifiers() &
                modifiersExclusion) == 0;
        doWithFields(clazz,
                fields::add,
                modifiersInclusion == null ? (modifiersExclusion == null ? null : fieldFilterModifierExclusion) :
                        fieldFilterModifierInclusion);
        return fields;
    }

    public Collection<String> getAllFieldsNamesOfType(Object object, final Class type) {
        return getAllFieldsNamesOfType(object.getClass(), null, type);
    }

    public Collection<String> getAllFieldsNamesOfType(Class clazz,
                                                      Collection<String> excludeFields,
                                                      final Class type) {
        return CollectionUtils.collect(getAllFieldsOfType(clazz, excludeFields, type), FIELD_NAME_TRANSFORMER);
    }

    public Collection<Field> getAllFieldsOfType(Class clazz,
                                                Collection<String> excludeFields,
                                                final Class type) {
        final Collection<Field> result = new ArrayList();
        if (clazz != null) {
            IterableUtils.forEach(getAllFieldsIncludingParents(clazz, excludeFields), field -> {
                if (field.getType().getName().equals(type.getName())) {
                    result.add(field);
                }
            });
        }
        return result;
    }

    public Collection<Field> getAllFieldsIncludingParents(Class clazz, Collection<String> excludeFields) {
        Collection<Field> result = getAllFieldsIncludingParents(clazz);
        if (CollectionUtils.isEmpty(excludeFields)) {
            return result;
        }
        CollectionUtils.filterInverse(result, field -> fieldIsContainedIn(field, excludeFields));
        return result;
    }

    public Collection<Field> getAllFieldsIncludingParents(Class clazz) {
        return getAllFieldsIncludingParents(clazz,
                null,
                Modifier.STATIC | Modifier.VOLATILE | Modifier.NATIVE | Modifier.TRANSIENT);
    }

    public String getExtractedGenerics(String s) {
        String s1 = s.trim();
        try {
            String firstPartType = s1.split("<")[1];
            if (firstPartType.endsWith(">")) {
                return firstPartType.substring(0, firstPartType.length() - 1);
            }
        } catch (Throwable ignored) {
        }
        return s1;
    }

    /**
     * @param object The object to obtain the class name
     * @return The class name
     * @deprecated
     */
    public String getSimpleClassName(Object object) {
        return getSimpleClassName(object.getClass());
    }

    public String getSimpleClassName(Class clazz) {
        String className = null;
        if (clazz != null) {
            className = clazz.getName();
            try {
                className = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
                className = className.substring(className.lastIndexOf("$") + 1);
                className = className.replaceAll(";", "[]");
            } catch (Exception e) {
                log.error("Impossible to get the simple name for the class: " + clazz.getName() + ". It'll be taken: " +
                        "" + "" + className);
            }
        }
        return className;
    }

    public boolean isList(String type) {
        try {
            boolean result = type.equals("List") || type.startsWith("List<") || type.startsWith("java.util.List<") || type.equals(
                    "Collection") || type.startsWith("Collection<") || type.startsWith("java.util.Collection<");
            if (!result) {
                String firstPartType = StringUtils.defaultIfBlank(returnNativeClass(type), type.split("<")[0]);
                result = Class.forName(firstPartType) != null;
            }
            return result;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public Collection<Object> createAndInitializeTypedCollection(Class<?> typedClassForCollection, Object
            value)
            throws IllegalAccessException, InstantiationException {
        Collection<Object> result = new ArrayList<>();
        if (value == null) {
            Object bean = typedClassForCollection.newInstance();
            result.add(bean);
        } else {
            result.add(value);
        }
        return result;
    }

    public boolean isMapImplementation(Class clazz) {
        return clazz != null && Map.class.isAssignableFrom(clazz);
    }

    public <E, F> F deepClone(E e) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo;
        try {
            oo = new ObjectOutputStream(bo);
            oo.writeObject(e);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi;
        try {
            oi = new ObjectInputStream(bi);
            return (F) (oi.readObject());
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public CtMethod generateGetter(CtClass declaringClass, String fieldName, CtClass fieldClass)
            throws CannotCompileException {

        Class clazz = fieldClass.getClass();
        String prefix = (clazz.getSimpleName().equals("Boolean") || clazz.getTypeName().equals("boolean")) ? "is" :
                "get";
        String getterName = prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String sb = "public " + fieldClass.getName() + " " + getterName + "(){" +
                "return this." + fieldName + ";" + "}";
        return CtMethod.make(sb, declaringClass);
    }

    public CtMethod generateSetter(CtClass declaringClass, String fieldName, CtClass fieldClass)
            throws CannotCompileException {

        String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String sb = "public void " + setterName + "(" + fieldClass.getName() + " " +
                fieldName + ")" + "{" + "this." + fieldName + "=" + fieldName +
                ";" + "}";
        return CtMethod.make(sb, declaringClass);
    }

    public String returnNativeClass(String type) {
        Class clazz;
        type = type.split("<")[0];
        for (String javaTypePrefix : COMMONS_TYPES_PREFIXES) {
            try {
                clazz = Class.forName(type.contains(".") ? type : javaTypePrefix + "." + type);
                return clazz.getName();
            } catch (ClassNotFoundException ignored) {

            }
        }
        return null;
    }

    public String getFullyQualifiedJavaTypeOrNull(String type, boolean considerLists) {
        if (type == null) {
            return null;
        }
        DataTypesConverter.DataTypeView dataTypeView = dataTypesConverter.getDataTypeView(type);
        type = dataTypeView.getTransformedDataType();
        String transformedType;
        boolean complexType = DataTypesConverter.DataTypeView.COMPLEX_TYPE.equals(dataTypeView.getDataType());
        if (complexType) {
            transformedType = type;
        } else {
            transformedType = StringUtils.capitalize(type);
        }
        Class clazz;
        if (considerLists) {
            if (isList(type)) {
                String generics = getExtractedGenerics(type);
                final String javaType = getFullyQualifiedJavaTypeOrNull(generics, false);
                if (StringUtils.isBlank(javaType)) {
                    return null;
                } else {
                    return "java.util.List<" + javaType + ">";
                }
            }
        }
        if (!complexType) {
            for (String javaTypePrefix : COMMONS_TYPES_PREFIXES) {
                try {
                    clazz = Class.forName(transformedType.contains(".") ? transformedType : javaTypePrefix + "." +
                            transformedType);
                    if (considerLists) {
                        if (isList(type)) {
                            return "java.util.List<" + clazz.getName() + ">";
                        }
                    }
                    if (!COMMONS_JAVA_TYPES_EXCLUSIONS.contains(clazz.getName())) {
                        return clazz.getName();
                    }
                } catch (ClassNotFoundException ignored) {

                }
            }
        }
        try {
            Method method = Class.class.getDeclaredMethod("getPrimitiveClass", String.class);
            method.setAccessible(true);
            clazz = (Class) method.invoke(Class.forName("java.lang.Class"), StringUtils.uncapitalize(type));
            if (considerLists) {
                if (isList(type)) {
                    return "java.util.List<" + clazz.getName() + ">";
                }
            }
            return clazz.getName();
        } catch (InvocationTargetException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                | NullPointerException ignored) {
        }
        return null;
    }

    public String getSimpleJavaTypeOrNull(Class type) {
        return getSimpleJavaTypeOrNull(type, true);
    }

    public String getSimpleJavaTypeOrNull(Class type, boolean considerLists) {
        return getSimpleJavaTypeOrNull(type.getSimpleName(), considerLists);
    }

    public String getSimpleJavaTypeOrNull(String type) {
        return getSimpleJavaTypeOrNull(type, true);
    }

    public String getSimpleJavaTypeOrNull(String type, boolean considerLists) {
        String fullyQualifedJavaType = getFullyQualifiedJavaTypeOrNull(type, considerLists);
        if (StringUtils.isNotBlank(fullyQualifedJavaType)) {
            try {
                return Class.forName(fullyQualifedJavaType).getSimpleName();
            } catch (ClassNotFoundException ignored) {

            }
        }
        return null;
    }

    /**
     * Add an enum instance to the enum class given as argument
     *
     * @param <T>         the type of the enum (implicit)
     * @param type        the class of the enum to be modified
     * @param name        the name of the new enum instance to be added to the class.
     * @param value       the value for the name of the new enum instance.
     * @param description the full description of the new enum instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends Enum<?>> void addEnum(Class<T> type, String name, String value, String description) {

        // 0. Sanity checks
        if (!Enum.class.isAssignableFrom(type)) {
            throw new RuntimeException("class " + type + " is not an instance of Enum");
        }

        // 1. Lookup "$VALUES" holder in enum class and get previous enum instances
        Field valuesField = null;
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals("enumConstants")) {
                valuesField = field;
                AccessibleObject.setAccessible(new Field[]{valuesField}, true);
                break;
            }
        }

        try {
            // 2. Copy it
            T[] previousValues;
            List<T> values = new ArrayList<>();
            if (valuesField != null) {
                previousValues = (T[]) valuesField.get(type);
                if (previousValues != null) {
                    values = new ArrayList<>(Arrays.asList(previousValues));
                }
            }
            // 3. build new enum
            T newValue; // could be used to pass values to the enum constuctor if needed

            newValue = (T) makeEnum(type, // The target enum class
                    name, value, description,// THE NEW ENUM INSTANCE TO BE DYNAMICALLY ADDED
                    values.size(), new Class<?>[]{}, // could be used to pass values to the enum constuctor if needed
                    new Object[]{});

            // 4. add new value
            values.add(newValue);

            // 5. Set new values field
            setFailsafeFieldValue(Objects.requireNonNull(valuesField), type, values.toArray((T[]) Array.newInstance(type, 0)));

            // 6. Clean enum cache
            //            cleanEnumCache(type);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Object makeEnum(Class<?> enumClass,
                            String name,
                            String value,
                            String description,
                            int ordinal,
                            Class<?>[] additionalTypes,
                            Object[] additionalValues)
            throws Exception {
        Object[] parms = new Object[additionalValues.length + 2];
        parms[0] = name;
        parms[1] = ordinal;
        System.arraycopy(additionalValues, 0, parms, 2, additionalValues.length);
        Object o = enumClass.cast(getConstructorAccessor(enumClass, additionalTypes).newInstance(parms));
        try {
            Method mValue = o.getClass().getMethod("setValue", String.class);
            mValue.setAccessible(true);
            mValue.invoke(o, value);
            Method mDescription = o.getClass().getMethod("setDescription", String.class);
            mDescription.setAccessible(true);
            mDescription.invoke(o, description);
        } catch (Throwable ignored) {
        }
        return o;
    }

    private void setFailsafeFieldValue(Field field, Class<?> target, Object value)
            throws NoSuchFieldException, IllegalAccessException {

        // let's make the field accessible
        field.setAccessible(true);

        // next we change the modifier in the Field instance to
        // not be final anymore, thus tricking reflection into
        // letting us modify the  final field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        int modifiers = modifiersField.getInt(field);

        // blank out the final bit in the modifiers int
        modifiers &= ~Modifier.FINAL;
        modifiersField.setInt(field, modifiers);

        FieldAccessor fa = reflectionFactory.newFieldAccessor(field, false);
        fa.set(target, value);
    }

    private ConstructorAccessor getConstructorAccessor(Class<?> enumClass, Class<?>[] additionalParameterTypes)
            throws NoSuchMethodException {
        Class<?>[] parameterTypes = new Class[additionalParameterTypes.length + 2];
        parameterTypes[0] = String.class;
        parameterTypes[1] = int.class;
        System.arraycopy(additionalParameterTypes, 0, parameterTypes, 2, additionalParameterTypes.length);
        return reflectionFactory.newConstructorAccessor(enumClass.getDeclaredConstructor(parameterTypes));
    }

    public boolean allFieldsAreNotEmptyOrNull(Object object) {
        String fieldName;
        if (object != null) {
            for (String s : getFieldNames(object.getClass())) {
                fieldName = s;
                Object value = invokeGetter(object, fieldName);
                if (isNullOrEmpty(value)) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }

    }

    public Map buildParametersAndAttributesMapFromRequest(HttpServletRequest request) {
        Map requestMap = new HashMap();
        requestMap.putAll(buildParametersMapFromRequest(request));
        requestMap.putAll(buildAttributesMapFromRequest(request));
        return requestMap;
    }

    public Map buildParametersMapFromRequest(HttpServletRequest request) {
        return request.getParameterMap();
    }

    public Map buildAttributesMapFromRequest(HttpServletRequest request) {
        String key;
        Object value;
        Map requestMap = new HashMap();
        for (Enumeration attributeNames = request.getAttributeNames(); attributeNames.hasMoreElements(); ) {
            key = (String) attributeNames.nextElement();
            value = request.getAttribute(key);
            requestMap.put(key, value);
        }
        return requestMap;
    }

    @SuppressWarnings({"unchecked"})
    public Object changeAnnotationValue(Annotation annotation, String key, Object newValue) {
        InvocationHandler handler = Proxy.getInvocationHandler(annotation);
        Field f;
        try {
            f = handler.getClass().getDeclaredField("memberValues");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        f.setAccessible(true);
        Map<String, Object> memberValues;
        try {
            memberValues = (Map<String, Object>) f.get(handler);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        Object oldValue = memberValues.get(key);
        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }
        memberValues.put(key, newValue);
        return oldValue;
    }

    public boolean checkWhetherOrNotSuperclassesExtendsCriteria(Class clazz, Class superClassCriteria) {
        boolean result;
        if (clazz != null && clazz != Object.class) {
            String clazzName = clazz.getName();
            result = clazzName.equals(superClassCriteria.getName());
            if (!result) {
                Class superClass = clazz.getSuperclass();
                return checkWhetherOrNotSuperclassesExtendsCriteria(superClass, superClassCriteria);
            }
            return true;
        }
        return false;
    }

    public boolean checkWhetherOrNotSuperclassesImplementsCriteria(final Class clazz,
                                                                   final Class interfaceCriteria) {
        boolean result;
        if (clazz != null && clazz != Object.class) {
            Collection<Class> incomingInterfaces = Arrays.asList(clazz.getInterfaces());
            if (incomingInterfaces.isEmpty()) {
                Class superClass = clazz.getSuperclass();
                return checkWhetherOrNotSuperclassesImplementsCriteria(superClass, interfaceCriteria);
            }
            result = IterableUtils.find(incomingInterfaces,
                    interface_ -> interface_.getName().equals(interfaceCriteria.getName())) != null;
            if (!result) {
                Class superClass = clazz.getSuperclass();
                return checkWhetherOrNotSuperclassesImplementsCriteria(superClass, interfaceCriteria);
            }
            return true;
        }
        return false;
    }

    private boolean classNameEqualsToPredicate(Class clazz, String className) {
        return StringUtils.isNotBlank(className) && clazz.getName().equals(className);
    }

    private void cleanEnumCache(Class<?> enumClass)
            throws NoSuchFieldException, IllegalAccessException {
        blankField(enumClass, "enumConstantDirectory"); // Sun (Oracle?!?) JDK 1.5/6
        blankField(enumClass, "enumConstants"); // IBM JDK
    }

    private void blankField(Class<?> enumClass, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        for (Field field : Class.class.getDeclaredFields()) {
            if (field.getName().contains(fieldName)) {
                AccessibleObject.setAccessible(new Field[]{field}, true);
                setFailsafeFieldValue(field, enumClass, null);
                break;
            }
        }
    }

    public Object createAndInitializeCollection(Class<?> clazz, Object value) {
        Class generics = extractGenerics(clazz);
        return getObject(clazz, value, generics);
    }

    private Object getObject(Class<?> clazz, Object value, Class<?> generics) {
        Object result;
        if (Collection.class.isAssignableFrom(clazz)) {
            result = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
            try {
                ((Collection) result).add(value);
            } catch (Exception ignored) {

            }
        } else
            result = getObject_(clazz, value, generics);
        return result;
    }

    public Object createAndInitializeCollection(Class<?> clazz, String methodName, Object value)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Class generics = extractGenerics(clazz);
        Object bean = generics.newInstance();
        PropertyUtils.setProperty(bean, methodName, value);
        return getObject(clazz, bean, generics);
    }

    public Collection<Object> createAndInitializeTypedCollection(Class<?> typedClassForCollection)
            throws IllegalAccessException, InstantiationException {
        return createAndInitializeTypedCollection(typedClassForCollection, null);
    }

    public Collection<Object> createAndInitializeTypedCollection(Class<?> clazz, String methodName, Object value)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Collection<Object> result = new ArrayList<>();
        Object bean = clazz.newInstance();
        PropertyUtils.setProperty(bean, methodName, value);
        result.add(bean);
        return result;
    }

    public <T> T createObject(Class<T> type)
            throws InvocationTargetException {
        return createObject(type, true);
    }

    public <T> T createObject(Class<T> type, boolean privateConstructors)
            throws InvocationTargetException {
        // Use no-arg constructor.
        Constructor constructor = null;
        for (Constructor typeConstructor : type.getConstructors()) {
            if (typeConstructor.getParameterTypes().length == 0) {
                constructor = typeConstructor;
                break;
            }
        }

        if (constructor == null && privateConstructors) {
            // Try a private constructor.
            try {
                constructor = type.getDeclaredConstructor();
                constructor.setAccessible(true);
            } catch (SecurityException | NoSuchMethodException ignored) {
            }
        }

        // Otherwise try to use a common implementation.
        if (constructor == null) {
            try {
                if (List.class.isAssignableFrom(type)) {
                    constructor = ArrayList.class.getConstructor();
                } else if (Set.class.isAssignableFrom(type)) {
                    constructor = HashSet.class.getConstructor();
                } else if (Map.class.isAssignableFrom(type)) {
                    constructor = LinkedHashMap.class.getConstructor();
                }
            } catch (Exception ex) {
                throw new InvocationTargetException(ex, "Error getting constructor for class: " + type.getName());
            }
        }

        if (constructor == null)
            throw new InvocationTargetException(null,
                    "Unable to find a no-arg constructor for class: " + type.getName());

        try {
            return (T) constructor.newInstance();
        } catch (Exception ex) {
            throw new InvocationTargetException(ex, "Error constructing instance of class: " + type.getName());
        }
    }

    /**
     * Perform a deep initialization of the given object. That action includes initializing all its fields, according to the 'includeParent' indicator.
     *
     * @param object        The object to initialize
     * @param includeParent Indicates whether or not include the parent's fields
     * @throws IllegalArgumentException If is not possible to initialize provided object.
     * @throws IllegalAccessException   If is not possible to initialize provided object.
     * @deprecated Use <code>com.araguacaima.commons.utils.ReflectionUtils#deepInitialization(java.lang.Class)</code> instead
     */
    @Deprecated()
    public void deepInitialization(Object object, boolean includeParent)
            throws IllegalArgumentException, IllegalAccessException {
        deepInitialization(object, null, includeParent);
    }

    /**
     * Perform a deep initialization of the given object including all its parent's fields.
     *
     * @param object The object to initialize
     * @throws IllegalArgumentException If is not possible to initialize provided object.
     * @throws IllegalAccessException   If is not possible to initialize provided object.
     * @deprecated Use <code>com.araguacaima.commons.utils.ReflectionUtils#deepInitialization(java.lang.Class)</code> instead
     */
    @Deprecated()
    public void deepInitialization(Object object)
            throws IllegalArgumentException, IllegalAccessException {
        deepInitialization(object, null, true);
    }

    /**
     * Perform a deep initialization of the given object. That action includes initializing all its fields, according to the 'includeParent' indicator inside the provided set of 'packages'.
     *
     * @param object   The object to initialize
     * @param packages Set of packages to searching for.
     * @throws IllegalArgumentException If is not possible to initialize provided object.
     * @throws IllegalAccessException   If is not possible to initialize provided object.
     * @deprecated Use <code>com.araguacaima.commons.utils.ReflectionUtils#deepInitialization(java.lang.Class)</code> instead
     */
    @Deprecated()
    public void deepInitialization(Object object, Set<String> packages)
            throws IllegalArgumentException, IllegalAccessException {
        deepInitialization(object, packages, true);
    }

    /**
     * Perform a deep initialization of the given class according to the default random builder configuration.
     *
     * @param clazz The class to initialize
     * @param <T>   This is the type parameter
     * @return A new instance of the given class full initialized.
     * @see io.github.benas.randombeans.EnhancedRandomBuilder
     */
    public <T> T deepInitialization(Class<T> clazz) {
        return deepInitialization(clazz, randomBuilder);
    }

    /**
     * Perform a deep initialization of the given class according to the provided random builder configuration.
     *
     * @param clazz         The class to initialize
     * @param randomBuilder The random builder configuration.
     * @param <T>           This is the type parameter
     * @return A new instance of the given class full initialized.
     * @see io.github.benas.randombeans.EnhancedRandomBuilder
     */
    public <T> T deepInitialization(Class<T> clazz, EnhancedRandomBuilder randomBuilder) {
        EnhancedRandom random = randomBuilder.build();
        return random.nextObject(clazz);
    }

    /**
     * Perform a deep initialization of the given object. That action includes initializing all its fields, according to the 'includeParent' indicator.
     *
     * @param object        The object to initialize
     * @param packages      Set of packages to searching for.
     * @param includeParent Indicates whether or not include the parent's fields.
     * @throws IllegalArgumentException If is not possible to initialize provided object.
     * @throws IllegalAccessException   If is not possible to initialize provided object.
     * @deprecated Use <code>com.araguacaima.commons.utils.ReflectionUtils#deepInitialization(java.lang.Class)</code> instead
     */
    @Deprecated()
    public void deepInitialization(Object object, Set<String> packages, boolean includeParent)
            throws IllegalArgumentException, IllegalAccessException {

        Field[] fields;

        if (includeParent) {
            Collection<Field> fields_ = getAllFieldsIncludingParents(object);
            fields = fields_.toArray(new Field[0]);
        } else {
            fields = object.getClass().getDeclaredFields();
        }
        for (Field field : fields) {
            String fieldName = field.getName();
            Class<?> fieldClass = field.getType();
            // skip primitives
            if (fieldClass.isEnum() || fieldClass.isAssignableFrom(Enum.class) || fieldClass.isPrimitive() || (!isCollectionImplementation(fieldClass) && StringUtils.isNotBlank(getSimpleJavaTypeOrNull
                    (fieldClass.getSimpleName(),
                            false)))) {
                continue;
            }
            // allow access to private fields
            boolean isAccessible = field.isAccessible();
            Object fieldValue;
            Object value = null;

            // skip if not in packages
            boolean inPackage = packages == null || packages.size() == 0;
            if (!inPackage) {
                for (String pack : packages) {
                    if (fieldClass.getPackage().getName().startsWith(pack)) {
                        inPackage = true;
                        break;
                    }
                }
                if (!inPackage) {
                    continue;
                }
            }
            field.setAccessible(true);
            fieldValue = field.get(object);

            if (fieldValue != null) {
                try {
                    if (isCollectionImplementation(fieldClass)) {
                        deepInitialization(fieldValue, packages, false);
                        value = createAndInitializeTypedCollection(fieldClass, fieldValue);
                    } else if (isMapImplementation(fieldClass)) {
                        deepInitialization(fieldValue, packages, false);
                        value = new TreeMap();
                        ((Map) value).putAll((Map) fieldValue);
                    } else {
                        value = fieldValue;
                    }
                } catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
                    log.error("Could not initialize " + fieldClass.getSimpleName() + ", Maybe it's an " +
                            "interface, abstract class, or it hasn't an empty Constructor");
                    continue;
                }
                field.set(object, value);
            } else {
                try {
                    value = fieldClass.newInstance();
                    field.set(object, value);
                    continue;
                } catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
                    log.error("Could not initialize " + fieldClass.getSimpleName() + ", Maybe it's an " +
                            "interface, abstract class, or it hasn't an empty Constructor");
                    continue;
                }
            }

            // reset accessible
            field.setAccessible(isAccessible);

            // recursive call for sub-objects
            deepInitialization(fieldValue, packages, false);
        }
    }

    public void encloseStringValuesWithCDATA(Object object) {
        if (object != null) {
            Class clazz = object.getClass();
            Collection<Method> declaredGetterMethods = getDeclaredGetterMethods(clazz);
            Collection<Method> declaredSetterMethods = getDeclaredSetterMethods(clazz);
            for (final Method getterMethod : declaredGetterMethods) {
                Object[] argsGetter = {};
                Object[] argsSetter = {String.class};
                Object value;
                Class returnedType = getterMethod.getReturnType();
                final Predicate<Method> methodPredicate = method -> method.getName().equals(getterMethod.getName()
                        .replaceFirst(
                                "get",
                                "set"));
                Method setterMethod = IterableUtils.find(declaredSetterMethods, methodPredicate);
                if (setterMethod != null) {
                    if (returnedType.equals(String.class)) {

                        try {
                            value = getterMethod.invoke(object, argsGetter);
                            argsSetter[0] = stringUtils.enclose((String) value,
                                    StringUtils.CDATA_START,
                                    StringUtils.CDATA_END);
                            setterMethod.invoke(object, argsSetter);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }

                    } else if (implementsClass(returnedType, Collection.class)) {
                        log.info("Is Collection");
                    } else if (isString(returnedType)) {
                        log.info("Is String");
                    } else if (isPrimitive(returnedType)) {
                        log.info("Is Primitive");
                    }
                }
            }
        }
    }

    public Collection<Method> getDeclaredGetterMethods(Class clazz) {
        return CollectionUtils.select(Arrays.asList(clazz.getDeclaredMethods()),
                method -> method.getName().startsWith("get"));
    }

    public Collection<Method> getDeclaredSetterMethods(Class clazz) {
        return CollectionUtils.select(Arrays.asList(clazz.getDeclaredMethods()),
                method -> method.getName().startsWith("set"));
    }

    public boolean implementsClass(Class clazz, final Class implementedClass) {
        return IterableUtils.find(Arrays.asList(clazz.getInterfaces()),
                interface_ -> interface_.getName().equals(implementedClass.getName())) != null;
    }

    public boolean isString(Class clazz) {
        return clazz.getName().equalsIgnoreCase("String") || clazz.getName().equalsIgnoreCase("java.lang.String");
    }

    public boolean isPrimitive(final Class clazz) {
        return IterableUtils.find(PRIMITIVE_TYPES, o -> ((Class) o).getName().equals(clazz.getName())) != null;
    }

    public Class extractGenericsKeyValue(Field field, boolean extractKey) {
        Class clazz = null;
        if (Map.class.isAssignableFrom(field.getType())) {
            try {
                clazz = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[extractKey ? 0
                        : 1];
            } catch (Throwable ignored) {
            }
            if (clazz == null) {
                clazz = field.getType();
            }
        } else {
            clazz = field.getType();
        }
        return extractGenerics(clazz);
    }

    public Class extractTypedGenericsParameter(Class clazz) {
        if (clazz == null) {
            return null;
        }
        if (!isCollectionImplementation(clazz)) {
            return clazz;
        }
        Class generics;
        if (Collection.class.isAssignableFrom(clazz)) {
            TypeVariable[] typedGenerics = clazz.getTypeParameters();
            if (typedGenerics == null) {
                generics = Object.class;
            } else {
                try {
                    Object type = (typedGenerics[0]).getBounds()[0];
                    generics = (Class) type;
                } catch (ClassCastException | IndexOutOfBoundsException ignored) {
                    try {
                        generics = (Class) clazz.getGenericInterfaces()[0];
                    } catch (Throwable t) {
                        generics = Object.class;
                    }
                }
            }
        } else
            generics = getClass(clazz);
        return generics;
    }

    public void fillObjectWithMap(Object object, Map arg) {
        String key;
        String value;
        for (Object o : arg.keySet()) {
            key = (String) o;
            value = (String) arg.get(key);
            invokeSimpleSetter(object, key, value);
        }
    }

    /**
     * @param object    The object for simple setter invokation
     * @param fieldName The field on which the simple setter will be performed
     * @param value     The value to be assigned as parameter to the setter method
     */
    public void invokeSimpleSetter(Object object, final String fieldName, final Object value) {
        Method method = IterableUtils.find(getDeclaredSetterMethods(object.getClass()), innerMethod -> {
            boolean result = false;
            boolean setterFound = (innerMethod.getName().toUpperCase().equals(("set" + fieldName).toUpperCase()));
            if (setterFound) {
                result = innerMethod.getParameterTypes()[0].getName().equals(value.getClass().getName());
                if (!result) {
                    Class clazz = getPrimitive(innerMethod.getParameterTypes()[0]);
                    if (clazz != null) {
                        String primitive = getClassFromPrimitive(clazz).getName();
                        if (StringUtils.isNotBlank(primitive)) {
                            result = (innerMethod.getName().toUpperCase().equals(("set" + fieldName).toUpperCase()))
                                    && (primitive.equals(
                                    value.getClass().getName()));
                        }
                    }
                }
            }
            return result;
        });
        Object[] args = {value};

        try {
            object = method.invoke(object, args);
        } catch (IllegalAccessException e) {
            log.error("Impossible to invoke method: " + fieldName + " because of an IllegalAccessException");
        } catch (InvocationTargetException e) {
            log.error("Impossible to invoke method: " + fieldName + " because of an InvocationTargetException");
        } catch (NullPointerException e) {
            log.error("Impossible to invoke method: " + fieldName + " because of an NullPointerException, " + "may "
                    + "be" + " the incoming field:" + fieldName + " have not a setter");
        }
    }

    public Class getPrimitive(final Class clazz) {
        return (Class) IterableUtils.find(PRIMITIVE_TYPES, o -> ((Class) o).getName().equals(clazz.getName()));
    }

    public Class getClassFromPrimitive(Class clazz) {
        Class newClazz = (Class) PRIMITIVE_AND_BASIC_TYPES.get(clazz);
        if (newClazz != null) {
            return newClazz;
        } else {
            return clazz;
        }

    }

    public String formatObjectValues(Object object,
                                     boolean includeHeader,
                                     boolean newLine,
                                     String indentation,
                                     boolean considerHierarchy) {
        try {
            StringBuilder objectValuesFormatted = new StringBuilder();
            try {
                if (object != null) {
                    Class clazz = object.getClass();
                    Collection fieldNames = getFieldNames(clazz);
                    if (newLine) {
                        objectValuesFormatted.append(StringUtils.NEW_LINE);
                    }
                    if (isPrimitive(clazz) || isBasic(clazz)) {
                        if (newLine) {
                            objectValuesFormatted.append(getPrimitive(clazz)).append(StringUtils.BLANK_SPACE).append(
                                    StringUtils.EQUAL_SYMBOL).append(StringUtils.BLANK_SPACE).append(object).append(
                                    StringUtils.NEW_LINE);
                        } else {
                            objectValuesFormatted.append(object);
                        }
                    } else {
                        if (includeHeader) {
                            objectValuesFormatted.append(
                                    "==============================================================").append
                                    (StringUtils.NEW_LINE).append(
                                    StringUtils.TAB).append("Values retrieved for incoming object ").append
                                    (StringUtils.BLANK_SPACE).append(
                                    "(that belongs to: ").append(getSimpleClassName(clazz)).append(")").append(
                                    StringUtils.NEW_LINE).append(
                                    "--------------------------------------------------------------").append
                                    (StringUtils.NEW_LINE);
                        }
                        Collection<Method> getterMethods = considerHierarchy ? getGetterMethods(clazz) :
                                getDeclaredGetterMethods(
                                        clazz);
                        for (final Method method : getterMethods) {
                            final String fieldName = (String) IterableUtils.find(fieldNames,
                                    o -> ((String) o).equalsIgnoreCase(method.getName().replaceFirst("get",
                                            StringUtils.EMPTY)));
                            Object value;
                            if (object instanceof Collection) {
                                if (!method.getName().equals("getClass")) {
                                    objectValuesFormatted.append(formatObjectValues(((Collection) object).toArray(),
                                            false,
                                            false,
                                            indentation,
                                            false));
                                }
                            } else if (object instanceof Object[]) {
                                int collectionSize = ((Object[]) object).length;
                                for (int i = 0; i < collectionSize; i++) {
                                    try {
                                        value = ((Object[]) object)[i];
                                        objectValuesFormatted.append(indentation).append("[").append(i).append("]")
                                                .append(
                                                        StringUtils.BLANK_SPACE).append("(").append(getSimpleClassName(value
                                                .getClass())).append(
                                                ")").append(StringUtils.BLANK_SPACE).append(StringUtils.EQUAL_SYMBOL)
                                                .append(
                                                        StringUtils.BLANK_SPACE).append(StringUtils.isNotBlank(indentation)
                                                && !isBasic(
                                                value.getClass()) ? String.valueOf(StringUtils
                                                .NEW_LINE) : StringUtils.EMPTY).append(
                                                formatObjectValues(value,
                                                        false,
                                                        false,
                                                        !isBasic(value.getClass()) ? indentation + StringUtils.TAB +
                                                                StringUtils.TAB : StringUtils.EMPTY,
                                                        false)).append(StringUtils.NEW_LINE);
                                    } catch (Exception e) {
                                        objectValuesFormatted.append("[").append(i).append("]").append(StringUtils
                                                .BLANK_SPACE).append(
                                                StringUtils.EQUAL_SYMBOL).append(StringUtils.BLANK_SPACE).append(
                                                UNKNOWN_VALUE).append(StringUtils.NEW_LINE);
                                    }
                                }
                            } else {
                                if (!method.getName().equals("getClass")) {
                                    Object[] args = {};
                                    try {
                                        value = method.invoke(object, args);
                                        objectValuesFormatted.append(fieldName).append(StringUtils.BLANK_SPACE).append(
                                                "(").append(getSimpleClassName(value.getClass())).append(")").append(
                                                StringUtils.BLANK_SPACE).append(StringUtils.EQUAL_SYMBOL).append(
                                                StringUtils.BLANK_SPACE).append(!isBasic(value.getClass()) ? String.valueOf(StringUtils.NEW_LINE) : StringUtils.EMPTY).append(value
                                                != object ? formatObjectValues(
                                                value,
                                                false,
                                                false,
                                                !isBasic(value.getClass()) ? String.valueOf(StringUtils
                                                        .TAB) +
                                                        StringUtils.TAB : StringUtils.EMPTY,
                                                false) : toString(object)).append(StringUtils.NEW_LINE);
                                    } catch (IllegalAccessException | InvocationTargetException e) {
                                        Object valueOfField = UNKNOWN_VALUE;
                                        Field field;
                                        try {
                                            field = getField(object, fieldName);
                                            field.setAccessible(true);
                                            valueOfField = field.get(object);
                                        } catch (IllegalAccessException e1) {
                                            log.error("Impossible to get the value of the field: " + fieldName + " "
                                                    + "directly from the object: " + object + " because of an " +
                                                    "IllegalAccessException");
                                        }
                                        objectValuesFormatted.append(getSimpleClassName(valueOfField.getClass()))
                                                .append(
                                                        ")").append(StringUtils.BLANK_SPACE).append(StringUtils.EQUAL_SYMBOL)
                                                .append(
                                                        StringUtils.BLANK_SPACE).append(valueOfField).append(StringUtils
                                                .NEW_LINE);
                                    } catch (NullPointerException e) {
                                        Object valueOfField = UNKNOWN_VALUE;
                                        Field field = getField(object, fieldName);
                                        objectValuesFormatted.append(field == null ? getSimpleClassName(method
                                                .getReturnType()) : getSimpleClassName(
                                                field.getType())).append(")").append(StringUtils.BLANK_SPACE).append(
                                                StringUtils.EQUAL_SYMBOL).append(StringUtils.BLANK_SPACE).append
                                                ("null").append(
                                                StringUtils.NEW_LINE);
                                    } catch (Exception e) {
                                        Object valueOfField = UNKNOWN_VALUE;
                                        Field field;
                                        try {
                                            field = getField(object, fieldName);

                                            if (field != null) {
                                                field.setAccessible(true);
                                                valueOfField = field.get(object);
                                            }
                                        } catch (IllegalAccessException e1) {
                                            log.error("Impossible to get the value of the field: " + fieldName + " "
                                                    + "directly from the object: " + object + " because of an " +
                                                    "IllegalAccessException");
                                        }
                                        objectValuesFormatted.append(valueOfField == null ? getSimpleClassName(method
                                                .getReturnType()) : getSimpleClassName(
                                                valueOfField.getClass())).append(")").append(StringUtils.BLANK_SPACE)
                                                .append(
                                                        StringUtils.EQUAL_SYMBOL).append(StringUtils.BLANK_SPACE).append(
                                                valueOfField).append(StringUtils.NEW_LINE);
                                    }
                                }
                            }
                        }
                        if (includeHeader) {
                            objectValuesFormatted.append(
                                    "==============================================================");
                        }
                    }
                } else {
                    return "null";
                }
            } catch (Exception e) {
                log.error("ERROR: " + e.getMessage());
            }
            return objectValuesFormatted.toString();
        } catch (Exception ignored) {
            return object.toString();
        }
    }

    public Collection<String> getAllEmptyFieldNames(Object object) {
        return getAllEmptyFieldNames(object, null);
    }

    public Collection<String> getAllEmptyFieldNames(Object object, Collection<String> excludeFields) {
        String fieldName;
        Collection<String> collection = null;
        if (excludeFields == null) {
            excludeFields = new ArrayList();
        }
        if (object != null) {
            final Collection<String> fieldNames = getFieldNames(object.getClass());
            collection = CollectionUtils.disjunction(fieldNames, excludeFields);
            CollectionUtils.filter(collection, fieldName1 -> {
                Object value = invokeGetter(object, fieldName1);
                return isNullOrEmpty(value);
            });
        }
        return collection;
    }

    public Collection<Field> getAllFieldsIncludingParents(Object object) {
        return getAllFieldsIncludingParents(object.getClass());
    }

    public Collection<String> getAllFieldsNamesOfType(Class clazz, final Class type) {
        return getAllFieldsNamesOfType(clazz, null, type);
    }

    public Collection<Field> getAllFieldsOfType(Object object, final Class type) {
        return getAllFieldsOfType(object.getClass(), null, type);
    }

    public Collection<Field> getAllFieldsOfType(Class clazz, final Class type) {
        return getAllFieldsOfType(clazz, null, type);
    }

    public Collection<String> getAllMethodNamesIncludingParents(Class clazz) {
        return CollectionUtils.collect(getAllMethodsIncludingParents(clazz,
                null,
                Modifier.VOLATILE | Modifier.NATIVE | Modifier.TRANSIENT), METHOD_NAME_TRANSFORMER);
    }

    public Collection<Method> getAllMethodsIncludingParents(Class clazz,
                                                            final Integer modifiersInclusion,
                                                            final Integer modifiersExclusion) {
        final Collection<Method> methods = new ArrayList<>();
        MethodFilter fieldFilterModifierInclusion = method -> modifiersInclusion == null || (method.getModifiers() &
                modifiersInclusion) != 0;
        MethodFilter fieldFilterModifierExclusion = method -> modifiersExclusion == null || (method.getModifiers() &
                modifiersExclusion) == 0;
        doWithMethods(clazz,
                methods::add,
                modifiersInclusion == null ? modifiersExclusion == null ? null : fieldFilterModifierExclusion :
                        fieldFilterModifierInclusion);
        return methods;
    }

    public Collection<String> getAllMethodsIncludingParentsNames(Class clazz) {
        return getAllMethodsIncludingParentsNames(clazz, null);
    }

    public Collection<String> getAllMethodsIncludingParentsNames(Class clazz, Collection<String> excludeMethods) {
        Collection<Method> allMethods = getAllMethodsIncludingParents(clazz);
        Collection<String> result = CollectionUtils.collect(allMethods, METHOD_NAME_TRANSFORMER);
        Collections.sort((List) result);
        if (CollectionUtils.isEmpty(excludeMethods)) {
            return result;
        }
        CollectionUtils.filterInverse(result, methodName -> methodNameIsContainedIn(methodName, excludeMethods));
        return result;
    }

    public Collection<Method> getAllMethodsIncludingParents(Class clazz) {
        return getAllMethodsIncludingParents(clazz, null, Modifier.VOLATILE | Modifier.NATIVE | Modifier.TRANSIENT);
    }

    private boolean methodNameIsContainedIn(String methodName, Collection<String> excludeMethods) {
        return excludeMethods.contains(methodName);
    }

    public Collection<String> getAllMethodsNamesOfType(Class clazz, final Class type) {
        return getAllMethodsNamesOfType(clazz, null, type);
    }

    public Collection<String> getAllMethodsNamesOfType(Class clazz,
                                                       Collection<String> excludeMethods,
                                                       final Class type) {
        final Collection result = getAllMethodsOfType(clazz, excludeMethods, type);
        CollectionUtils.transform(result, o -> ((Method) o).getName());
        return result;
    }

    public Collection<Method> getAllMethodsOfType(Class clazz, Collection<String> excludeMethods,
                                                  final Class type) {
        final Collection<Method> result = new ArrayList();
        if (clazz != null) {
            IterableUtils.forEach(getAllMethodsIncludingParents(clazz, excludeMethods), method -> {
                if (method.getReturnType().equals(type)) {
                    result.add(method);
                }
            });
        }
        return result;
    }

    public Collection<Method> getAllMethodsIncludingParents(Class clazz, Collection<String> excludeMethods) {
        Collection<Method> result = getAllMethodsIncludingParents(clazz);
        if (CollectionUtils.isEmpty(excludeMethods)) {
            return result;
        }
        return CollectionUtils.select(result, method -> {
            String methodName = method.getName();
            return !excludeMethods.contains(methodName);
        });
    }

    public Collection<String> getAllMethodsNamesOfType(Object object, final Class type) {
        return getAllMethodsNamesOfType(object, null, type);
    }

    public Collection<String> getAllMethodsNamesOfType(Object object,
                                                       Collection<String> excludeMethods,
                                                       final Class type) {
        return CollectionUtils.collect(getAllMethodsOfType(object, excludeMethods, type), METHOD_NAME_TRANSFORMER);
    }

    public Collection<Method> getAllMethodsOfType(Object object, Collection<String> excludeFields,
                                                  final Class type) {
        Collection<Method> result = new ArrayList();

        if (object != null) {
            result = getAllMethodsOfType(object.getClass(), excludeFields, type);
        }
        return result;
    }

    public Collection<String> getDeclaredMethodsNames(Class clazz) {
        return getDeclaredMethodsNames(clazz, null);
    }

    public Collection<String> getDeclaredMethodsNames(Class clazz, Collection<String> excludeMethods) {
        Collection<Method> declaredMethods = new ArrayList(Arrays.asList(clazz.getDeclaredMethods()));
        Collection<String> result = CollectionUtils.collect(declaredMethods, METHOD_NAME_TRANSFORMER);
        if (CollectionUtils.isEmpty(excludeMethods)) {
            return result;
        }
        CollectionUtils.filterInverse(result, excludeMethods::contains);
        Collections.sort((List) result);
        return result;
    }

    public Field getField(Class clazz, String fieldName) {
        return IterableUtils.find(getAllFieldsIncludingParents(clazz),
                field -> fieldNameEqualsToPredicate(field, fieldName));
    }

    public FieldCompare getFieldCompare() {
        return FIELD_COMPARE;
    }

    public Field getFieldInclusiveOnParents(Class clazz, String field) {
        return findField(clazz, field);
    }

    public Collection<String> getFieldNames(Class clazz) {
        return getFieldNames(clazz, null);
    }

    public Collection<String> getFieldNames(Class clazz, Collection<String> excludeFields) {
        Collection<Field> fields = getAllFieldsIncludingParents(clazz);
        Collection<String> result = CollectionUtils.collect(fields, FIELD_NAME_TRANSFORMER);
        if (CollectionUtils.isEmpty(excludeFields)) {
            return result;
        }
        CollectionUtils.filterInverse(result, field -> field.startsWith("class$") || excludeFields.contains(field));
        Collections.sort((List) result);
        return result;

    }

    public Object getFieldType(Object object, final String fieldName) {
        Object value = null;
        if (isBasic(object.getClass()) || StringUtils.isBlank(fieldName)) {
            return object.getClass().getName();
        }
        Field field = IterableUtils.find(getDeclaredFields(object),
                innerField -> innerField.getName().toUpperCase().equals((fieldName).toUpperCase()));
        return field.getType().getName();
    }

    public boolean isBasic(final Class clazz) {
        return IterableUtils.find(BASIC_CLASSES, o -> ((Class) o).getName().equals(clazz.getName())) != null;
    }

    public Collection<Field> getDeclaredFields(Object object) {
        Field[] declaredFields = getDeclaredFields(object.getClass());
        return Arrays.asList(declaredFields);
    }

    public Map getFieldValueMap(Object object) {
        Map objectFieldValueMap = new HashMap();
        try {
            if (object != null) {
                Class clazz = object.getClass();
                if (!isPrimitive(clazz) && !isBasic(clazz)) {
                    Collection<Field> fields = getAllFieldsIncludingParents(clazz);
                    String fieldName;
                    Object[] args = new Object[0];
                    for (Field field : fields) {
                        fieldName = field.getName();
                        objectFieldValueMap.put(fieldName, invokeGetter(object, fieldName));
                    }
                } else {
                    objectFieldValueMap.put("[".concat(getSimpleClassName(clazz)).concat("]"), object);
                }
            }
        } catch (Exception e) {
            log.error("ERROR: " + e.getMessage());
        }
        return objectFieldValueMap;
    }

    public Object invokeGetter(Object object, final String fieldName) {
        Object value = null;
        Method method = IterableUtils.find(getGetterMethods(object.getClass()),
                innerMethod -> innerMethod.getName().toUpperCase().equals(("get" + fieldName).toUpperCase()));
        Object[] args = {};
        try {
            value = method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Impossible to invoke method: " + "get" + fieldName + " because of an " + e.getClass().getName());
            try {
                Field field = getField(object, fieldName);
                field.setAccessible(true);
                value = field.get(object);
            } catch (IllegalAccessException e1) {
                log.error("Impossible to get the value of the field: " + fieldName + " directly from the object: " +
                        object + " because of an " + e1.getClass().getName());
            }
        } catch (NullPointerException e) {
            log.error("Impossible to invoke method: " + "get" + fieldName + " because of an NullPointerException, " +
                    "may be the incoming field :" + fieldName + " have not a getter");
            try {
                Field field = getField(object, fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    value = field.get(object);
                }
            } catch (IllegalAccessException e1) {
                log.error("Impossible to get the value of the field: " + fieldName + " directly from the object: " +
                        object + " because of an IllegalAccessException");
            }
        }
        return value;
    }

    public Collection<Method> getGetterMethods(Class clazz) {
        return CollectionUtils.select(getAllMethodsIncludingParents(clazz), METHOD_IS_GETTER_PREDICATE);
    }

    public Field getField(Object object, final String fieldName) {
        return getField(object.getClass(), fieldName);
    }

    public String getFullyQualifiedJavaTypeOrNull(Object object) {
        return getFullyQualifiedJavaTypeOrNull(object.getClass());
    }

    public String getFullyQualifiedJavaTypeOrNull(Class clazz) {
        return getFullyQualifiedJavaTypeOrNull(clazz.getName(), true);
    }

    public Collection<Method> getGetterMethodsForField(Class clazz, final String fieldName) {
        return CollectionUtils.select(getGetterMethods(clazz),
                method -> methodNameEqualsToPredicate(method, "get".concat(StringUtils.capitalize(fieldName))));
    }

    private boolean methodNameEqualsToPredicate(Method method, String methodName) {
        return !StringUtils.isBlank(methodName) && method.getName().equals(methodName);
    }

    public Method getMethod(Class clazz, String methodName) {
        return IterableUtils.find(getAllMethodsIncludingParents(clazz),
                method -> methodNameEqualsToPredicate(method, methodName));
    }

    public Collection<Method> getSetterMethodsForField(Class clazz, final String fieldName) {
        return CollectionUtils.select(getSetterMethods(clazz),
                method -> methodNameEqualsToPredicate(method, "set".concat(StringUtils.capitalize(fieldName))));
    }

    public Collection<Method> getSetterMethods(Class clazz) {
        return CollectionUtils.select(getAllMethodsIncludingParents(clazz), METHOD_IS_SETTER_PREDICATE);
    }

    public Object getValueFromCollectionImplementation(Object value) {

        if (value == null) {
            return null;
        }

        Class<?> clazz = value.getClass();

        if (Collection.class.isAssignableFrom(clazz)) {
            return new ArrayList<>((Collection) value).get(0);
        } else if (Object[].class.isAssignableFrom(clazz) || clazz.isArray()) {
            return ((Object[]) value)[0];
        } else {
            throw new IllegalArgumentException("Invalid Type. Incoming type must be a collection implementation");
        }

    }

    public boolean implementsClass(Class clazz, final String implementedClass) {
        return IterableUtils.find(recursivelyGetAllInterfaces(clazz),
                interface_ -> classNameEqualsToPredicate(interface_, implementedClass)) != null;
    }

    public Object invokeMethod(Object object, final String methodName, final Object[] args) {
        Object result = new Object();
        try {
            return invokeMethodThrowException(object, methodName, args);
        } catch (Throwable e) {
            log.error("Impossible to invoke method: " + methodName + " because of an Exception of type: " + e
                    .getClass() + ". The exception message is: " + e.getMessage());
        }
        return result;
    }

    public Object invokeMethodThrowException(Object object, final String methodName, final Object[] args)
            throws Exception {

        Object result;
        final List<Object> objectClasses = new ArrayList(Arrays.asList(args));
        CollectionUtils.transform(objectClasses, CLASS_FROM_OBJECT_TRANSFORMER);
        Collection<Method> methods = CollectionUtils.select(getAllMethodsIncludingParents(object.getClass()),
                method -> methodNameEqualsToPredicate(method, methodName));

        Method method = IterableUtils.find(methods, (Predicate) o -> {
            Method innerMethod = (Method) o;
            final List<Class> parameterClasses = Arrays.asList(innerMethod.getParameterTypes());
            int size = objectClasses.size();
            if (size == parameterClasses.size()) {
                CollectionUtils.transform(parameterClasses, (Transformer) o1 -> getClassFromPrimitive((Class) o1));
                boolean isAssignableFrom = true;
                for (int i = 0; i < size; i++) {
                    Object objectClass = objectClasses.get(i);
                    Class aClass = parameterClasses.get(i);
                    try {
                        Class<?> cls = objectClass.getClass();
                        if (Class.class.equals(cls)) {
                            isAssignableFrom = isAssignableFrom && aClass.isAssignableFrom((Class) objectClass);
                        } else {
                            isAssignableFrom = isAssignableFrom && aClass.isAssignableFrom(cls);
                        }
                    } catch (Throwable ignored) {
                        isAssignableFrom = false;
                    }
                }
                String s = innerMethod.getName().toUpperCase();
                String s1 = (methodName).toUpperCase();
                return (s.equals(s1)) && isAssignableFrom;
            } else {
                return false;
            }
        });

        try {
            result = method.invoke(object, args);
        } catch (IllegalAccessException e) {
            log.error("Impossible to invoke method: " + methodName + " because of an IllegalAccessException. " + e
                    .getMessage());
            throw e;
        } catch (InvocationTargetException e) {
            log.error("Impossible to invoke method: " + methodName + " because of an InvocationTargetException. " + e
                    .getMessage());
            throw e;
        } catch (NullPointerException e) {
            log.error("Impossible to invoke method: " + methodName + " because of an NullPointerException, " +
                    "maybe" + " the incoming method name: '" + methodName + "' does not exists");
            throw e;
        }
        return result;
    }

    public void invokeVoid(Object object, final String voidName, final Object[] args) {
        final Collection<Object> objectClasses = new ArrayList(Arrays.asList(args));

        CollectionUtils.transform(objectClasses, CLASS_FROM_OBJECT_TRANSFORMER);
        Collection<Method> methods = CollectionUtils.select(getAllMethodsIncludingParents(object.getClass()),
                method -> methodNameEqualsToPredicate(method, voidName));

        Method method = IterableUtils.find(methods, (Predicate) o -> {
            Method innerMethod = (Method) o;

            final Collection<Class> parameterClasses = Arrays.asList(innerMethod.getParameterTypes());
            CollectionUtils.transform(parameterClasses, (Transformer) o1 -> getClassFromPrimitive((Class) o1));

            boolean result = innerMethod.getName().toUpperCase().equals((voidName).toUpperCase());
            if (parameterClasses.size() == objectClasses.size()) {
                Class[] parameterClassesArray = (Class[]) parameterClasses.toArray();
                Object[] objectClassesArray = objectClasses.toArray();

                for (int i = 0; i < parameterClassesArray.length; i++) {
                    Class parameterClass = parameterClassesArray[i];
                    Class objectClass = (Class) objectClassesArray[i];
                    result = result && (checkWhetherOrNotSuperclassesExtendsCriteria(objectClass,
                            parameterClass) || checkWhetherOrNotSuperclassesImplementsCriteria(objectClass,
                            parameterClass));
                }
            } else {
                result = false;
            }

            return result;
        });

        try {
            method.invoke(object, args);
        } catch (IllegalArgumentException e) {
            log.error("Impossible to invoke void method: " + voidName + " because of an IllegalArgumentException");
        } catch (IllegalAccessException e) {
            log.error("Impossible to invoke void method: " + voidName + " because of an IllegalAccessException");
        } catch (InvocationTargetException e) {
            log.error("Impossible to invoke void method: " + voidName + " because of an InvocationTargetException");
            Throwable exception = e.getTargetException();
            exception.printStackTrace();
        } catch (NullPointerException e) {
            log.error("Impossible to invoke void method: " + voidName + " because of an NullPointerException, " +
                    "may be the incoming void name:" + voidName + " does not exists");
        }
    }

    public boolean isBoolean(Class clazz) {
        return clazz.getName().equalsIgnoreCase("Boolean") || clazz.getName().equalsIgnoreCase("java.lang.Boolean");
    }

    public boolean isDouble(Class clazz) {
        return clazz.getName().equalsIgnoreCase("Double") || clazz.getName().equalsIgnoreCase("java.lang.Double");
    }

    public boolean isFloat(Class clazz) {
        return clazz.getName().equalsIgnoreCase("Float") || clazz.getName().equalsIgnoreCase("java.lang.Float");
    }

    public boolean isInteger(Class clazz) {
        return clazz.getName().equalsIgnoreCase("Integer") || clazz.getName().equalsIgnoreCase("java.lang.Integer");
    }

    public boolean isNullOrEmpty(Object object) {
        //TODO AMM 30072010: Este metodo explota para todos los tipos Map o Collection. Quizas sea bueno preguntar si
        // el la clase al que pertenece el objeto es una instancia de Collection o Map e invocar el método "isEmpty"
        boolean result = false;
        if (object != null) {
            Class clazz = object.getClass();
            if (isPrimitive(clazz)) {
            } else if (isString(clazz)) {
                result = StringUtils.isNotBlank((String) object);
            } else if (object instanceof Collection || object instanceof Map) {
                try {
                    Method method = clazz.getMethod("isEmpty");
                    Object value = method.invoke(object);
                    result = (Boolean) value;
                } catch (NoSuchMethodException e) {
                    log.error("Impossible to obtain the isEmpty method from object: " + object + ".");
                } catch (InvocationTargetException | IllegalAccessException e) {
                    log.error("Impossible to invoke the isEmpty method to object: " + object + ".");
                }
            }
        } else {
            return true;
        }
        return result;
    }

    public <T> T mergeObjects(T origin, T target)
            throws IllegalAccessException, InstantiationException {
        return mergeObjects(origin, target, false, false);
    }

    @SuppressWarnings("unchecked")
    public <T> T mergeObjects(T origin, T target, boolean override, boolean nullify)
            throws IllegalAccessException, InstantiationException {
        Class<?> targetClazz = target.getClass();
        Collection<Field> targetFields = getAllFieldsIncludingParents(targetClazz);
        Object returnValue = targetClazz.newInstance();
        for (Field field : targetFields) {
            try {
                field.setAccessible(true);
                Object value = field.get(origin);
                if (nullify || (value != null)) {
                    if (override) {
                        field.set(returnValue, value);
                    } else {
                        if (isMapImplementation(value.getClass())) {
                            if (((Map) value).size() == 0) {
                                field.set(returnValue, value);
                                continue;
                            }
                        }
                        if (isCollectionImplementation(value.getClass())) {
                            if (((Collection) value).size() == 0) {
                                field.set(returnValue, value);
                                continue;
                            }
                        }
                        final java.io.Serializable defaultValue = PrimitiveDefaults.getDefaultValue(value.getClass());
                        if (defaultValue != null && !defaultValue.equals(value)) {
                            field.set(returnValue, value);
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return (T) returnValue;
    }

    private boolean methodIsNotContainedIn(Method method, Collection<String> excludeMethods) {
        return !methodIsContainedIn(method, excludeMethods);
    }

    private boolean methodIsContainedIn(Method method, Collection<String> excludeMethods) {
        return IterableUtils.find(excludeMethods,
                methodName -> methodNameEqualsToPredicate(method, methodName)) != null;
    }

/*    public String toString(Object object) {
        return formatObjectValues(object, true, true, StringUtils.EMPTY, false);
    }*/

    private boolean methodNameIsNotContainedIn(String methodName, Collection<String> excludeMethods) {
        return !methodNameIsContainedIn(methodName, excludeMethods);
    }

    public LinkedList<Class> recursivelyGetAllInterfaces(Class clazz) {
        if (clazz == null || Object.class.getName().equals(clazz.getName()) || isCollectionImplementation(
                clazz) || isMapImplementation(clazz) || getFullyQualifiedJavaTypeOrNull(clazz.getName(),
                true) != null) {
            return new LinkedList<>();
        } else {
            LinkedList<Class> result = new LinkedList<>();
            Class superclass = clazz.getSuperclass();
            if (superclass != null && !Object.class.getName().equals(superclass.getName()) && clazz.isAssignableFrom(
                    superclass) && superclass.isInterface()) {
                result.add(superclass);
                result.addAll(recursivelyGetAllInterfaces(superclass));
            }
            return result;
        }
    }

    public LinkedList<Class> recursivelyGetAllSuperClasses(Class clazz) {
        if (clazz == null || Object.class.getName().equals(clazz.getName()) || isCollectionImplementation(
                clazz) || getFullyQualifiedJavaTypeOrNull(clazz.getName(), true) != null) {
            return new LinkedList<>();
        } else {
            LinkedList<Class> result = new LinkedList<>();
            Class superclass = clazz.getSuperclass();
            if (superclass != null && !Object.class.getName().equals(superclass.getName())) {
                result.add(superclass);
                result.addAll(recursivelyGetAllSuperClasses(superclass));
            }
            return result;
        }
    }

    public String toString(Object object) {
        try {
            return "\n".concat(ToStringBuilder.reflectionToString(object));
        } catch (Exception ignored) {
            return toString(object, false);
        }
    }

    public String toString(Object object, boolean includeHeader) {
        try {
            return formatObjectValues(object, includeHeader, true, StringUtils.EMPTY, false);
        } catch (Exception ignored) {
            return object.toString();
        }
    }

    public String toString(Object object, boolean includeHeader, boolean considerHierarchy) {
        try {
            return formatObjectValues(object, includeHeader, true, StringUtils.EMPTY, considerHierarchy);
        } catch (Exception ignored) {
            return object.toString();
        }
    }

    private <T> void traverseForExtraction(Object entity, Class<?>
            incomingClass, Class<T> outcomingClass, Set<T> result) {
        ReflectionUtils.doWithFields(incomingClass, field -> {
            field.setAccessible(true);
            Object object_ = field.get(entity);
            if (object_ != null) {
                if (isCollectionImplementation(object_.getClass())) {
                    ((Collection) object_).forEach(iter -> {
                        traverseForExtraction(iter, iter.getClass(), outcomingClass, result);
                        T iter1 = (T) iter;
                        if (outcomingClass.isAssignableFrom(iter1.getClass())) {
                            result.add(iter1);
                        }
                    });
                } else {
                    if (!result.contains(object_)) {
                        Class<?> generic = extractGenerics(field);
                        if (outcomingClass.isAssignableFrom(generic)) {
                            Class<?> fieldType = field.getType();
                            if (isCollectionImplementation(fieldType)) {
                                ((Collection) object_).forEach(iter -> {
                                    traverseForExtraction(iter, generic, outcomingClass, result);
                                    T iter1 = (T) iter;
                                    if (outcomingClass.isAssignableFrom(iter1.getClass())) {
                                        result.add(iter1);
                                    }
                                });
                            } else {
                                T object = (T) field.get(entity);
                                if (outcomingClass.isAssignableFrom(object.getClass())) {
                                    result.add(object);
                                }
                            }
                        } else {
                            String genericType = getFullyQualifiedJavaTypeOrNull(generic);
                            if (genericType == null) {
                                traverseForExtraction(object_, generic, outcomingClass, result);
                            }
                        }
                    }
                }
            }
        }, this::isComplex);
    }

    private boolean isComplex(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers)
                || Modifier.isFinal(modifiers)
                || Modifier.isNative(modifiers)
                || Modifier.isAbstract(modifiers)
                || Modifier.isTransient(modifiers)) {
            return false;
        }
        Class aClass = null;
        try {
            aClass = extractGenerics(field);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return aClass != null && (getFullyQualifiedJavaTypeOrNull(aClass) == null && !aClass.isEnum() && !Enum.class.isAssignableFrom(aClass));
    }

    public <T> Set<T> extractByType(Object object, Class<T> outcomingClass) {
        Set<T> result = new HashSet<>();
        traverseForExtraction(object, object.getClass(), outcomingClass, result);
        return result;
    }

    private static class FieldCompare implements Comparator {
        public int compare(Object o1, Object o2) {
            String path1 = ((Field) o1).getName();
            String path2 = ((Field) o2).getName();
            return path1.compareTo(path2);
        }
    }

    public static class PrimitiveDefaults {
        // These gets initialized to their default values
        private static final boolean DEFAULT_BOOLEAN = Boolean.FALSE;
        private static final Boolean DEFAULT_BOOLEAN_ = Boolean.FALSE;
        private static final byte DEFAULT_BYTE = (byte) 0;
        private static final Byte DEFAULT_BYTE_ = (byte) 0;
        private static final char DEFAULT_CHAR = '\0';
        private static final double DEFAULT_DOUBLE = 0d;
        private static final Double DEFAULT_DOUBLE_ = 0d;
        private static final float DEFAULT_FLOAT = 0f;
        private static final Float DEFAULT_FLOAT_ = 0f;
        private static final int DEFAULT_INT = 0;
        private static final Integer DEFAULT_INT_ = 0;
        private static final long DEFAULT_LONG = 0L;
        private static final Long DEFAULT_LONG_ = 0L;
        private static final short DEFAULT_SHORT = (short) 0;
        private static final Short DEFAULT_SHORT_ = (short) 0;
        private static final String DEFAULT_STRING_ = StringUtils.EMPTY;

        public static java.io.Serializable getDefaultValue(Class clazz) {
            if (clazz.equals(boolean.class)) {
                return DEFAULT_BOOLEAN;
            } else if (clazz.equals(Boolean.class)) {
                return DEFAULT_BOOLEAN_;
            } else if (clazz.equals(byte.class)) {
                return DEFAULT_BYTE;
            } else if (clazz.equals(Byte.class)) {
                return DEFAULT_BYTE_;
            } else if (clazz.equals(short.class)) {
                return DEFAULT_SHORT;
            } else if (clazz.equals(Short.class)) {
                return DEFAULT_SHORT_;
            } else if (clazz.equals(char.class)) {
                return DEFAULT_CHAR;
            } else if (clazz.equals(int.class)) {
                return DEFAULT_INT;
            } else if (clazz.equals(Integer.class)) {
                return DEFAULT_INT_;
            } else if (clazz.equals(long.class)) {
                return DEFAULT_LONG;
            } else if (clazz.equals(Long.class)) {
                return DEFAULT_LONG_;
            } else if (clazz.equals(float.class)) {
                return DEFAULT_FLOAT;
            } else if (clazz.equals(Float.class)) {
                return DEFAULT_FLOAT_;
            } else if (clazz.equals(double.class)) {
                return DEFAULT_DOUBLE;
            } else if (clazz.equals(Double.class)) {
                return DEFAULT_DOUBLE_;
            } else if (clazz.equals(String.class)) {
                return DEFAULT_STRING_;
            } else if (clazz.equals(Object.class)) {
                return null;
            } else {
                throw new IllegalArgumentException("Class type " + clazz + " not supported");
            }
        }
    }


    /**
     * Pre-built MethodFilter that matches all non-bridge non-synthetic methods
     * which are not declared on {@code java.lang.Object}.
     *
     * @since 3.0.5
     */
    public static final MethodFilter USER_DECLARED_METHODS =
            (method -> !method.isBridge() && !method.isSynthetic());

    /**
     * Pre-built FieldFilter that matches all non-static, non-final fields.
     */
    public static final FieldFilter COPYABLE_FIELDS =
            (field -> !(Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())));


    /**
     * Naming prefix for CGLIB-renamed methods.
     *
     * @see #isCglibRenamedMethod
     */
    private static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];

    private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];


    /**
     * Cache for {@link Class#getDeclaredMethods()} plus equivalent default methods
     * from Java 8 based interfaces, allowing for fast iteration.
     */
    private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentReferenceHashMap<>(256);

    /**
     * Cache for {@link Class#getDeclaredFields()}, allowing for fast iteration.
     */
    private static final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentReferenceHashMap<>(256);


    // Exception handling

    /**
     * Handle the given reflection exception.
     * <p>Should only be called if no checked exception is expected to be thrown
     * by a target method, or if an error occurs while accessing a method or field.
     * <p>Throws the underlying RuntimeException or Error in case of an
     * InvocationTargetException with such a root cause. Throws an
     * IllegalStateException with an appropriate message or
     * UndeclaredThrowableException otherwise.
     *
     * @param ex the reflection exception to handle
     */
    public static void handleReflectionException(Exception ex) {
        if (ex instanceof NoSuchMethodException) {
            throw new IllegalStateException("Method not found: " + ex.getMessage());
        }
        if (ex instanceof IllegalAccessException) {
            throw new IllegalStateException("Could not access method or field: " + ex.getMessage());
        }
        if (ex instanceof InvocationTargetException) {
            handleInvocationTargetException((InvocationTargetException) ex);
        }
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }

    /**
     * Handle the given invocation target exception. Should only be called if no
     * checked exception is expected to be thrown by the target method.
     * <p>Throws the underlying RuntimeException or Error in case of such a root
     * cause. Throws an UndeclaredThrowableException otherwise.
     *
     * @param ex the invocation target exception to handle
     */
    public static void handleInvocationTargetException(InvocationTargetException ex) {
        rethrowRuntimeException(ex.getTargetException());
    }

    /**
     * Rethrow the given {@link Throwable exception}, which is presumably the
     * <em>target exception</em> of an {@link InvocationTargetException}.
     * Should only be called if no checked exception is expected to be thrown
     * by the target method.
     * <p>Rethrows the underlying exception cast to a {@link RuntimeException} or
     * {@link Error} if appropriate; otherwise, throws an
     * {@link UndeclaredThrowableException}.
     *
     * @param ex the exception to rethrow
     * @throws RuntimeException the rethrown exception
     */
    public static void rethrowRuntimeException(Throwable ex) {
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }

    /**
     * Rethrow the given {@link Throwable exception}, which is presumably the
     * <em>target exception</em> of an {@link InvocationTargetException}.
     * Should only be called if no checked exception is expected to be thrown
     * by the target method.
     * <p>Rethrows the underlying exception cast to an {@link Exception} or
     * {@link Error} if appropriate; otherwise, throws an
     * {@link UndeclaredThrowableException}.
     *
     * @param ex the exception to rethrow
     * @throws Exception the rethrown exception (in case of a checked exception)
     */
    public static void rethrowException(Throwable ex) throws Exception {
        if (ex instanceof Exception) {
            throw (Exception) ex;
        }
        if (ex instanceof Error) {
            throw (Error) ex;
        }
        throw new UndeclaredThrowableException(ex);
    }


    // Constructor handling

    /**
     * Obtain an accessible constructor for the given class and parameters.
     *
     * @param clazz          the clazz to check
     * @param parameterTypes the parameter types of the desired constructor
     * @return the constructor reference
     * @throws NoSuchMethodException if no such constructor exists
     * @since 5.0
     */
    public static <T> Constructor<T> accessibleConstructor(Class<T> clazz, Class<?>... parameterTypes)
            throws NoSuchMethodException {

        Constructor<T> ctor = clazz.getDeclaredConstructor(parameterTypes);
        makeAccessible(ctor);
        return ctor;
    }

    /**
     * Make the given constructor accessible, explicitly setting it accessible
     * if necessary. The {@code setAccessible(true)} method is only called
     * when actually necessary, to avoid unnecessary conflicts with a JVM
     * SecurityManager (if active).
     *
     * @param ctor the constructor to make accessible
     * @see java.lang.reflect.Constructor#setAccessible
     */
    @SuppressWarnings("deprecation")  // on JDK 9
    public static void makeAccessible(Constructor<?> ctor) {
        if ((!Modifier.isPublic(ctor.getModifiers()) ||
                !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
            ctor.setAccessible(true);
        }
    }


    // Method handling

    /**
     * Attempt to find a {@link Method} on the supplied class with the supplied name
     * and no parameters. Searches all superclasses up to {@code Object}.
     * <p>Returns {@code null} if no {@link Method} can be found.
     *
     * @param clazz the class to introspect
     * @param name  the name of the method
     * @return the Method object, or {@code null} if none found
     */
    public static Method findMethod(Class<?> clazz, String name) {
        return findMethod(clazz, name, EMPTY_CLASS_ARRAY);
    }

    /**
     * Attempt to find a {@link Method} on the supplied class with the supplied name
     * and parameter types. Searches all superclasses up to {@code Object}.
     * <p>Returns {@code null} if no {@link Method} can be found.
     *
     * @param clazz      the class to introspect
     * @param name       the name of the method
     * @param paramTypes the parameter types of the method
     *                   (may be {@code null} to indicate any signature)
     * @return the Method object, or {@code null} if none found
     */

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            Method[] methods = searchType.isInterface() ?
                    searchType.getMethods() :
                    getDeclaredMethods(searchType, false);
            for (Method method : methods) {
                if (name.equals(method.getName()) &&
                        (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
                    return method;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * Invoke the specified {@link Method} against the supplied target object with no arguments.
     * The target object can be {@code null} when invoking a static {@link Method}.
     * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException}.
     *
     * @param method the method to invoke
     * @param target the target object to invoke the method on
     * @return the invocation result, if any
     * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
     */
    
    public static Object invokeMethod(Method method, Object target) {
        return invokeMethod(method, target, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Invoke the specified {@link Method} against the supplied target object with the
     * supplied arguments. The target object can be {@code null} when invoking a
     * static {@link Method}.
     * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException}.
     *
     * @param method the method to invoke
     * @param target the target object to invoke the method on
     * @param args   the invocation arguments (may be {@code null})
     * @return the invocation result, if any
     */
    
    public static Object invokeMethod(Method method, Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception ex) {
            handleReflectionException(ex);
        }
        throw new IllegalStateException("Should never get here");
    }

    /**
     * Determine whether the given method explicitly declares the given
     * exception or one of its superclasses, which means that an exception
     * of that type can be propagated as-is within a reflective invocation.
     *
     * @param method        the declaring method
     * @param exceptionType the exception to throw
     * @return {@code true} if the exception can be thrown as-is;
     * {@code false} if it needs to be wrapped
     */
    public static boolean declaresException(Method method, Class<?> exceptionType) {
        Class<?>[] declaredExceptions = method.getExceptionTypes();
        for (Class<?> declaredException : declaredExceptions) {
            if (declaredException.isAssignableFrom(exceptionType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Perform the given callback operation on all matching methods of the given
     * class, as locally declared or equivalent thereof (such as default methods
     * on Java 8 based interfaces that the given class implements).
     *
     * @param clazz the class to introspect
     * @param mc    the callback to invoke for each method
     * @throws IllegalStateException if introspection fails
     * @see #doWithMethods
     * @since 4.2
     */
    public static void doWithLocalMethods(Class<?> clazz, MethodCallback mc) {
        Method[] methods = getDeclaredMethods(clazz, false);
        for (Method method : methods) {
            try {
                mc.doWith(method);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Not allowed to access method '" + method.getName() + "': " + ex);
            }
        }
    }

    /**
     * Perform the given callback operation on all matching methods of the given
     * class and superclasses.
     * <p>The same named method occurring on subclass and superclass will appear
     * twice, unless excluded by a {@link MethodFilter}.
     *
     * @param clazz the class to introspect
     * @param mc    the callback to invoke for each method
     * @throws IllegalStateException if introspection fails
     * @see #doWithMethods(Class, MethodCallback, MethodFilter)
     */
    public static void doWithMethods(Class<?> clazz, MethodCallback mc) {
        doWithMethods(clazz, mc, null);
    }

    /**
     * Perform the given callback operation on all matching methods of the given
     * class and superclasses (or given interface and super-interfaces).
     * <p>The same named method occurring on subclass and superclass will appear
     * twice, unless excluded by the specified {@link MethodFilter}.
     *
     * @param clazz the class to introspect
     * @param mc    the callback to invoke for each method
     * @param mf    the filter that determines the methods to apply the callback to
     * @throws IllegalStateException if introspection fails
     */
    public static void doWithMethods(Class<?> clazz, MethodCallback mc, MethodFilter mf) {
        // Keep backing up the inheritance hierarchy.
        Method[] methods = getDeclaredMethods(clazz, false);
        for (Method method : methods) {
            if (mf != null && !mf.matches(method)) {
                continue;
            }
            try {
                mc.doWith(method);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Not allowed to access method '" + method.getName() + "': " + ex);
            }
        }
        if (clazz.getSuperclass() != null && (mf != USER_DECLARED_METHODS || clazz.getSuperclass() != Object.class)) {
            doWithMethods(clazz.getSuperclass(), mc, mf);
        } else if (clazz.isInterface()) {
            for (Class<?> superIfc : clazz.getInterfaces()) {
                doWithMethods(superIfc, mc, mf);
            }
        }
    }

    /**
     * Get all declared methods on the leaf class and all superclasses.
     * Leaf class methods are included first.
     *
     * @param leafClass the class to introspect
     * @throws IllegalStateException if introspection fails
     */
    public static Method[] getAllDeclaredMethods(Class<?> leafClass) {
        final List<Method> methods = new ArrayList<>(32);
        doWithMethods(leafClass, methods::add);
        return methods.toArray(EMPTY_METHOD_ARRAY);
    }

    /**
     * Get the unique set of declared methods on the leaf class and all superclasses.
     * Leaf class methods are included first and while traversing the superclass hierarchy
     * any methods found with signatures matching a method already included are filtered out.
     *
     * @param leafClass the class to introspect
     * @throws IllegalStateException if introspection fails
     */
    public static Method[] getUniqueDeclaredMethods(Class<?> leafClass) {
        return getUniqueDeclaredMethods(leafClass, null);
    }

    /**
     * Get the unique set of declared methods on the leaf class and all superclasses.
     * Leaf class methods are included first and while traversing the superclass hierarchy
     * any methods found with signatures matching a method already included are filtered out.
     *
     * @param leafClass the class to introspect
     * @param mf        the filter that determines the methods to take into account
     * @throws IllegalStateException if introspection fails
     * @since 5.2
     */
    public static Method[] getUniqueDeclaredMethods(Class<?> leafClass, MethodFilter mf) {
        final List<Method> methods = new ArrayList<>(32);
        doWithMethods(leafClass, method -> {
            boolean knownSignature = false;
            Method methodBeingOverriddenWithCovariantReturnType = null;
            for (Method existingMethod : methods) {
                if (method.getName().equals(existingMethod.getName()) &&
                        Arrays.equals(method.getParameterTypes(), existingMethod.getParameterTypes())) {
                    // Is this a covariant return type situation?
                    if (existingMethod.getReturnType() != method.getReturnType() &&
                            existingMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
                        methodBeingOverriddenWithCovariantReturnType = existingMethod;
                    } else {
                        knownSignature = true;
                    }
                    break;
                }
            }
            if (methodBeingOverriddenWithCovariantReturnType != null) {
                methods.remove(methodBeingOverriddenWithCovariantReturnType);
            }
            if (!knownSignature && !isCglibRenamedMethod(method)) {
                methods.add(method);
            }
        }, mf);
        return methods.toArray(EMPTY_METHOD_ARRAY);
    }

    /**
     * Variant of {@link Class#getDeclaredMethods()} that uses a local cache in
     * order to avoid the JVM's SecurityManager check and new Method instances.
     * In addition, it also includes Java 8 default methods from locally
     * implemented interfaces, since those are effectively to be treated just
     * like declared methods.
     *
     * @param clazz the class to introspect
     * @return the cached array of methods
     * @throws IllegalStateException if introspection fails
     * @see Class#getDeclaredMethods()
     * @since 5.2
     */
    public static Method[] getDeclaredMethods(Class<?> clazz) {
        return getDeclaredMethods(clazz, true);
    }

    private static Method[] getDeclaredMethods(Class<?> clazz, boolean defensive) {
        Method[] result = declaredMethodsCache.get(clazz);
        if (result == null) {
            try {
                Method[] declaredMethods = clazz.getDeclaredMethods();
                List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
                if (defaultMethods != null) {
                    result = new Method[declaredMethods.length + defaultMethods.size()];
                    System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
                    int index = declaredMethods.length;
                    for (Method defaultMethod : defaultMethods) {
                        result[index] = defaultMethod;
                        index++;
                    }
                } else {
                    result = declaredMethods;
                }
                declaredMethodsCache.put(clazz, (result.length == 0 ? EMPTY_METHOD_ARRAY : result));
            } catch (Throwable ex) {
                throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
                        "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
            }
        }
        return (result.length == 0 || !defensive) ? result : result.clone();
    }

    
    private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
        List<Method> result = null;
        for (Class<?> ifc : clazz.getInterfaces()) {
            for (Method ifcMethod : ifc.getMethods()) {
                if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(ifcMethod);
                }
            }
        }
        return result;
    }

    /**
     * Determine whether the given method is an "equals" method.
     *
     * @see java.lang.Object#equals(Object)
     */
    public static boolean isEqualsMethod(Method method) {
        if (method == null || !method.getName().equals("equals")) {
            return false;
        }
        Class<?>[] paramTypes = method.getParameterTypes();
        return (paramTypes.length == 1 && paramTypes[0] == Object.class);
    }

    /**
     * Determine whether the given method is a "hashCode" method.
     *
     * @see java.lang.Object#hashCode()
     */
    public static boolean isHashCodeMethod(Method method) {
        return (method != null && method.getName().equals("hashCode") && method.getParameterCount() == 0);
    }

    /**
     * Determine whether the given method is a "toString" method.
     *
     * @see java.lang.Object#toString()
     */
    public static boolean isToStringMethod(Method method) {
        return (method != null && method.getName().equals("toString") && method.getParameterCount() == 0);
    }

    /**
     * Determine whether the given method is originally declared by {@link java.lang.Object}.
     */
    public static boolean isObjectMethod(Method method) {
        return (method != null && (method.getDeclaringClass() == Object.class ||
                isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(method)));
    }

    /**
     * Determine whether the given method is a CGLIB 'renamed' method,
     * following the pattern "CGLIB$methodName$0".
     *
     * @param renamedMethod the method to check
     */
    public static boolean isCglibRenamedMethod(Method renamedMethod) {
        String name = renamedMethod.getName();
        if (name.startsWith(CGLIB_RENAMED_METHOD_PREFIX)) {
            int i = name.length() - 1;
            while (i >= 0 && Character.isDigit(name.charAt(i))) {
                i--;
            }
            return (i > CGLIB_RENAMED_METHOD_PREFIX.length() && (i < name.length() - 1) && name.charAt(i) == '$');
        }
        return false;
    }

    /**
     * Make the given method accessible, explicitly setting it accessible if
     * necessary. The {@code setAccessible(true)} method is only called
     * when actually necessary, to avoid unnecessary conflicts with a JVM
     * SecurityManager (if active).
     *
     * @param method the method to make accessible
     * @see java.lang.reflect.Method#setAccessible
     */
    @SuppressWarnings("deprecation")  // on JDK 9
    public static void makeAccessible(Method method) {
        if ((!Modifier.isPublic(method.getModifiers()) ||
                !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
            method.setAccessible(true);
        }
    }


    // Field handling

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the
     * supplied {@code name}. Searches all superclasses up to {@link Object}.
     *
     * @param clazz the class to introspect
     * @param name  the name of the field
     * @return the corresponding Field object, or {@code null} if not found
     */
    
    public static Field findField(Class<?> clazz, String name) {
        return findField(clazz, name, null);
    }

    /**
     * Attempt to find a {@link Field field} on the supplied {@link Class} with the
     * supplied {@code name} and/or {@link Class type}. Searches all superclasses
     * up to {@link Object}.
     *
     * @param clazz the class to introspect
     * @param name  the name of the field (may be {@code null} if type is specified)
     * @param type  the type of the field (may be {@code null} if name is specified)
     * @return the corresponding Field object, or {@code null} if not found
     */
    
    public static Field findField(Class<?> clazz, String name, Class<?> type) {
        Class<?> searchType = clazz;
        while (Object.class != searchType && searchType != null) {
            Field[] fields = getDeclaredFields(searchType);
            for (Field field : fields) {
                if ((name == null || name.equals(field.getName())) &&
                        (type == null || type.equals(field.getType()))) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }

    /**
     * Set the field represented by the supplied {@linkplain Field field object} on
     * the specified {@linkplain Object target object} to the specified {@code value}.
     * <p>In accordance with {@link Field#set(Object, Object)} semantics, the new value
     * is automatically unwrapped if the underlying field has a primitive type.
     * <p>This method does not support setting {@code static final} fields.
     * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException(Exception)}.
     *
     * @param field  the field to set
     * @param target the target object on which to set the field
     * @param value  the value to set (may be {@code null})
     */
    public static void setField(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex);
        }
    }

    /**
     * Get the field represented by the supplied {@link Field field object} on the
     * specified {@link Object target object}. In accordance with {@link Field#get(Object)}
     * semantics, the returned value is automatically wrapped if the underlying field
     * has a primitive type.
     * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException(Exception)}.
     *
     * @param field  the field to get
     * @param target the target object from which to get the field
     * @return the field's current value
     */
    
    public static Object getField(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            handleReflectionException(ex);
        }
        throw new IllegalStateException("Should never get here");
    }

    /**
     * Invoke the given callback on all locally declared fields in the given class.
     *
     * @param clazz the target class to analyze
     * @param fc    the callback to invoke for each field
     * @throws IllegalStateException if introspection fails
     * @see #doWithFields
     * @since 4.2
     */
    public static void doWithLocalFields(Class<?> clazz, FieldCallback fc) {
        for (Field field : getDeclaredFields(clazz)) {
            try {
                fc.doWith(field);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Not allowed to access field '" + field.getName() + "': " + ex);
            }
        }
    }

    /**
     * Invoke the given callback on all fields in the target class, going up the
     * class hierarchy to get all declared fields.
     *
     * @param clazz the target class to analyze
     * @param fc    the callback to invoke for each field
     * @throws IllegalStateException if introspection fails
     */
    public static void doWithFields(Class<?> clazz, FieldCallback fc) {
        doWithFields(clazz, fc, null);
    }

    /**
     * Invoke the given callback on all fields in the target class, going up the
     * class hierarchy to get all declared fields.
     *
     * @param clazz the target class to analyze
     * @param fc    the callback to invoke for each field
     * @param ff    the filter that determines the fields to apply the callback to
     * @throws IllegalStateException if introspection fails
     */
    public static void doWithFields(Class<?> clazz, FieldCallback fc, FieldFilter ff) {
        // Keep backing up the inheritance hierarchy.
        Class<?> targetClass = clazz;
        do {
            Field[] fields = getDeclaredFields(targetClass);
            for (Field field : fields) {
                if (ff != null && !ff.matches(field)) {
                    continue;
                }
                try {
                    fc.doWith(field);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException("Not allowed to access field '" + field.getName() + "': " + ex);
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);
    }

    /**
     * This variant retrieves {@link Class#getDeclaredFields()} from a local cache
     * in order to avoid the JVM's SecurityManager check and defensive array copying.
     *
     * @param clazz the class to introspect
     * @return the cached array of fields
     * @throws IllegalStateException if introspection fails
     * @see Class#getDeclaredFields()
     */
    private static Field[] getDeclaredFields(Class<?> clazz) {
        Field[] result = declaredFieldsCache.get(clazz);
        if (result == null) {
            try {
                result = clazz.getDeclaredFields();
                declaredFieldsCache.put(clazz, (result.length == 0 ? EMPTY_FIELD_ARRAY : result));
            } catch (Throwable ex) {
                throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
                        "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
            }
        }
        return result;
    }

    /**
     * Given the source object and the destination, which must be the same class
     * or a subclass, copy all fields, including inherited fields. Designed to
     * work on objects with public no-arg constructors.
     *
     * @throws IllegalStateException if introspection fails
     */
    public static void shallowCopyFieldState(final Object src, final Object dest) {
        if (!src.getClass().isAssignableFrom(dest.getClass())) {
            throw new IllegalArgumentException("Destination class [" + dest.getClass().getName() +
                    "] must be same or subclass as source class [" + src.getClass().getName() + "]");
        }
        doWithFields(src.getClass(), field -> {
            makeAccessible(field);
            Object srcValue = field.get(src);
            field.set(dest, srcValue);
        }, COPYABLE_FIELDS);
    }

    /**
     * Determine whether the given field is a "public static final" constant.
     *
     * @param field the field to check
     */
    public static boolean isPublicStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers));
    }

    /**
     * Make the given field accessible, explicitly setting it accessible if
     * necessary. The {@code setAccessible(true)} method is only called
     * when actually necessary, to avoid unnecessary conflicts with a JVM
     * SecurityManager (if active).
     *
     * @param field the field to make accessible
     * @see java.lang.reflect.Field#setAccessible
     */
    @SuppressWarnings("deprecation")  // on JDK 9
    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) ||
                !Modifier.isPublic(field.getDeclaringClass().getModifiers()) ||
                Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }


    // Cache handling

    /**
     * Clear the internal method/field cache.
     *
     * @since 4.2.4
     */
    public static void clearCache() {
        declaredMethodsCache.clear();
        declaredFieldsCache.clear();
    }


    /**
     * Action to take on each method.
     */
    @FunctionalInterface
    public interface MethodCallback {

        /**
         * Perform an operation using the given method.
         *
         * @param method the method to operate on
         */
        void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
    }


    /**
     * Callback optionally used to filter methods to be operated on by a method callback.
     */
    @FunctionalInterface
    public interface MethodFilter {

        /**
         * Determine whether the given method matches.
         *
         * @param method the method to check
         */
        boolean matches(Method method);
    }


    /**
     * Callback interface invoked on each field in the hierarchy.
     */
    @FunctionalInterface
    public interface FieldCallback {

        /**
         * Perform an operation using the given field.
         *
         * @param field the field to operate on
         */
        void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
    }


    /**
     * Callback optionally used to filter fields to be operated on by a field callback.
     */
    @FunctionalInterface
    public interface FieldFilter {

        /**
         * Determine whether the given field matches.
         *
         * @param field the field to check
         */
        boolean matches(Field field);
    }

}