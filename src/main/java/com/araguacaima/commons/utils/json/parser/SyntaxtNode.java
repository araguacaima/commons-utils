package com.araguacaima.commons.utils.json.parser;

import java.lang.reflect.Field;

public interface SyntaxtNode<T> {
    T getBean();

    Field getField();

    String getName();

    Class getType();
}
