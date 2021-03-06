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
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


public class MapUtils {

    public static final int EVALUATE_BOTH_KEY_AND_VALUE = 0;
    public static final int DEFAULT_EVALUATION_TYPE = EVALUATE_BOTH_KEY_AND_VALUE;
    public static final int EVALUATE_BOTH_KEY_OR_VALUE = 3;
    public static final int EVALUATE_JUST_KEY = 1;
    public static final int EVALUATE_JUST_VALUE = 2;
    public static final StringKeyHashMapUtil stringKeyHashMapUtil = new StringKeyHashMapUtil();
    private static final Logger log = LoggerFactory.getLogger(MapUtils.class);

    private static final MapUtils INSTANCE = new MapUtils();

    private MapUtils() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static MapUtils getInstance() {
        return INSTANCE;
    }

    public static Map<String, String> fromProperties(final Properties properties) {
        final Map<String, String> map = new HashMap<>();
        IterableUtils.forEach(properties.keySet(), key -> {
            if (key != null) {
                Object value = properties.get(key);
                if (value != null) {
                    value = String.valueOf(value);
                } else {
                    value = StringUtils.EMPTY;
                }
                map.put((String) key, (String) value);
            }
        });
        return map;
    }

    public static boolean isEmpty(Map map) {
        return org.apache.commons.collections4.MapUtils.isEmpty(map);
    }

    public static boolean isNotEmpty(Map<java.io.File, Set<java.io.File>> map) {
        return org.apache.commons.collections4.MapUtils.isNotEmpty(map);
    }

    public static <K, V> Map<K, V> clone(Map<K, V> original) {
        return original.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cannot clone instance of this class");
    }

    public <E, T> Map find(Map<E, T> map, Predicate<E> keyPredicate, Predicate<T> valuePredicate, int evaluationType) {
        Map<E, T> newMap = new HashMap<>();
        E key;
        T value;
        for (Map.Entry<E, T> entry : map.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            if (existsInMap(key, value, keyPredicate, valuePredicate, evaluationType)) {
                newMap.put(key, value);
                break;
            }
        }
        return newMap;
    }

