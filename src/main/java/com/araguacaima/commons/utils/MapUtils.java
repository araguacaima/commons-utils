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
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
public class MapUtils {

    public static final int EVALUATE_BOTH_KEY_AND_VALUE = 0;
    public static final int DEFAULT_EVALUATION_TYPE = EVALUATE_BOTH_KEY_AND_VALUE;
    public static final int EVALUATE_BOTH_KEY_OR_VALUE = 3;
    public static final int EVALUATE_JUST_KEY = 1;
    public static final int EVALUATE_JUST_VALUE = 2;
    private final Logger log = LoggerFactory.getLogger(MapUtils.class);

    /**
     * <code>MapUtils</code> should not normally be instantiated.
     */
    private MapUtils() {
    }

    public Map<Object, Object> find(Map map, Predicate keyPredicate, Predicate valuePredicate, int evaluationType) {
        Map<Object, Object> newMap = new HashMap<>();
        Object key;
        Object value;
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            key = entry.getKey();
            value = entry.getValue();
            if (existsInMap(key, value, keyPredicate, valuePredicate, evaluationType)) {
                newMap.put(key, value);
                break;
            }
        }
        return newMap;
    }

    private boolean existsInMap(Object key,
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
            log.debug("Error evaluating predicates. " + e.getMessage());
        }
        return result;
    }

    public Object findObject(Map map, Predicate keyPredicate, Predicate valuePredicate, int evaluationType) {

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

    public static boolean isEmpty(Map map) {
        return org.apache.commons.collections4.MapUtils.isEmpty(map);
    }

    public static boolean isNotEmpty(Map map) {
       return org.apache.commons.collections4.MapUtils.isNotEmpty(map);
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

    public Map transform(Map<Object, Object> map, Transformer keyTransformer, Transformer valueTransformer) {
        Map<Object, Object> newMap = new HashMap<>(map);
        Object key;
        Object value;
        for (Iterator<Map.Entry<Object, Object>> it = map.entrySet().iterator(); it.hasNext(); appendIntoMap(key,
                value,
                keyTransformer,
                valueTransformer,
                newMap)) {
            Map.Entry<Object, Object> entry = it.next();
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

    public class StringKeyHashMapUtil extends HashMap<String, Object> {

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
