package com.araguacaima.commons.utils;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Alejandro on 03/12/2014.
 */
public class CollectionUtils {

    private static final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    public static NotNullsLinkedHashSet<Object> wrapList(Collection<Object> collection) {
        return new NotNullsLinkedHashSet<Object>(false, null, collection);
    }

    public static List buildEmptyList(List collection) {
        return new ArrayList<Object>(collection);
    }

    public static List buildInitializedList(Object value) {
        final ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add(value);
        return arrayList;
    }

    public static List<Object> fillList(List<Object> collection, Object value) {
        collection.add(value);
        return collection;
    }

    public static boolean startsWithAny(final Collection<String> collection, final String value) {
        return org.apache.commons.collections4.CollectionUtils.find(collection, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return value.startsWith((String) object);
            }
        }) != null;
    }

    public static boolean isPrefixedByAny(final Collection<String> collection, final String value) {
        return org.apache.commons.collections4.CollectionUtils.find(collection, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return value.startsWith((String) object);
            }
        }) != null;
    }

    public static List transformByGetterOfField(Collection collection, final String fieldName) {
        final List collection1 = new ArrayList(collection);
        org.apache.commons.collections4.CollectionUtils.transform(collection1, new Transformer() {
            @Override
            public Object transform(Object input) {
                if (input != null) {
                    try {
                        return propertyUtilsBean.getSimpleProperty(input, fieldName);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                        ignored.printStackTrace();
                    }
                }
                return null;
            }

        });
        return collection1;
    }

    public static boolean isMemberOf(Collection<Object> collection, Object value) {
        return collection.contains(value);
    }

    public static boolean isMemberOf(Collection<Object> collection, Collection<Object> value) {
        return org.apache.commons.collections4.CollectionUtils.isSubCollection(collection, value);
    }


    public static Object getMemberOf(Collection<Object> collection, Object value) {

        Object value_ = null;

        if (collection == null || collection.isEmpty()) {
            return value_;
        }

        if (collection.contains(value)) {
            value_ = value;
        }
        return value_;
    }

    public static boolean isMemberOf(Collection<Object> collection, Object value, String field) {
        boolean found = false;
        for (Object o : collection) {
            Object value_ = null;
            try {
                value_ = propertyUtilsBean.getSimpleProperty(o, field);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (value_ != null && value_.equals(value)) {
                found = true;
                break;
            }
        }
        return found;
    }

    public static NotNullsLinkedHashSet<String> wrapListToString(Collection<Object> collection) {
        Collection<String> collection_ = org.apache.commons.collections4.CollectionUtils.transformingCollection(collection, new Transformer() {
            @Override
            public Object transform(Object input) {
                return input == null ? StringUtils.EMPTY : input.toString();
            }
        });
        return new NotNullsLinkedHashSet<String>(false, null, collection_);
    }
}
