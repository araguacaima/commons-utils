package com.araguacaima.commons.utils.parser;

/*
  Created by Alejandro on 20/11/2014.
 */

import com.araguacaima.commons.utils.EnumsUtils;
import com.araguacaima.commons.utils.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.search.AndSearchCondition;
import org.apache.cxf.jaxrs.ext.search.ConditionType;
import org.apache.cxf.jaxrs.ext.search.OrSearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.utils.InjectionUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses <a
 * href="http://tools.ietf.org/html/draft-nottingham-atompub-fiql-00">FIQL</a>
 * expression to construct
 * {@link org.apache.cxf.jaxrs.ext.search.SearchCondition} structure. Since this
 * class operates on Java type T, not on XML structures "selectors" part of
 * specification is not applicable; instead selectors describes getters of type
 * T used as search condition type (see
 * {@link ExtendedSearchCondition#isMet(Object)} for details.
 *
 * @param <T> type of search condition.
 */
public class ExtendableFiqlParser<T> {

    public static final String OR = ",";
    public static final String AND = ";";

    public static final String GT = "=gt=";
    public static final String GE = "=ge=";
    public static final String LT = "=lt=";
    public static final String LE = "=le=";
    public static final String EQ = "==";
    public static final String NEQ = "!=";

    private static final String FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss:SSSSSS";
    private static final ReflectionUtils reflectionUtils = new ReflectionUtils(null);
    private static final EnumsUtils enumsUtils = new EnumsUtils();
    private static final Map<String, ConditionType> operatorsMap;

    static {
        operatorsMap = new HashMap<>();
        operatorsMap.put(GT, ConditionType.GREATER_THAN);
        operatorsMap.put(GE, ConditionType.GREATER_OR_EQUALS);
        operatorsMap.put(LT, ConditionType.LESS_THAN);
        operatorsMap.put(LE, ConditionType.LESS_OR_EQUALS);
        operatorsMap.put(EQ, ConditionType.EQUALS);
        operatorsMap.put(NEQ, ConditionType.NOT_EQUALS);
    }

    private final com.araguacaima.commons.utils.parser.Beanspector<T> beanspector;

    /**
     * Creates FIQL parser.
     *
     * @param tclass      - class of T used to create condition objects in built syntax
     *                    tree. Class T must have accessible no-arg constructor and
     *                    complementary setters to these used in FIQL expressions.
     * @param packageBase Package base.
     */
    public ExtendableFiqlParser(final Class<T> tclass, String packageBase) {
        this(tclass, tclass.getClassLoader(), packageBase);
    }

    public ExtendableFiqlParser(Class<T> tClass, ClassLoader classLoader, String packageBase) {
        beanspector = new com.araguacaima.commons.utils.parser.Beanspector<>(tClass, classLoader, packageBase);
    }

    public static void addOperator(final String operator, final ConditionType conditionType) {
        if (operatorsMap.get(operator) == null) {
            operatorsMap.put(operator, conditionType);
        } else {
            throw new UnsupportedOperationException("The operator '" + operator + "' is already defined and cannot be overwritten");
        }
    }

    /**
     * Parses expression and builds search filter. Names used in FIQL expression
     * are names of getters/setters in type T.
     * <br>
     * Example:
     * <br>
     * <br>
     * <pre>
     * class Condition {
     *   public String getFoo() {...}
     *   public void setFoo(String foo) {...}
     *   public int getBar() {...}
     *   public void setBar(int bar) {...}
     * }
     *
     * FiqlParser&lt;Condition&gt; parser = new FiqlParser&lt;Condition&gt;(Condition.class);
     * parser.parse("foo==mystery*;bar=ge=10");
     * </pre>
     *
     * @param fiqlExpression expression of filter.
     * @param packageBase    Package base.
     * @return tree of {@link org.apache.cxf.jaxrs.ext.search.SearchCondition}
     * objects representing runtime search structure.
     * @throws Exception when expression does not follow FIQL grammar
     */
    public SearchCondition<T> parse(final String fiqlExpression, final String packageBase) throws Exception {
        final ASTNode<T> ast = parseAndsOrsBrackets(fiqlExpression);
        return ast.build(packageBase);
    }

    private ASTNode<T> parseAndsOrsBrackets(final String expr) throws Exception {
        final List<String> subexpressions = new ArrayList<>();
        final List<String> operators = new ArrayList<>();
        int level = 0;
        int lastIdx = 0;
        int idx;
        for (idx = 0; idx < expr.length(); idx++) {
            final char c = expr.charAt(idx);
            if (c == '(') {
                level++;
            } else if (c == ')') {
                level--;
                if (level < 0) {
                    throw new Exception(String.format("Unexpected closing bracket at position %d", idx));
                }
            }
            final String cs = Character.toString(c);
            final boolean isOperator = AND.equals(cs) || OR.equals(cs);
            if (level == 0 && isOperator) {
                final String s1 = expr.substring(lastIdx, idx);
                final String s2 = expr.substring(idx, idx + 1);
                subexpressions.add(s1);
                operators.add(s2);
                lastIdx = idx + 1;
            }
            final boolean isEnd = idx == expr.length() - 1;
            if (isEnd) {
                final String s1 = expr.substring(lastIdx, idx + 1);
                subexpressions.add(s1);
                operators.add(null);
                lastIdx = idx + 1;
            }
        }
        if (level != 0) {
            throw new Exception(String.format("Unmatched opening and closing brackets in expression: %s", expr));
        }
        if (operators.get(operators.size() - 1) != null) {
            final String op = operators.get(operators.size() - 1);
            final String ex = subexpressions.get(subexpressions.size() - 1);
            throw new Exception("Dangling operator at the end of expression: ..." + ex + op);
        }
        // looking for adjacent ANDs then group them into ORs
        // Note: in case not ANDs is found (e.g only ORs) every single
        // subexpression is
        // treated as "single item group of ANDs"
        int from = 0;
        int to = 0;
        final SubExpression ors = new SubExpression(OR);
        while (to < operators.size()) {
            while (to < operators.size() && AND.equals(operators.get(to))) {
                to++;
            }
            final SubExpression ands = new SubExpression(AND);
            for (; from <= to; from++) {
                final String subex = subexpressions.get(from);
                ASTNode<T> node;
                if (subex.startsWith("(")) {
                    node = parseAndsOrsBrackets(subex.substring(1, subex.length() - 1));
                } else {
                    node = parseComparison(subex);
                }
                ands.add(node);
            }
            to = from;
            if (ands.getSubnodes().size() == 1) {
                ors.add(ands.getSubnodes().get(0));
            } else {
                ors.add(ands);
            }
        }
        if (ors.getSubnodes().size() == 1) {
            return ors.getSubnodes().get(0);
        } else {
            return ors;
        }
    }

    private Comparison parseComparison(final String expr) throws Exception {
        final String comparators = StringUtils.join(operatorsMap.keySet(), "|");
        final String s1 = "[\\p{ASCII}]+(" + comparators + ")";
        final Pattern p = Pattern.compile(s1);
        final Matcher m = p.matcher(expr);
        if (m.find()) {
            final String name = expr.substring(0, m.start(1));
            final String operator = m.group(1);
            final String value = expr.substring(m.end(1));
            if ("".equals(value)) {
                throw new Exception("Not a comparison expression: " + expr);
            }
            final Object castedValue = parseDatatype(name, value);
            return new Comparison(name, operator, castedValue);
        } else {
            throw new Exception("Not a comparison expression: " + expr);
        }
    }

    private Object parseDatatype(final String setter, final String value) throws Exception {
        Object castedValue;
        Class<?> firstTokenType;
        Class<?> valueType;
        boolean isCollection;
        String methodName;
        try {

            final String[] tokens = setter.split("\\.");
            final String token;
            if (tokens.length > 1) {
                token = tokens[0];
                methodName = tokens[1];
            } else {
                token = setter;
                methodName = null;
            }

            firstTokenType = beanspector.getAccessorType(token);
            isCollection = ReflectionUtils.isCollectionImplementation(firstTokenType);

            valueType = beanspector.getAccessorType(setter);

        } catch (final Exception e) {
            throw new Exception(e);
        }
        if (Date.class.isAssignableFrom(valueType)) {
            DateFormat df;
            try {
                df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                // zone in XML is "+01:00" in Java is "+0100"; stripping
                // semicolon
                final int idx = value.lastIndexOf(':');
                if (idx != -1) {
                    final String v = value.substring(0, idx) + value.substring(idx + 1);
                    castedValue = df.parse(v);
                } else {
                    castedValue = df.parse(value);
                }
            } catch (final ParseException e) {
                // is that duration?
                try {
                    final Date now = new Date();
                    DatatypeFactory.newInstance().newDuration(value).addTo(now);
                    castedValue = now;
                } catch (final DatatypeConfigurationException e1) {
                    throw new Exception(e1);
                } catch (final IllegalArgumentException e1) {
                    throw new Exception("Can parse " + value + " neither as date nor duration", e);
                }
            }
        }
        // ESTE BLOQUE ES PARA LOS DATOS TIPO DATETIME
        // else if (DateTime.class.isAssignableFrom(valueType)) {
        // final DateTimeFormatter dateTimeFormatter =
        // DateTimeFormat.forPattern(FORMAT_DATETIME);
        //
        // DateTime dateTime = null;
        // try {
        // if (value != null) {
        //
        // dateTime = dateTimeFormatter.parseDateTime(value);
        //
        // }
        // } catch (final Exception e) {
        // throw new Exception("cannot dataTime parser", e);
        // }
        // castedValue = dateTime;
        // }
        else if (BigDecimal.class.isAssignableFrom(valueType)) {
            castedValue = new BigDecimal(value);
        } else if (valueType.isEnum() || Enumeration.class.isAssignableFrom(valueType)) {
            String valueEnum = value;
            if (valueEnum.startsWith("'")) {
                valueEnum = valueEnum.replaceFirst("'", StringUtils.EMPTY);
                if (valueEnum.endsWith("'")) {
                    valueEnum = valueEnum.substring(0, valueEnum.length() - 1);
                }
            } else if (valueEnum.startsWith("\"")) {
                valueEnum = valueEnum.replaceFirst("\"", StringUtils.EMPTY);
                if (valueEnum.endsWith("\"")) {
                    valueEnum = valueEnum.substring(0, valueEnum.length() - 1);
                }
            }
            castedValue = enumsUtils.getEnum(valueType, valueEnum);
        } else {
            try {
                if (ReflectionUtils.isCollectionImplementation(valueType)) {
                    castedValue = reflectionUtils.createAndInitializeCollection(ReflectionUtils.extractGenerics(valueType), methodName, value);
                } else {
                    castedValue = InjectionUtils.convertStringToPrimitive(value, valueType);
                }
            } catch (final Exception e) {
                throw new Exception("Cannot convert String value \"" + value + "\" to a value of class " + valueType.getName(), e);
            }
        }
        if (isCollection) {
            try {
                return reflectionUtils.createAndInitializeCollection(firstTokenType, methodName, castedValue);
            } catch (final Exception e) {
                throw new Exception("Cannot set value for attribute '" + methodName + "' of type '" + firstTokenType.getSimpleName() + "' as a part of a collection", e);
            }
        } else {
            return castedValue;
        }
    }

    public Object getBean() {
        return beanspector.getBean();
    }

    // node of abstract syntax tree
    private interface ASTNode<T> {
        SearchCondition<T> build(String packageBase) throws Exception;
    }

    private class SubExpression implements ASTNode<T> {
        private final String operator;
        private final List<ASTNode<T>> subnodes = new ArrayList<>();

        public SubExpression(final String operator) {
            this.operator = operator;
        }

        public void add(final ASTNode<T> node) {
            subnodes.add(node);
        }

        public List<ASTNode<T>> getSubnodes() {
            return Collections.unmodifiableList(subnodes);
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder(operator.equals(AND) ? "AND" : "OR");
            s.append(":[");
            for (int i = 0; i < subnodes.size(); i++) {
                s.append(subnodes.get(i));
                if (i < subnodes.size() - 1) {
                    s.append(", ");
                }
            }
            s.append("]");
            return s.toString();
        }

        @Override
        public SearchCondition<T> build(String packageBase) throws Exception {
            boolean hasSubtree = false;
            for (final ASTNode<T> node : subnodes) {
                if (node instanceof ExtendableFiqlParser.SubExpression) {
                    hasSubtree = true;
                    break;
                }
            }
            if (!hasSubtree && AND.equals(operator)) {
                try {
                    final Map<String, ConditionType> map = new HashMap<>();
                    beanspector.instantiate(true);
                    for (final ASTNode<T> node : subnodes) {
                        final Comparison comp = (Comparison) node;
                        map.put(comp.getName(), operatorsMap.get(comp.getOperator()));
                        beanspector.setValue(comp.getName(), comp.getValue());
                    }
                    return new ExtendedSearchCondition<>(map, beanspector.getBean());
                } catch (final Throwable e) {
                    throw new RuntimeException(e);
                }
            } else {
                final List<SearchCondition<T>> scNodes = new ArrayList<>();
                for (final ASTNode<T> node : subnodes) {
                    scNodes.add(node.build(packageBase));
                }
                if (OR.equals(operator)) {
                    return new OrSearchCondition<>(scNodes);
                } else {
                    return new AndSearchCondition<>(scNodes);
                }
            }
        }
    }

    private class Comparison implements ASTNode<T> {
        private final String name;
        private final String operator;
        private final Object value;

        public Comparison(final String name, final String operator, final Object value) {
            this.name = name;
            this.operator = operator;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getOperator() {
            return operator;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name + " " + operator + " " + value + " (" + value.getClass().getSimpleName() + ")";
        }

        @Override
        public SearchCondition<T> build(String packageBase) throws Exception {
            final T cond = createTemplate(name, value);
            final ConditionType ct = operatorsMap.get(operator);
            return new ExtendedSearchCondition<>(ct, cond, packageBase);
        }

        private T createTemplate(final String setter, final Object val) throws Exception {
            try {
                beanspector.instantiate(false).setValue(setter, val);
                return beanspector.getBean();
            } catch (final Throwable e) {
                throw new Exception(e);
            }
        }

    }
}
