package com.araguacaima.commons.utils.parser;

/*
  Created by Alejandro on 20/11/2014.
  <br>
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at
  <br>
  http://www.apache.org/licenses/LICENSE-2.0
  <br>
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
  <br>
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at
  <br>
  http://www.apache.org/licenses/LICENSE-2.0
  <br>
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
  <br>
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements. See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership. The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License. You may obtain a copy of the License at
  <br>
  http://www.apache.org/licenses/LICENSE-2.0
  <br>
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied. See the License for the
  specific language governing permissions and limitations
  under the License.
 */

import org.apache.commons.collections4.Predicate;
import org.apache.cxf.jaxrs.ext.search.*;

import java.util.*;

/**
 * Simple search condition comparing primitive objects or complex object by its
 * getters. For details see {@link #isMet(Object)} description.
 *
 * @param <T> type of search condition.
 */
public class ExtendedSearchCondition<T> implements SearchCondition<T> {

    private static final Set<ConditionType> supportedTypes = new HashSet<>();

    static {
        supportedTypes.add(ConditionType.EQUALS);
        supportedTypes.add(ConditionType.NOT_EQUALS);
        supportedTypes.add(ConditionType.GREATER_THAN);
        supportedTypes.add(ConditionType.GREATER_OR_EQUALS);
        supportedTypes.add(ConditionType.LESS_THAN);
        supportedTypes.add(ConditionType.LESS_OR_EQUALS);
        supportedTypes.add(ConditionType.CUSTOM);
    }

    private final ConditionType joiningType = ConditionType.AND;
    private final T condition;

    private final List<SearchCondition<T>> scts;
    private String packageBase;

    /**
     * Creates search condition with same operator (equality, inequality)
     * applied in all comparison; see {@link #isMet(Object)} for details of
     * comparison.
     *
     * @param cType       shared condition type
     * @param condition   template object
     * @param packageBase Package base
     */
    public ExtendedSearchCondition(final ConditionType cType, final T condition, final String packageBase) {
        if (cType == null) {
            throw new IllegalArgumentException("cType is null");
        }
        if (condition == null) {
            throw new IllegalArgumentException("condition is null");
        }
        if (!supportedTypes.contains(cType)) {
            throw new IllegalArgumentException("unsupported condition type: " + cType.name());
        }
        this.condition = condition;
        scts = createConditions(null, cType);
        this.packageBase = packageBase;
    }

    /**
     * Creates search condition with different operators (equality, inequality
     * etc) specified for each getter; see {@link #isMet(Object)} for details of
     * comparison. Cannot be used for primitive T type due to per-getter
     * comparison strategy.
     *
     * @param getters2operators getters names and operators to be used with them during
     *                          comparison
     * @param condition         template object
     */
    public ExtendedSearchCondition(final Map<String, ConditionType> getters2operators, final T condition) {
        if (getters2operators == null) {
            throw new IllegalArgumentException("getters2operators is null");
        }
        if (condition == null) {
            throw new IllegalArgumentException("condition is null");
        }
        if (isPrimitive(condition)) {
            throw new IllegalArgumentException("mapped operators strategy is " + "not supported for primitive type "
                    + condition.getClass().getName());
        }
        this.condition = condition;
        for (final ConditionType ct : getters2operators.values()) {
            if (!supportedTypes.contains(ct)) {
                throw new IllegalArgumentException("unsupported condition type: " + ct.name());
            }
        }
        scts = createConditions(getters2operators, null);
    }

    public T getCondition() {
        return condition;
    }

    /**
     * {@inheritDoc}
     * <br>
     * When constructor with map is used it returns null.
     */
    public ConditionType getConditionType() {
        if (scts.size() > 1) {
            return joiningType;
        } else {
            return scts.get(0).getStatement().getCondition();
        }
    }

    public List<SearchCondition<T>> getSearchConditions() {
        if (scts.size() > 1) {
            return Collections.unmodifiableList(scts);
        } else {
            return null;
        }
    }

    private List<SearchCondition<T>> createConditions(final Map<String, ConditionType> getters2operators, final ConditionType sharedType) {
        if (isPrimitive(condition)) {
            return Collections.singletonList(new PrimitiveSearchCondition<>(null, condition, sharedType, condition));
        } else {
            final List<SearchCondition<T>> list = new ArrayList<>();
            final Map<String, Object> get2val = getGettersAndValues(condition);

            for (final String getter : get2val.keySet()) {
                ConditionType conditionType = null;
                if (getters2operators != null) {
                    conditionType = getters2operators.get(getter);
                    if (conditionType == null) {
                        conditionType = find(getters2operators, o -> o != null && ((String) o).startsWith(getter + "."));
                    }
                }
                final ConditionType ct = getters2operators == null ? sharedType : conditionType;
                if (ct == null) {
                    continue;
                }
                final Object rval = get2val.get(getter);
                if (rval == null) {
                    continue;
                }
                list.add(new PrimitiveSearchCondition<>(getter, rval, ct, condition));

            }
            if (list.isEmpty()) {
                throw new IllegalStateException("This search condition is empty and can not be used");
            }
            return list;
        }
    }

    private ConditionType find(final Map<String, ConditionType> getters2operators, final Predicate keyMapCriteria) {
        for (final String key : getters2operators.keySet()) {
            if (keyMapCriteria.evaluate(key)) {
                return getters2operators.get(key);
            }
        }
        return null;
    }

