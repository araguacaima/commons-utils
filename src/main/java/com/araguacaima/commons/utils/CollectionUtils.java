package com.araguacaima.commons.utils;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Alejandro on 03/12/2014.
 */
public class CollectionUtils {

    private static final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    private static final CollectionUtils INSTANCE = new CollectionUtils();
    ;

    private CollectionUtils() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static CollectionUtils getInstance() {
        return INSTANCE;
    }

    public static List<Object> buildEmptyList(List collection) {
        return new ArrayList<Object>(collection);
    }

    public static List buildInitializedList(Object value) {
        final ArrayList<Object> arrayList = new ArrayList<>();
        arrayList.add(value);
        return arrayList;
    }

    public static List<Object> fillList(List<Object> collection, Object value) {
        collection.add(value);
        return collection;
    }

    public static Object getMemberOf(Collection<Object> collection, Object value) {

        Object value_ = null;

        if (collection == null || collection.isEmpty()) {
            return null;
        }

        if (collection.contains(value)) {
            value_ = value;
        }
        return value_;
    }

    public static boolean isMemberOf(Collection<Object> collection, Object value) {
        return collection.contains(value);
    }

    public static boolean isMemberOf(Collection<Object> collection, Collection<Object> value) {
        return org.apache.commons.collections4.CollectionUtils.isSubCollection(collection, value);
    }

    public static boolean isMemberOf(Collection<Object> collection, Object value, String field) {
        boolean found = false;
        for (Object o : collection) {
            Object value_ = null;
            try {
                value_ = propertyUtilsBean.getSimpleProperty(o, field);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if (value_ != null && value_.equals(value)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public static boolean isPrefixedByAny(final Collection<String> collection, final String value) {
        return org.apache.commons.collections4.IterableUtils.find(collection, value::startsWith) != null;
    }

    public static boolean startsWithAny(final Collection<String> collection, final String value) {
        return org.apache.commons.collections4.IterableUtils.find(collection, value::startsWith) != null;
    }

    public static List transformByGetterOfField(Collection<Object> collection, final String fieldName) {
        final List<Object> collection1 = new ArrayList<>(collection);
        org.apache.commons.collections4.CollectionUtils.transform(collection1, input -> {
            if (input != null) {
                try {
                    return propertyUtilsBean.getSimpleProperty(input, fieldName);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
        return collection1;
    }

    public static NotNullsLinkedHashSet<Object> wrapList(Collection<Object> collection) {
        return new NotNullsLinkedHashSet<>(false, null, collection);
    }

    public static NotNullsLinkedHashSet<String> wrapListToString(Collection<Object> collection) {
        Collection<String> collection_ = org.apache.commons.collections4.CollectionUtils.transformingCollection(
                collection,
                (Transformer) input -> input == null ? StringUtils.EMPTY : input.toString());
        return new NotNullsLinkedHashSet<>(false, null, collection_);
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cannot clone instance of this class");
    }

    public boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }
}
