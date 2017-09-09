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
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.reflect.ConstructorAccessor;
import sun.reflect.FieldAccessor;
import sun.reflect.ReflectionFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

@SuppressWarnings("unchecked")
@Component
public class ReflectionUtils {

    private static final Logger log = LoggerFactory.getLogger(ReflectionUtils.class);

    public final Collection<String> COMMONS_JAVA_TYPES_EXCLUSIONS = new ArrayList<String>() {
        {
            add("java.util.Currency");
            add("java.util.Calendar");
            add("org.joda.time.Period");
        }
    };
    public final Collection<String> COMMONS_TYPES_PREFIXES = new ArrayList<String>() {
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
    private final Collection<Class> COMMONS_COLLECTIONS_IMPLEMENTATIONS = new ArrayList<Class>() {
        {
            add(ArrayList.class);
            add(TreeSet.class);
            add(HashSet.class);
            add(LinkedHashSet.class);
            add(LinkedList.class);
        }
    };
    private final DataTypesConverter dataTypesConverter;

    private final ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();

    @Autowired
    public ReflectionUtils(DataTypesConverter dataTypesConverter) {
        this.dataTypesConverter = dataTypesConverter;
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
        Field[] fields = type.getClass().getDeclaredFields();
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
            setFailsafeFieldValue(valuesField, type, values.toArray((T[]) Array.newInstance(type, 0)));

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

    @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess"})
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

    public Collection<Object> createAndInitializeTypedCollection(Class<?> typedClassForCollection, Object value)
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

    public Collection<Object> createAndInitializeTypedCollection(Class<?> clazz, String methodName, Object value)
            throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        Collection<Object> result = new ArrayList<>();
        Object bean = clazz.newInstance();
        PropertyUtils.setProperty(bean, methodName, value);
        result.add(bean);
        return result;
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

    public Object createObject(Class type)
            throws InvocationTargetException {
        return createObject(type, true);
    }

    public Object createObject(Class type, boolean privateConstructors)
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
            return constructor.newInstance();
        } catch (Exception ex) {
            throw new InvocationTargetException(ex, "Error constructing instance of class: " + type.getName());
        }
    }

