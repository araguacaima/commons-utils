package org.araguacaima.utils;

import org.apache.commons.collections.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class NotNullOrEmptyStringPredicate implements Predicate, Serializable {

    private final StringUtils stringUtils;

    @Autowired
    public NotNullOrEmptyStringPredicate(StringUtils stringUtils) {

        this.stringUtils = stringUtils;
    }

    public boolean evaluate(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof String)) {
            throw new ClassCastException("Expected String and found " + object.getClass());
        }
        return org.apache.commons.lang3.StringUtils.isNotBlank((String) object);
    }

}
