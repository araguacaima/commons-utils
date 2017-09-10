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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.builder.StandardToStringStyle;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.reflect.ConstructorAccessor;
import sun.reflect.FieldAccessor;
import sun.reflect.ReflectionFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

@SuppressWarnings({"unchecked", "UnusedReturnValue"})
@Component
public class ReflectionUtils extends org.springframework.util.ReflectionUtils {

    public static final List BASIC_CLASSES = Arrays.asList(String.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class);
    public static final Transformer<Class, String> CLASS_NAME_TRANSFORMER = Class::getName;
    public static final Transformer CLASS_FROM_OBJECT_TRANSFORMER = Object::getClass;
    public static final Transformer<Field, String> FIELD_NAME_TRANSFORMER = Field::getName;
    public static final Predicate<Method> METHOD_IS_GETTER_PREDICATE = method -> method.getName().matches
            ("get[A-Z]+") || method.getName().matches(
            "is[A-Z]+");
    public static final Predicate<Method> METHOD_IS_SETTER_PREDICATE = method -> method.getName().matches("set[A-Z]+");
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
    private static final FieldCompare FIELD_COMPARE = new FieldCompare();
    private static final Logger log = LoggerFactory.getLogger(ReflectionUtils.class);

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
    }

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
    private static final Collection<Class> COMMONS_COLLECTIONS_IMPLEMENTATIONS = new ArrayList<Class>() {
        {
            add(ArrayList.class);
            add(TreeSet.class);
            add(HashSet.class);
            add(LinkedHashSet.class);
            add(LinkedList.class);
        }
    };

    private static final DataTypesConverter dataTypesConverter = new DataTypesConverter();
    private final ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
    private StringUtils stringUtils;

    @Autowired
    public ReflectionUtils(StringUtils stringUtils) {
        this.stringUtils = stringUtils;
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

    public boolean checkWhetherOrNotSuperclassesImplementsCriteria(final Class clazz, final Class interfaceCriteria) {
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

    public static Class extractGenerics(Class clazz) {
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

    public static boolean isCollectionImplementation(Class clazz) {
        return clazz != null && (Collection.class.isAssignableFrom(clazz) || Object[].class.isAssignableFrom(clazz)
                || clazz.isArray());
    }

    private static Class getClass(Class clazz) {
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

    private static Object getObject_(Class<?> clazz, Object value, Class<?> generics) {
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

    public static Collection<Object> createAndInitializeTypedCollection(Class<?> typedClassForCollection, Object value)
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

    public static Object createCollectionObject(Class<?> clazz)
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

    @SuppressWarnings("JavaReflectionMemberAccess")
    public static Class extractGenerics(Field field) {
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

    private static boolean fieldIsNotContainedIn(Field field, Collection<String> excludeFields) {
        return !fieldIsContainedIn(field, excludeFields);
    }

    private static boolean fieldIsContainedIn(Field field, Collection<String> excludeFields) {
        return IterableUtils.find(excludeFields, fieldName -> fieldNameEqualsToPredicate(field, fieldName)) != null;
    }

    private static boolean fieldNameEqualsToPredicate(Field field, String fieldName) {
        return !StringUtils.isBlank(fieldName) && field.getName().equals(fieldName);
    }

    private static boolean fieldNameIsNotContainedIn(String fieldName, Collection<String> excludeFields) {
        return !fieldNameIsContainedIn(fieldName, excludeFields);
    }

    private static boolean fieldNameIsContainedIn(String fieldName, Collection<String> excludeFields) {
        return excludeFields.contains(fieldName);
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
     * @noinspection UnusedAssignment
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
                                                value.getClass()) ? ((new StringBuffer()).append(StringUtils
                                                .NEW_LINE)).toString() : StringUtils.EMPTY).append(
                                                formatObjectValues(value,
                                                        false,
                                                        false,
                                                        !isBasic(value.getClass()) ? ((new StringBuffer()).append(
                                                                indentation).append(StringUtils.TAB).append
                                                                (StringUtils.TAB)).toString() : StringUtils.EMPTY,
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
                                                StringUtils.BLANK_SPACE).append(!isBasic(value.getClass()) ? ((new
                                                StringBuffer()).append(
                                                StringUtils.NEW_LINE)).toString() : StringUtils.EMPTY).append(value
                                                != object ? formatObjectValues(
                                                value,
                                                false,
                                                false,
                                                !isBasic(value.getClass()) ? ((new StringBuffer()).append(StringUtils
                                                        .TAB).append(
                                                        StringUtils.TAB)).toString() : StringUtils.EMPTY,
                                                false) : toString(object)).append(StringUtils.NEW_LINE);
                                    } catch (IllegalAccessException | InvocationTargetException e) {
                                        Object valueOfField = UNKNOWN_VALUE;
                                        Field field;
                                        try {
                                            field = getFieldByFieldName(object, fieldName);
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
                                        Field field = getFieldByFieldName(object, fieldName);
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
                                            field = getFieldByFieldName(object, fieldName);

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

    public static Collection<String> getAllFieldNamesIncludingParents(Class clazz) {
        return CollectionUtils.collect(getAllFieldsIncludingParents(clazz,
                null,
                Modifier.VOLATILE | Modifier.NATIVE | Modifier.TRANSIENT), FIELD_NAME_TRANSFORMER);
    }

    public static Collection<Field> getAllFieldsIncludingParents(Class clazz,
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

    public Collection<Field> getAllFieldsIncludingParents(Object object) {
        return getAllFieldsIncludingParents(object.getClass());
    }

    public Collection<String> getAllFieldsNamesOfType(Class clazz, final Class type) {
        return getAllFieldsNamesOfType(clazz, null, type);
    }

    public static Collection<String> getAllFieldsNamesOfType(Class clazz, Collection<String> excludeFields, final Class type) {
        return CollectionUtils.collect(getAllFieldsOfType(clazz, excludeFields, type), FIELD_NAME_TRANSFORMER);
    }

    public static Collection<Field> getAllFieldsOfType(Class clazz, Collection<String> excludeFields, final Class type) {
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

    public static Collection<Field> getAllFieldsIncludingParents(Class clazz, Collection<String> excludeFields) {
        Collection<Field> result = getAllFieldsIncludingParents(clazz);
        if (CollectionUtils.isEmpty(excludeFields)) {
            return result;
        }
        CollectionUtils.filterInverse(result, field -> fieldIsContainedIn(field, excludeFields));
        return result;
    }

    public static Collection<Field> getAllFieldsIncludingParents(Class clazz) {
        return getAllFieldsIncludingParents(clazz,
                null,
                Modifier.STATIC | Modifier.VOLATILE | Modifier.NATIVE | Modifier.TRANSIENT);
    }

    public static Collection<String> getAllFieldsNamesOfType(Object object, final Class type) {
        return getAllFieldsNamesOfType(object.getClass(), null, type);
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

    public Collection<Method> getAllMethodsOfType(Class clazz, Collection<String> excludeMethods, final Class type) {
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

    public Collection<Method> getAllMethodsOfType(Object object, Collection<String> excludeFields, final Class type) {
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

    public static String getExtractedGenerics(String s) {
        String s1 = s.trim();
        try {
            s1 = s1.split("List<")[1].replaceAll(">", StringUtils.EMPTY);
        } catch (Throwable ignored) {
        }
        return s1;
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
        return getDeclaredFields(object.getClass());
    }

    public Collection<Field> getDeclaredFields(Class clazz) {
        return Arrays.asList(clazz.getDeclaredFields());
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
                Field field = getFieldByFieldName(object, fieldName);
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
                Field field = getFieldByFieldName(object, fieldName);
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

    public static String getSimpleClassName(Class clazz) {
        String className = null;
        if (clazz != null) {
            className = clazz.getName();
            try {
                className = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
                className = className.substring(className.lastIndexOf("$") + 1);
                className = className.replaceAll(";", "[]");
            } catch (Exception e) {
                log.error("Impossible to get the simple name for the class: " + clazz.getName() + ". It'll be taken: " +
                        className);
            }
        }
        return className;
    }

    public Collection<Method> getGetterMethods(Class clazz) {
        return CollectionUtils.select(getAllMethodsIncludingParents(clazz), METHOD_IS_GETTER_PREDICATE);
    }

    public Field getFieldByFieldName(Object object, final String fieldName) {
        return getFieldByFieldName(object.getClass(), fieldName);
    }

    public Field getFieldByFieldName(Class clazz, final String fieldName) {
        return IterableUtils.find(getAllFieldsIncludingParents(clazz),
                field -> fieldNameEqualsToPredicate(field, fieldName));
    }

    public static String getFullyQualifiedJavaTypeOrNull(String type, boolean considerLists) {
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

    /**
     * @param object The object to obtain the class name
     * @return The class name
     * @deprecated
     */
    public static String getSimpleClassName(Object object) {
        return getSimpleClassName(object.getClass());
    }

    public static String getSimpleJavaTypeOrNull(Class type) {
        return getSimpleJavaTypeOrNull(type, true);
    }

    public static String getSimpleJavaTypeOrNull(Class type, boolean considerLists) {
        return getSimpleJavaTypeOrNull(type.getSimpleName(), considerLists);
    }

    public static String getSimpleJavaTypeOrNull(String type) {
        return getSimpleJavaTypeOrNull(type, true);
    }

    public static String getSimpleJavaTypeOrNull(String type, boolean considerLists) {
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
        final Collection<Object> objectClasses = new ArrayList(Arrays.asList(args));
        CollectionUtils.transform(objectClasses, CLASS_FROM_OBJECT_TRANSFORMER);
        Collection<Method> methods = CollectionUtils.select(getAllMethodsIncludingParents(object.getClass()), method -> methodNameEqualsToPredicate(method, methodName));

        Method method = IterableUtils.find(methods, (Predicate) o -> {
            Method innerMethod = (Method) o;

            final Collection<Class> parameterClasses = Arrays.asList(innerMethod.getParameterTypes());
            CollectionUtils.transform(parameterClasses, (Transformer) o1 -> getClassFromPrimitive((Class) o1));

            return (innerMethod.getName().toUpperCase().equals((methodName).toUpperCase())) & (Arrays.equals(
                    parameterClasses.toArray(),
                    objectClasses.toArray()));
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

    public boolean isCollectionImplementation(String fullyQulifiedClassName) {
        try {
            Class clazz = Class.forName(fullyQulifiedClassName);
            return isCollectionImplementation(clazz);
        } catch (Throwable ignored) {
        }
        return false;

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

    public static boolean isList(String type) {
        try {
            return type.equals("List") || type.startsWith("List<") || type.startsWith("java.util.List<") || type.equals(
                    "Collection") || type.startsWith("Collection<") || type.startsWith("java.util.Collection<");
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean isNullOrEmpty(Object object) {
        //TODO AMM 30072010: Este metodo explota para todos los tipos Map o Collection. Quizas sea bueno preguntar si
        // el la clase al que pertenece el objeto es una instancia de Collection o Map e invocar el método "isEmpty"
        boolean result = false;
        if (object != null) {
            Class clazz = object.getClass();
            if (isPrimitive(clazz)) {
                result = false;
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

    public boolean isMapImplementation(Class clazz) {
        return clazz != null && Map.class.isAssignableFrom(clazz);
    }

    private boolean methodIsNotContainedIn(Method method, Collection<String> excludeMethods) {
        return !methodIsContainedIn(method, excludeMethods);
    }

    private boolean methodIsContainedIn(Method method, Collection<String> excludeMethods) {
        return IterableUtils.find(excludeMethods,
                methodName -> methodNameEqualsToPredicate(method, methodName)) != null;
    }

    private boolean methodNameIsNotContainedIn(String methodName, Collection<String> excludeMethods) {
        return !methodNameIsContainedIn(methodName, excludeMethods);
    }

    public LinkedList<Class> recursivelyGetAllInterfaces(Class clazz) {
        if (clazz == null || Object.class.getName().equals(clazz.getClass().getName()) || isCollectionImplementation(
                clazz) || isMapImplementation(clazz) || getFullyQualifiedJavaTypeOrNull(clazz.getSimpleName(),
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

/*    public String toString(Object object) {
        return formatObjectValues(object, true, true, StringUtils.EMPTY, false);
    }*/

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

}