    /**
     * This method makes a "deep clone" of any Java object it is given.
     *
     * @param object The object to be cloned
     * @return A new fresh object cloned from the incoming one
     */
    public Object deepClone(Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public void deepInitialization(Object object, boolean includeParent)
            throws IllegalArgumentException, IllegalAccessException {
        deepInitialization(object, null, includeParent, true);
    }

    public void deepInitialization(Object object)
            throws IllegalArgumentException, IllegalAccessException {
        deepInitialization(object, null, true, true);
    }

    public void deepInitialization(Object object, Set<String> packages)
            throws IllegalArgumentException, IllegalAccessException {
        deepInitialization(object, packages, true, true);
    }

    public void deepInitialization(Object object, Set<String> packages, boolean includeParent, boolean verbose)
            throws IllegalArgumentException, IllegalAccessException {

        Field[] fields;

        if (includeParent) {
            Collection<Field> fields_ = getAllFieldsIncludingParents(object);
            fields = fields_.toArray(new Field[fields_.size()]);
        } else {
            fields = object.getClass().getDeclaredFields();
        }
        if (fields != null) {
            for (Field field : fields) {
                String fieldName = field.getName();
                Class<?> fieldClass = field.getType();

                // skip primitives
                if (!isCollectionImplementation(fieldClass) && StringUtils.isNotBlank(getSimpleJavaTypeOrNull
                        (fieldClass.getSimpleName(),
                        false))) {
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
                if (fieldValue == null) {
                    try {

                        if (Collection.class.isAssignableFrom(fieldClass)) {
                            //TODO Mejor esfuerzo para intentar inizilizar el collection con los tipos más comunes de
                            // implementaciones de la interfaz Collection
                            boolean found = false;
                            for (Class clazz : COMMONS_COLLECTIONS_IMPLEMENTATIONS) {
                                try {
                                    value = clazz.newInstance();
                                    field.set(object, value);
                                    found = true;
                                    break;
                                } catch (Throwable ignored) {
                                }
                            }
                            if (found) {
                                continue;
                            }
                        } else if (Object[].class.isAssignableFrom(fieldClass)) {
                            value = Object[].class.newInstance();
                        } else if (fieldClass.isArray()) {
                            value = Array.newInstance(int.class, 1);
                        } else {
                            value = fieldClass.newInstance();
                        }
                        //            child = value;
                        field.set(object, value);
                    } catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {

                        log.error("Could not initialize " + fieldClass.getSimpleName() + ", Maybe it's an " +
                                "interface, abstract class, or it hasn't an empty Constructor");
                        continue;
                    }
                }

                fieldValue = field.get(object);

                // reset accessible
                field.setAccessible(isAccessible);

                // recursive call for sub-objects
                deepInitialization(fieldValue, packages, false, verbose);
            }
        }
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    public Class extractGenerics(Field field) {
        Class clazz = null;
        try {
            clazz = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        } catch (ClassCastException ignored) {
            Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
            try {
                Field rawTypeField = type.getClass().getDeclaredField("rawType");
                rawTypeField.setAccessible(true);
                clazz = (Class) rawTypeField.get(type);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.error(e.getMessage());
            }
        } catch (Throwable ignored) {
        }
        if (clazz == null) {
            clazz = field.getType();
        }
        return extractGenerics(clazz);
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

    public Collection<Field> getAllFieldsIncludingParents(Object object) {
        return getAllFieldsIncludingParents(object.getClass());
    }

    public String getExtractedGenerics(String s) {
        String s1 = s.trim();
        try {
            s1 = s1.split("List<")[1].replaceAll(">", StringUtils.EMPTY);
        } catch (Throwable ignored) {
        }
        return s1;
    }

    public Field getFieldInclusiveOnParents(Class clazz, String field) {
        return org.springframework.util.ReflectionUtils.findField(clazz, field);
    }

    public String getFullyQualifiedJavaTypeOrNull(String type, boolean considerLists) {
        if (type == null) {
            return null;
        }
        type = dataTypesConverter.getDataTypeView(type).getTransformedDataType();
        String capitalizedType = StringUtils.capitalize(type);
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
        for (String javaTypePrefix : COMMONS_TYPES_PREFIXES) {
            try {
                clazz = Class.forName(capitalizedType.contains(".") ? capitalizedType : javaTypePrefix + "." +
                        capitalizedType);
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

    public String getFullyQualifiedJavaTypeOrNull(Object object) {
        return getFullyQualifiedJavaTypeOrNull(object.getClass());
    }

    public String getFullyQualifiedJavaTypeOrNull(Class clazz) {
        return getFullyQualifiedJavaTypeOrNull(clazz.getSimpleName(), true);
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

    public boolean isCollectionImplementation(String fullyQulifiedClassName) {
        try {
            Class clazz = Class.forName(fullyQulifiedClassName);
            return isCollectionImplementation(clazz);
        } catch (Throwable ignored) {
        }
        return false;

    }

    public boolean isList(String type) {
        try {
            return type.equals("List") || type.startsWith("List<") || type.startsWith("java.util.List<") || type.equals(
                    "Collection") || type.startsWith("Collection<") || type.startsWith("java.util.Collection<");
        } catch (Throwable ignored) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
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

    public Collection<Field> getAllFieldsIncludingParents(Class clazz) {
        return getAllFieldsIncludingParents(clazz,
                null,
                Modifier.STATIC | Modifier.VOLATILE | Modifier.NATIVE | Modifier.TRANSIENT);
    }

    public boolean isMapImplementation(Class clazz) {
        return clazz != null && Map.class.isAssignableFrom(clazz);
    }

    public Collection<Field> getAllFieldsIncludingParents(Class clazz,
                                                          final Integer modifiersInclusion,
                                                          final Integer modifiersExclusion) {
        final Collection<Field> fields_ = new ArrayList<>();
        org.springframework.util.ReflectionUtils.FieldFilter fieldFilterModifierInclusion = field ->
                modifiersInclusion == null || (field.getModifiers() & modifiersInclusion) != 0;
        org.springframework.util.ReflectionUtils.FieldFilter fieldFilterModifierExclusion = field ->
                modifiersExclusion == null || (field.getModifiers() & modifiersExclusion) == 0;
        org.springframework.util.ReflectionUtils.doWithFields(clazz,
                fields_::add,
                modifiersInclusion == null ? modifiersExclusion == null ? null : fieldFilterModifierExclusion :
                        fieldFilterModifierInclusion);
        return fields_;
    }

    public LinkedList<Class> recursivelyGetAllSuperClasses(Class clazz) {
        if (clazz == null || Object.class.getName().equals(clazz.getClass().getName()) || isCollectionImplementation(
                clazz) || getFullyQualifiedJavaTypeOrNull(clazz.getSimpleName(), true) != null) {
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
}