    public <E, T> boolean existsInMap(E key,
                                      T value,
                                      Predicate<E> keyPredicate,
                                      Predicate<T> valuePredicate,
                                      int evaluationType) {
        boolean result = false;
        try {
            switch (evaluationType) {
                case EVALUATE_JUST_KEY:
                    result = keyPredicate != null && keyPredicate.evaluate(key);
                    break;
                case EVALUATE_JUST_VALUE:
                    result = valuePredicate != null && valuePredicate.evaluate(value);
                    break;
                case EVALUATE_BOTH_KEY_AND_VALUE:
                    result = keyPredicate != null && keyPredicate.evaluate(key) && valuePredicate != null &&
                            valuePredicate.evaluate(
                                    value);
                    break;
                case EVALUATE_BOTH_KEY_OR_VALUE:
                    result = keyPredicate != null && keyPredicate.evaluate(key) || valuePredicate != null &&
                            valuePredicate.evaluate(
                                    value);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.debug("Error evaluating predicates. " + e.getMessage(), 1);
        }
        return result;
    }

    public <E, T> Object findObject(Map<E, T> map,
                                    Predicate<E> keyPredicate,
                                    Predicate<T> valuePredicate,
                                    int evaluationType) {
        E key;
        T value = null;
        for (Map.Entry<E, T> entry : map.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            if (existsInMap(key, value, keyPredicate, valuePredicate, evaluationType)) {
                break;
            }
        }
        return value;
    }

    public void removeAll(final Map<?, ?> map, Collection<?> keys) {
        IterableUtils.forEach(keys, map::remove);
    }

    public <E, F> Map<?, ?> select(Map<E, F> map, Predicate<E> keyPredicate, Predicate<F> valuePredicate) {
        Map<Object, Object> newMap = new HashMap<>(map);
        E key;
        F value;
        for (Iterator<Map.Entry<E, F>> it = map.entrySet().iterator(); it.hasNext(); removeFromMap(key,
                value,
                keyPredicate,
                valuePredicate,
                newMap)) {
            java.util.Map.Entry<E, F> entry = it.next();
            key = entry.getKey();
            value = entry.getValue();
        }
        return newMap;
    }

    private <E, T> void removeFromMap(E key, T value, Predicate<E> keyPredicate, Predicate<T> valuePredicate, Map<Object, Object> map) {
        if (keyPredicate != null && !keyPredicate.evaluate(key)) {
            map.remove(key);
            return;
        }
        if (valuePredicate != null && !valuePredicate.evaluate(value)) {
            map.remove(key);
        }
    }

    public Map<String, String> toMap(final Properties properties) {
        final Map<String, String> map = new HashMap<>();
        IterableUtils.forEach(properties.keySet(), key -> {
            if (key != null) {
                Object value = properties.get(key);
                key = String.valueOf(key);
                if (value != null) {
                    value = String.valueOf(value);
                } else {
                    value = StringUtils.EMPTY;
                }
                map.put((String) key, (String) value);
            }
        });
        return map;
    }

    public Properties toProperties(final Map<String, String> map) {
        final Properties properties = new Properties();
        IterableUtils.forEach(map.keySet(), key -> {
            if (key != null) {
                String value = map.get(key);
                if (value != null) {
                } else {
                    value = StringUtils.EMPTY;
                }
                properties.setProperty(key, value);
            }
        });
        return properties;
    }

    public <E, F, G, H> Map<?, ?> transform(Map<E, F> map,
                                            Transformer<E, G> keyTransformer,
                                            Transformer<F, H> valueTransformer) {
        Map<Object, Object> newMap = new HashMap<>(map);
        E key;
        F value;
        for (Iterator<Map.Entry<E, F>> it = map.entrySet().iterator(); it.hasNext(); appendIntoMap(key,
                value,
                keyTransformer,
                valueTransformer,
                newMap)) {
            Map.Entry<E, F> entry = it.next();
            key = entry.getKey();
            value = entry.getValue();
        }
        return newMap;
    }

    private <E, F, G, H> void appendIntoMap(E key,
                                            F value,
                                            Transformer<E, G> keyTransformer,
                                            Transformer<F, H> valueTransformer,
                                            Map<Object, Object> map) {
        map.remove(key);
        insertIntoMap(key, value, keyTransformer, valueTransformer, map);

    }

    private <E, F, G, H> void insertIntoMap(E key,
                                            F value,
                                            Transformer<E, G> keyTransformer,
                                            Transformer<F, H> valueTransformer,
                                            Map<Object, Object> map) {
        G transformedKey = null;
        H transformedValue = null;
        if (keyTransformer != null) {
            transformedKey = keyTransformer.transform(key);
        }
        if (valueTransformer != null) {
            transformedValue = valueTransformer.transform(value);
        }
        if (transformedKey != null) {
            map.put(transformedKey, transformedValue);
        }
    }

    public Map traverseAndCreateNew(Map originMap) throws IllegalAccessException, InstantiationException {
        if (originMap == null) {
            return null;
        }
        Map map = originMap.getClass().newInstance();
        for (Object key : originMap.keySet()) {
            Object value = originMap.get(key);
            if (Map.class.isAssignableFrom(value.getClass())) {
                map.put(key, traverseAndCreateNew((Map) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    public LinkedHashMap<String, LinkedHashMap> createKeysFromPackageName(String key) {
        if (StringUtils.isBlank(key)) {
            return new LinkedHashMap<>();
        }
        String entry = key.split("\\.")[0];
        String remaining = key.replaceFirst(entry, StringUtils.EMPTY);
        if (remaining.startsWith(".")) {
            remaining = remaining.substring(1);
        }
        LinkedHashMap<String, LinkedHashMap> map = new LinkedHashMap<>();
        LinkedHashMap<String, LinkedHashMap> value = new LinkedHashMap<>();
        map.put(entry, createKeysFromPackageName(remaining, value));
        return map;
    }

    public LinkedHashMap<String, LinkedHashMap> createKeysFromPackageName(String key, LinkedHashMap<String, LinkedHashMap> parentMap) {
        if (StringUtils.isBlank(key)) {
            return new LinkedHashMap<>();
        }
        String entry = key.split("\\.")[0];
        String remaining = key.replaceFirst(entry, StringUtils.EMPTY);
        if (remaining.startsWith(".")) {
            remaining = remaining.substring(1);
        }
        LinkedHashMap<String, LinkedHashMap> map = (LinkedHashMap<String, LinkedHashMap>) parentMap.get(entry);
        if (map == null) {
            map = new LinkedHashMap<>();
            LinkedHashMap<String, LinkedHashMap> value = new LinkedHashMap<>();
            map.put(entry, createKeysFromPackageName(remaining, value));
        } else {
            if (!key.equals(entry)) {
                LinkedHashMap<String, LinkedHashMap> map1 = createKeysFromPackageName(remaining, map);
                if (!map1.equals(map)) {
                    if (!remaining.contains(".")) {
                        map.putAll(map1);
                        return parentMap;
                    } else {
                        map.put(entry, map1);
                    }
                } else {
                    return parentMap;
                }
            } else {
                return parentMap;
            }
        }
        return map;
    }

    public Map getLastValueFromPackageName(String key, Map parentMap) {
        if (StringUtils.isBlank(key)) {
            return parentMap;
        }
        String entry = key.split("\\.")[0];
        Map map = (Map) parentMap.get(entry);
        if (map == null) {
            return null;
        } else {
            String remaining = key.substring(entry.length());
            if (remaining.startsWith(".")) {
                remaining = remaining.substring(1);
            }
            return getLastValueFromPackageName(remaining, map);
        }
    }

    public static class StringKeyHashMapUtil extends HashMap<String, Object> {

        private final long serialVersionUID = -8603163772769655779L;

        StringKeyHashMapUtil() {
            super();
        }

        /**
         * Returns <tt>true</tt> if this map contains a mapping for the
         * specified key, that match with the incoming substring.
         *
         * @param substring The substring in the key whose presence in this map is to be tested
         * @return <tt>true</tt> if this map contains a mapping for the specified substring
         * key.
         */
        public boolean containsKeySubstring(String substring) {
            for (String o : this.keySet()) {
                if (o.contains(substring)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns <tt>true</tt> if this map contains a mapping for the
         * specified key, that match with the incoming substring.
         *
         * @param substring The substring in the key whose presence in this map is to be tested
         * @return <tt>true</tt> if this map contains a mapping for the specified substring
         * key.
         */
        public Object getKeySubstringValue(String substring) {
            for (String o : this.keySet()) {
                if (o.contains(substring)) {
                    return this.get(o);
                }
            }
            return null;
        }

        /**
         * Returns <tt>true</tt> if this map contains a mapping for the
         * specified substring, that match with the incoming key.
         *
         * @param key The key whose presence in this map is to be tested
         * @return <tt>true</tt> if this map contains a mapping for the specified substring
         * key.
         */
        public Object getSubstringKeyValue(String key) {
            for (String o : this.keySet()) {
                if (key.contains(o)) {
                    return this.get(o);
                }
            }
            return null;
        }

    }
}
