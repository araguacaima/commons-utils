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

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
@Component
public class EnumsUtils<T> {

    private static final Logger log = LoggerFactory.getLogger(EnumsUtils.class);
    private final Pattern getterPattern = Pattern.compile("(?:get|is)\\p{Upper}+");
    private final RandomDataGenerator randomData = new RandomDataGenerator();

    public EnumsUtils() {

    }

    public T getAnyEnumElement(Class<T> enumType) {
        T[] constants = enumType.getEnumConstants();
        if (constants != null) {
            return constants[randomData.nextInt(0, constants.length - 1)];
        }
        return null;
    }

    /**
     * Returns the enum constant of the specified enum type with the
     * specified name.  The name must match exactly an identifier used
     * to declare an enum constant in this type.  (Extraneous whitespace
     * characters are not permitted.)
     * <br>
     * Note that for a particular enum type {@code T}, the
     * implicitly declared {@code public  T valueOf(String)}
     * method on that enum may be used instead of this method to map
     * from a name to the corresponding enum constant.  All the
     * constants of an enum type can be obtained by calling the
     * implicit {@code public  T[] values()} method of that
     * type.
     *
     * @param enumType the {@code Class} object of the enum type from which
     *                 to return a constant
     * @param name     the name of the constant to return
     * @return the enum constant of the specified enum type with the
     * specified name
     * @throws IllegalArgumentException if the specified enum type has
     *                                  no constant with the specified name, or the specified
     *                                  class object does not represent an enum type
     * @throws NullPointerException     if {@code enumType} or {@code name}
     *                                  is null
     */
    public T getEnum(Class<T> enumType, final String name) {
        T[] constants = enumType.getEnumConstants();
        T result = null;
        if (constants != null) {
            List<T> enumConstants = Arrays.asList(constants);
            result = IterableUtils.find(enumConstants, object -> object.toString().equals(name));
        }
        if (result != null)
            return result;
        if (name == null)
            throw new NullPointerException("Name is null");
        throw new IllegalArgumentException("No enum constant " + enumType.getCanonicalName() + "." + name);
    }

    public Object getField(Class<T> clazz, String name, String fieldName) {
        if (Enum.class.isAssignableFrom(clazz)) {
            try {
                return getField((Enum) getEnum(clazz, name), fieldName);
            } catch (Throwable t) {
                log.error(t.getMessage());
            }
        }
        return null;
    }

    public Object getField(Enum anEnum, String fieldName) {

        try {
            final Method value = anEnum.getClass().getMethod(fieldName);
            java.security.AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                value.setAccessible(true);
                return null;
            });
            return value.invoke(anEnum);
        } catch (NoSuchMethodException e) {
            Matcher matcher = getterPattern.matcher(fieldName);
            if (!matcher.find()) {
                fieldName = "get" + StringUtils.capitalize(fieldName);
            }
            try {
                return getField(anEnum, fieldName);
            } catch (Throwable ignored) {
            }
            return anEnum.toString();
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error(e.getMessage());
        }
        return anEnum.name();
    }

    public List<String> getNamesList(Class enumerateClazz) {
        if (Enum.class.isAssignableFrom(enumerateClazz)) {
            final List<String> result = new ArrayList<>();
            Object[] constants = enumerateClazz.getEnumConstants();
            if (constants != null) {
                List enumConstants = Arrays.asList(constants);
                IterableUtils.forEach(enumConstants, o -> result.add(((Enum) o).name()));
            }
            return result;

        } else {
            throw new IllegalArgumentException("Class is not an Enum type");
        }
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    public String getStringValue(Enum anEnum) {
        if (Enum.class.isAssignableFrom(anEnum.getClass())) {
            try {
                final Method value = anEnum.getClass().getMethod("value");
                java.security.AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    value.setAccessible(true);
                    return null;
                });
                return (String) value.invoke(anEnum);
            } catch (NoSuchMethodException e) {
                return anEnum.toString();
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error(e.getMessage());
            }
            return anEnum.name();
        }
        return null;
    }

    public List<String> getValuesList(Class enumerateClazz) {
        if (Enum.class.isAssignableFrom(enumerateClazz)) {
            final List<String> result = new ArrayList<>();
            Object[] constants = enumerateClazz.getEnumConstants();
            if (constants != null) {
                List enumConstants = Arrays.asList(constants);
                IterableUtils.forEach(enumConstants, o -> {
                    try {
                        final Method value = o.getClass().getMethod("value");
                        java.security.AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                            value.setAccessible(true);
                            return null;
                        });
                        result.add((String) value.invoke(o));
                    } catch (NoSuchMethodException e) {
                        result.add(o.toString());
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        log.error(e.getMessage());
                    }

                });
            }
            return result;

        } else {
            throw new IllegalArgumentException("Class is not an Enum type");
        }
    }

}
