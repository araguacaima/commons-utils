package com.araguacaima.commons.utils.json.parser;

import com.araguacaima.commons.utils.ReflectionUtils;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses <a
 * href="http://tools.ietf.org/html/draft-nottingham-atompub-jsonPath-00">JSON_PATH</a>
 * expression to construct
 *
 * @param <T> type of search condition.
 */
public class JsonPathParser<T> {

    private final Beanspector<T> beanspector;
    private SyntaxtNode<T> node;

    /**
     * Creates Json parser.
     *
     * @param tclass - class of T used to create condition objects in built syntax
     *               tree. Class T must have accessible no-arg constructor and
     *               complementary setters to these used in Json expressions.
     */
    public JsonPathParser(final Class<T> tclass) {
        this(tclass, tclass.getClassLoader());
    }

    public JsonPathParser(Class<T> tClass, ClassLoader classLoader) {
        beanspector = new Beanspector<>(tClass, classLoader);
    }

    public Object getBean() {
        return beanspector.getBean();
    }

    public SyntaxtNode<T> getNode() {
        return node;
    }

    public Class getTclass() {
        return beanspector.getTclass();
    }

    /**
     * Parses expression. Names used in JSON_PATH expression
     * are names of getters/setters in type T.
     *
     * @param jsonPathExpression json path expression.
     * @return tree of {@link T}
     * objects representing runtime structure.
     * @throws JsonParseException        when expression does not follow JSON_PATH grammar
     * @throws InstantiationException    when expression does not follow JSON_PATH grammar
     * @throws IllegalAccessException    when expression does not follow JSON_PATH grammar
     */
    public Map<T, Field> parse(final String jsonPathExpression)
            throws
            JsonParseException,
            InstantiationException,
            IllegalAccessException {
        this.node = parseDatatype(jsonPathExpression);
        return new HashMap<T, Field>() {{
            put(node.getBean(), node.getField());
        }};
    }

    private SyntaxtNode<T> parseDatatype(final String setter)
            throws JsonParseException, InstantiationException, IllegalAccessException {
        Class<?> firstTokenType;
        Class<?> valueType;
        boolean isCollection = false;
        String methodName;
        String token;
        try {
            final String[] tokens = setter.split("\\.");
            if (tokens.length > 1) {
                token = tokens[0];
                methodName = tokens[1];
            } else {
                token = setter;
                methodName = null;
            }
            firstTokenType = beanspector.getAccessorType(token);
            if (firstTokenType != null) {
                isCollection = ReflectionUtils.isCollectionImplementation(firstTokenType);
            }
            valueType = beanspector.getAccessorType(setter);

        } catch (final Exception e) {
            throw new JsonParseException("", JsonLocation.NA, e);
        }

        if (ReflectionUtils.isCollectionImplementation(valueType)) {
            valueType = ReflectionUtils.createAndInitializeTypedCollection(ReflectionUtils.extractGenerics(valueType),
                    null).getClass();
        }

        if (isCollection) {
            try {
                valueType = ReflectionUtils.createAndInitializeTypedCollection(firstTokenType, null).getClass();
            } catch (final Exception e) {
                throw new JsonParseException("Cannot set value for attribute '" + methodName + "' of type '" +
                        firstTokenType.getSimpleName() + "' as a part of a collection",
                        JsonLocation.NA,
                        e);
            }
        }
        return new Pair(setter, valueType);
    }

    private class Pair implements SyntaxtNode<T> {
        private final String name;
        private final Class type;

        public Pair(final String name, final Class type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public T getBean() {
            return beanspector.getBean();
        }

        @Override
        public Field getField() {
            return beanspector.getLastTokenField();
        }

        public String getName() {
            return name;
        }

        public Class getType() {
            return type;
        }

        @Override
        public String toString() {
            return name + " " + type + " (" + type.getSimpleName() + ")";
        }
    }
}
