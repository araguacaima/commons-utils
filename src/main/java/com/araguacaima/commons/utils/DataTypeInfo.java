package com.araguacaima.commons.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Field;

/**
 * Created by XMZ5547 on 31/01/2016.
 */
public class DataTypeInfo implements Comparable<DataTypeInfo> {
    private Class type;
    private String path;
    private Field field;
    private boolean collection = false;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
        if (this.type != null) {
            this.setCollection(ReflectionUtils.isCollectionImplementation(this.type));
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DataTypeInfo that = (DataTypeInfo) o;

        return new EqualsBuilder()
                .append(path, that.path)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(path)
                .append(field)
                .append(collection)
                .toHashCode();
    }

    @Override
    public int compareTo(DataTypeInfo o) {
        if (o == null) {
            return 0;
        } else {
            return this.path.compareTo(o.path);
        }
    }
}
