package org.araguacaima.commons.utils;

import org.apache.commons.collections.Predicate;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class NotNullOrEmptyStringPredicate implements Predicate, Serializable {

    public NotNullOrEmptyStringPredicate(StringUtils stringUtils) {

    }

    public boolean evaluate(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof String)) {
            throw new ClassCastException("Expected String and found " + object.getClass());
        }
        return StringUtils.isNotBlank((String) object);
    }

}
