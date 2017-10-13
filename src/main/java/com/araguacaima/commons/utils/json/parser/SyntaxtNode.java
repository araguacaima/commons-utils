package com.araguacaima.commons.utils.json.parser;

import com.fasterxml.jackson.core.JsonParseException;

import java.lang.reflect.Field;

public interface SyntaxtNode<T> {
    T getBean();

    String getName();

    Class getType();

    Field getField();
}