    /**
     * Compares given object against template condition object.
     * <br>
     * For primitive type T like String, Number (precisely, from type T located
     * in subpackage of "java.lang.*") given object is directly compared with
     * template object. Comparison for {@link ConditionType#EQUALS} requires
     * correct implementation of {@link Object#equals(Object)}, using
     * inequalities requires type T implementing {@link Comparable}.
     * <br>
     * For other types comparison of given object against template object is
     * done using these <b>getters</b>; returned "is met" value is
     * <b>conjunction ('and' operator)</b> of comparisons per each getter.
     * Getters of template object that return null or throw exception are not
     * used in comparison, in extreme if all getters are excluded it means every
     * given pojo object matches. If
     * {@link #ExtendedSearchCondition(ConditionType, Object, String) constructor with
     * shared operator} was used, then getters are compared using the same
     * operator. If {@link #ExtendedSearchCondition(Map, Object) constructor
     * with map of operators} was used then for every getter specified operator
     * is used (getters for missing mapping are ignored). The way that
     * comparison per getter is done depends on operator type per getter -
     * comparison for {@link ConditionType#EQUALS} requires correct
     * implementation of {@link Object#equals(Object)}, using inequalities
     * requires that getter type implements {@link Comparable}.
     * <br>
     * For equality comparison and String type in template object (either being
     * primitive or getter from complex type) it is allowed to used asterisk at
     * the beginning or at the end of text as wild card (zero or more of any
     * characters) e.g. "foo*", "*foo" or "*foo*". Inner asterisks are not
     * interpreted as wild cards.
     * <br>
     * <b>Example:</b>
     * <br>
     *
     * <pre>
     * SimpleSearchCondition&lt;Integer&gt; ssc = new SimpleSearchCondition&lt;Integer&gt;(
     *   ConditionType.GREATER_THAN, 10);
     * ssc.isMet(20);
     * // true since 20&gt;10
     *
     * class Entity {
     *   public String getName() {...
     *   public int getLevel() {...
     *   public String getMessage() {...
     * }
     *
     * Entity template = new Entity("bbb", 10, null);
     * ssc = new SimpleSearchCondition&lt;Entity&gt;(
     *   ConditionType.GREATER_THAN, template);
     *
     * ssc.isMet(new Entity("aaa", 20, "some mesage"));
     * // false: is not met, expression '"aaa"&gt;"bbb" and 20&gt;10' is not true
     * // since "aaa" is not greater than "bbb"; not that message is null in template hence ingored
     *
     * ssc.isMet(new Entity("ccc", 30, "other message"));
     * // true: is met, expression '"ccc"&gt;"bbb" and 30&gt;10' is true
     *
     * Map&lt;String,ConditionType&gt; map;
     * map.put("name", ConditionType.EQUALS);
     * map.put("level", ConditionType.GREATER_THAN);
     * ssc = new SimpleSearchCondition&lt;Entity&gt;(
     *   ConditionType.GREATER_THAN, template);
     *
     * ssc.isMet(new Entity("ccc", 30, "other message"));
     * // false due to expression '"aaa"=="ccc" and 30&gt;10"' (note different operators)
     *
     * @param pojo Object.
     * </pre>
     */
    public boolean isMet(final T pojo) {
        for (final SearchCondition<T> sc : scts) {
            if (!sc.isMet(pojo)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates cache of getters from template (condition) object and its values
     * returned during one-pass invocation. Method isMet() will use its keys to
     * introspect getters of passed pojo object, and values from map in
     * comparison.
     *
     * @param condition Condition
     * @return map
     */
    private Map<String, Object> getGettersAndValues(final T condition) {

        final Map<String, Object> getters2values = new HashMap<>();
        final Beanspector<T>
                beanspector = new Beanspector<>(condition, packageBase);
        for (final String getter : beanspector.getGettersNames()) {
            final Object value = getValue(beanspector, getter, condition);
            getters2values.put(getter, value);
        }
        // we do not need compare class objects
        getters2values.keySet().remove("class");
        return getters2values;
    }

    private Object getValue(final com.araguacaima.commons.utils.parser.Beanspector<T> beanspector, final String getter, final T pojo) {
        try {
            return beanspector.swap(pojo).getValue(getter);
        } catch (final Throwable e) {
            return null;
        }
    }

    private boolean isPrimitive(final T pojo) {
        return pojo.getClass().getName().startsWith("java.lang");
    }

    public List<T> findAll(final Collection<T> pojos) {
        final List<T> result = new ArrayList<>();
        for (final T pojo : pojos) {
            if (isMet(pojo)) {
                result.add(pojo);
            }
        }
        return result;
    }

    public String toSQL(final String table, final String... columns) {
        if (isPrimitive(condition)) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();

        if (table != null) {
            SearchUtils.startSqlQuery(sb, table, columns);
        }

        boolean first = true;
        for (final SearchCondition<T> sc : scts) {
            final PrimitiveStatement ps = sc.getStatement();
            if (ps.getPropery() == null) {
                continue;
            }
            if (!first) {
                sb.append(" ").append(joiningType.toString()).append(" ");
            } else {
                first = false;
            }

            sb.append(sc.toSQL(null));
        }
        return sb.toString();
    }

    public PrimitiveStatement getStatement() {
        if (scts.size() == 1) {
            return scts.get(0).getStatement();
        } else {
            return null;
        }
    }
}
