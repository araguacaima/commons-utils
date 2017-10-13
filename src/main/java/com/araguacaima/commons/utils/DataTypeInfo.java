package com.araguacaima.commons.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Field;

/**
 * Created by XMZ5547 on 31/01/2016.
 */
public class DataTypeInfo implements Comparable<DataTypeInfo> {
    private boolean collection = false;
    private Field field;
    private String path;
    private Class type;

    @Override
    public int compareTo(DataTypeInfo o) {
        if (o == null) {
            return 0;
        } else {
            return this.path.compareTo(o.path);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        DataTypeInfo that = (DataTypeInfo) o;

        return new EqualsBuilder().append(path, that.path).isEquals();
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
        if (this.type != null) {
            this.setCollection(ReflectionUtils.isCollectionImplementation(this.type));
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(type).append(path).append(field).append(collection).toHashCode();
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }
}
