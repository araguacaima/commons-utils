package com.araguacaima.commons.utils.json.parser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonSerializerInvocationHandler extends JsonSerializer implements InvocationHandler {

    private Class clazz;
    private Map<String, Object> properties = new HashMap<>();
    private List<Method> readMethods = new ArrayList<>();
    private List<Method> writeMethods = new ArrayList<>();

    public JsonSerializerInvocationHandler(List<Method> readMethods,
                                           List<Method> writeMethods,
                                           Map<String, Object> properties,
                                           Class clazz) {
        this.readMethods = readMethods;
        this.writeMethods = writeMethods;
        this.properties = properties;
        this.clazz = clazz;
    }

    public JsonSerializerInvocationHandler() {
    }

    public Object invoke(Object proxy, final Method method, Object[] args) {

        Method method_ = CollectionUtils.find(readMethods,
                (Predicate) object -> ((Method) object).getName().equals(method.getName()));

        if (method_ != null) {
            return properties.get(StringUtils.uncapitalize(method_.getName().replaceFirst("get", StringUtils.EMPTY)));
        } else {
            method_ = CollectionUtils.find(writeMethods,
                    (Predicate) object -> ((Method) object).getName().equals(method.getName()));

            if (method_ != null) {
                properties.put(StringUtils.uncapitalize(method_.getName().replaceFirst("set", StringUtils.EMPTY)),
                        args[0]);
            }
        }
        if ("getClass".equals(method.getName())) {
            return clazz;
        } else {
            return null;
        }
    }

    @Override
    public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeStartObject();
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            jgen.writeObjectField(property.getKey(), property.getValue());
        }
        jgen.writeEndObject();
    }
}