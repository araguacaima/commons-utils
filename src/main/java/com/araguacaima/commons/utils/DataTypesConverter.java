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

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataTypesConverter {

    public final Map<Class, DataTypeView> DATA_TYPES_CONVERTER = new HashMap<>();
    private static final Map<String, Class> PRIMITIVE_CLASS_MAP = new HashMap<>();

    static {
        PRIMITIVE_CLASS_MAP.put("int", Integer.class);
        PRIMITIVE_CLASS_MAP.put("integer", Integer.class);
        PRIMITIVE_CLASS_MAP.put("long", Long.class);
        PRIMITIVE_CLASS_MAP.put("double", Double.class);
        PRIMITIVE_CLASS_MAP.put("float", Float.class);
        PRIMITIVE_CLASS_MAP.put("boolean", Boolean.class);
        PRIMITIVE_CLASS_MAP.put("char", Character.class);
        PRIMITIVE_CLASS_MAP.put("byte", Byte.class);
        PRIMITIVE_CLASS_MAP.put("string", String.class);
        PRIMITIVE_CLASS_MAP.put("short", Short.class);
    }

    public DataTypesConverter() {
        DATA_TYPES_CONVERTER.put(Integer.TYPE, new DataTypeView("integer", "int32", "Integer", false));
        DATA_TYPES_CONVERTER.put(Long.TYPE, new DataTypeView("integer", "int64", "Integer", false));
        DATA_TYPES_CONVERTER.put(Float.TYPE, new DataTypeView("number", "float", "Number", false));
        DATA_TYPES_CONVERTER.put(Double.TYPE, new DataTypeView("number", "double", "Number", false));
        DATA_TYPES_CONVERTER.put(String.class, new DataTypeView("string", null, "String", false));
        DATA_TYPES_CONVERTER.put(Byte.TYPE, new DataTypeView("string", "byte", "String", false));
        DATA_TYPES_CONVERTER.put(Boolean.TYPE, new DataTypeView("boolean", null, "Boolean", false));
        DATA_TYPES_CONVERTER.put(Date.class, new DataTypeView("string", "date", "Date", false));
        DATA_TYPES_CONVERTER.put(DateTime.class, new DataTypeView("string", "datetime", "DateTime", false));
        DATA_TYPES_CONVERTER.put(Period.class, new DataTypeView("string", "period", "Period", false));
        DATA_TYPES_CONVERTER.put(java.sql.Date.class, new DataTypeView("string", "date", "Date", false));
        DATA_TYPES_CONVERTER.put(Integer.class, new DataTypeView("integer", "int32", "Integer", false));
        DATA_TYPES_CONVERTER.put(Long.class, new DataTypeView("integer", "int64", "Integer", false));
        DATA_TYPES_CONVERTER.put(Float.class, new DataTypeView("number", "float", "Number", false));
        DATA_TYPES_CONVERTER.put(Double.class, new DataTypeView("number", "double", "Number", false));
        DATA_TYPES_CONVERTER.put(Byte.class, new DataTypeView("string", "byte", "String", false));
        DATA_TYPES_CONVERTER.put(Boolean.class, new DataTypeView("boolean", null, "Boolean", false));
        DATA_TYPES_CONVERTER.put(Enum.class, new DataTypeView("string", null, "String", false));
        DATA_TYPES_CONVERTER.put(Character.class, new DataTypeView("char", null, "Character", false));
        DATA_TYPES_CONVERTER.put(null, new DataTypeView("unknown", null, null, false));
    }

    public DataTypeView getDataTypeView(String type) {
        if (type == null) {
            return null;
        }
        if (type.startsWith("List<")) {
            String temp = type.split("List<")[1];
            temp = temp.substring(0, temp.length() - 1);
            return new DataTypeView("list", null, temp, true);
        }
        if (type.startsWith("Map<")) {
            String temp = type.split("Map<")[1];
            temp = temp.substring(0, temp.length() - 1);
            return new DataTypeView("map", null, temp, true);
        }
        if (type.equalsIgnoreCase("Date") || type.equals(Date.class.getName()) || type.equals(java.sql.Date.class.getName())) {
            return new DataTypeView("date", null, "Date", false);
        }
        if (type.equalsIgnoreCase("DateTime") || type.equalsIgnoreCase("Date-Time") || type.equalsIgnoreCase
                ("Date-Only") || type.equalsIgnoreCase(
                "Time-Only")) {
            return new DataTypeView("datetime", null, "DateTime", false);
        }
        if (type.equalsIgnoreCase("Period")) {
            return new DataTypeView("period", null, "Period", false);
        }
        Class clazz = PRIMITIVE_CLASS_MAP.get(type.contains(".") ? StringUtils.getLastToken(type, ".").toLowerCase() : type.toLowerCase());
        if (clazz != null) {
            return DATA_TYPES_CONVERTER.get(clazz);
        } else {
            return new DataTypeView(DataTypeView.COMPLEX_TYPE, null, type, true);
        }
    }

    public class DataTypeView {
        public static final String COMPLEX_TYPE = "complex";
        private boolean complexDataType;
        private String dataType;
        private String format;
        private String transformedDataType;

        public DataTypeView(String dataType, String format, String transformedDataType, boolean complexDataType) {

            this.dataType = dataType;
            this.format = format;
            this.transformedDataType = transformedDataType;
            this.complexDataType = complexDataType;
        }

        public String extractDataTypeFromListOrMap() {
            if (isListOrMap()) {
                return transformedDataType;
            } else {
                return null;
            }
        }

        public boolean isListOrMap() {
            return "list".equals(dataType) || "map".equals(dataType);
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getTransformedDataType() {
            return transformedDataType;
        }

        public void setTransformedDataType(String transformedDataType) {
            this.transformedDataType = transformedDataType;
        }

        public boolean isComplexDataType() {
            return complexDataType;
        }

        public void setComplexDataType(boolean isComplexDataType) {
            this.complexDataType = isComplexDataType;
        }
    }
}
