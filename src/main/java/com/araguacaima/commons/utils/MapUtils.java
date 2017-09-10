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

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MapUtils {

    public static final int EVALUATE_BOTH_KEY_AND_VALUE = 0;
    public static final int DEFAULT_EVALUATION_TYPE = EVALUATE_BOTH_KEY_AND_VALUE;
    public static final int EVALUATE_BOTH_KEY_OR_VALUE = 3;
    public static final int EVALUATE_JUST_KEY = 1;
    public static final int EVALUATE_JUST_VALUE = 2;
    public static final StringKeyHashMapUtil stringKeyHashMapUtil = new StringKeyHashMapUtil();
    private static final Logger log = LoggerFactory.getLogger(MapUtils.class);

    /**
     * <code>MapUtils</code> should not normally be instantiated.
     */
    private MapUtils() {
    }

    public static Map find(Map map, Predicate keyPredicate, Predicate valuePredicate, int evaluationType) {
        Map newMap = new HashMap();
        Object key;
        Object value;
        for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
            java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
            key = entry.getKey();
            value = entry.getValue();
            if (existsInMap(key, value, keyPredicate, valuePredicate, evaluationType)) {
                newMap.put(key, value);
                break;
            }
        }
        return newMap;
    }

    private static boolean existsInMap(Object key,
                                       Object value,
                                       Predicate keyPredicate,
                                       Predicate valuePredicate,
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

    public static Object findObject(Map map, Predicate keyPredicate, Predicate valuePredicate, int evaluationType) {
        Object key;
        Object value = null;
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            key = entry.getKey();
            value = entry.getValue();
            if (existsInMap(key, value, keyPredicate, valuePredicate, evaluationType)) {
                break;
            }
        }
        return value;
    }

    public static Map/*<String, String>*/ fromProperties(final Properties properties) {
        final Map/*<String, String>*/ map = new HashMap/*<String, String>*/();
        IterableUtils.forEach(properties.keySet(), new Closure() {
            public void execute(Object o) {
                Object key = o;
                Object value = properties.get(key);
                if (key != null) {
                    key = String.valueOf(o);
                    if (value != null) {
                        value = String.valueOf(value);
                    } else {
                        value = StringUtils.EMPTY;
                    }
                    map.put(/*(String)*/ key, /*(String)*/ value);
                }
            }
        });
        return map;
    }

    public static StringKeyHashMapUtil getStringKeyHashMapUtil() {
        return stringKeyHashMapUtil;
    }

    public static boolean isEmpty(Map map) {
        return org.apache.commons.collections4.MapUtils.isEmpty(map);
    }

    public static boolean isNotEmpty(Map map) {
        return org.apache.commons.collections4.MapUtils.isNotEmpty(map);
    }

    public static Map/*<String, String>*/ toMap(final Properties properties) {
        final Map map = new HashMap();
        IterableUtils.forEach(properties.keySet(), new Closure() {
            public void execute(Object o) {
                Object key = o;
                Object value = properties.get(key);
                if (key != null) {
                    key = String.valueOf(o);
                    if (value != null) {
                        value = String.valueOf(value);
                    } else {
                        value = StringUtils.EMPTY;
                    }
                    map.put((String) key, (String) value);
                }
            }
        });
        return map;
    }

    public static Properties toProperties(final Map/*<String, String>*/ map) {
        if (map instanceof Properties) {
            return (Properties) map;
        }
        final Properties properties = new Properties();
        IterableUtils.forEach(map.keySet(), new Closure() {
            public void execute(Object o) {
                Object key = o;
                Object value = map.get(key);
                if (key != null) {
                    key = String.valueOf(o);
                    if (value != null) {
                        value = String.valueOf(value);
                    } else {
                        value = StringUtils.EMPTY;
                    }
                    properties.setProperty((String) key, (String) value);
                }
            }
        });
        return properties;
    }

    public void removeAll(final Map<?, ?> map, Collection<?> keys) {
        IterableUtils.forEach(keys, map::remove);
    }

    public Map select(Map<Object, Object> map, Predicate keyPredicate, Predicate valuePredicate) {
        Map<Object, Object> newMap = new HashMap<>(map);
        Object key;
        Object value;
        for (Iterator it = map.entrySet().iterator(); it.hasNext(); removeFromMap(key,
                value,
                keyPredicate,
                valuePredicate,
                newMap)) {
            java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
            key = entry.getKey();
            value = entry.getValue();
        }
        return newMap;
    }

    private void removeFromMap(Object key, Object value, Predicate keyPredicate, Predicate valuePredicate, Map map) {
        if (keyPredicate != null && !keyPredicate.evaluate(key)) {
            map.remove(key);
            return;
        }
        if (valuePredicate != null && !valuePredicate.evaluate(value)) {
            map.remove(key);
        }
    }

    public Map transform(Map map, Transformer keyTransformer, Transformer valueTransformer) {
        Map newMap = new HashMap(map);
        Object key;
        Object value;
        for (Iterator it = map.entrySet().iterator(); it.hasNext(); appendIntoMap(key,
                value,
                keyTransformer,
                valueTransformer,
                newMap)) {
            java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
            key = entry.getKey();
            value = entry.getValue();
        }
        return newMap;
    }

    private void appendIntoMap(Object key,
                               Object value,
                               Transformer keyTransformer,
                               Transformer valueTransformer,
                               Map<Object, Object> map) {
        map.remove(key);
        insertIntoMap(key, value, keyTransformer, valueTransformer, map);

    }

    private void insertIntoMap(Object key,
                               Object value,
                               Transformer keyTransformer,
                               Transformer valueTransformer,
                               Map<Object, Object> map) {
        Object transformedKey = null;
        Object transformedValue = null;
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
                String key = o;
                if (key.contains(substring)) {
                    return this.get(key);
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
                String substring = o;
                if (key.contains(substring)) {
                    return this.get(substring);
                }
            }
            return null;
        }

    }

}
