package com.araguacaima.commons.utils.filter;

import com.araguacaima.commons.utils.NotNullsLinkedHashSet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Alejandro on 08/12/2014.
 */
public class Message implements IMessage {

    public String origin;
    public String references;
    public String values;
    private Map<String, String> comments = new HashMap<>();
    private String fieldName;
    private String fileName;
    private Object object;
    private Map<String, String> ruleNames = new HashMap<>();
    private Map<String, NotNullsLinkedHashSet<String>> expectedValues = new HashMap<>();

    public Message() {

    }

    @Override
    public void addComment(String locale, String comment) {
        this.comments.put(locale, comment);
    }

    @Override
    public void addRuleName(String locale, String comment) {
        this.ruleNames.put(locale, comment);
    }

    @Override
    public Map<String, String> getComments() {
        return comments;
    }

    @Override
    public void setComments(Map<String, String> comments) {
        this.comments = comments;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    @JsonIgnore
    @Transient
    public String getLocalizedComment() {
        return getLocalizedString(comments);
    }

    private String getLocalizedString(Map<String, String> comment) {
        if (comment != null && comment.size() > 0) {
            String s = comment.get(Locale.getDefault().getLanguage());
            if (StringUtils.isBlank(s)) {
                s = comment.get(Locale.ENGLISH.getLanguage());
            }
            return s;
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    @JsonIgnore
    @Transient
    public String getLocalizedRuleName() {
        return getLocalizedString(ruleNames);
    }

    @Override
    public Object getObject() {
        return object;
    }

    @Override
    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @Override
    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    @Override
    public Map<String, String> getRuleNames() {
        return ruleNames;
    }

    @Override
    public void setRuleNames(Map<String, String> ruleNames) {
        this.ruleNames = ruleNames;
    }

    @Override
    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public Map<String, NotNullsLinkedHashSet<String>> getExpectedValues() {
        return expectedValues;
    }

    public void setExpectedValues(Map<String, NotNullsLinkedHashSet<String>> expectedValues) {
        this.expectedValues = expectedValues;
    }

    public void setExpectedValues(String locale, NotNullsLinkedHashSet<String> expectedValues) {
        getExpectedValues().put(locale, expectedValues);
    }

    public void addExpectedValues(String locale, NotNullsLinkedHashSet<String> expectedValues) {
        final Map<String, NotNullsLinkedHashSet<String>> expectedValues_ = getExpectedValues();
        NotNullsLinkedHashSet<String> values = expectedValues_.get(locale);
        if (CollectionUtils.isEmpty(values)) {
            values = new NotNullsLinkedHashSet<>();
            expectedValues_.put(locale, values);
        }
        values.addAll(expectedValues);
    }

    public void addExpectedValue(String locale, String value) {
        final Map<String, NotNullsLinkedHashSet<String>> expectedValues = getExpectedValues();
        NotNullsLinkedHashSet<String> values = expectedValues.get(locale);
        if (CollectionUtils.isEmpty(values)) {
            values = new NotNullsLinkedHashSet<>();
            expectedValues.put(locale, values);
        }
        values.add(value);
    }

    @JsonIgnore
    @Transient
    public String getExpectedValuesCleaned() {
        String expectedValuesCleaned;
        if (expectedValues != null && expectedValues.size() > 0) {
            final String join = StringUtils.join(expectedValues, ", ");
            if (StringUtils.isNotBlank(join)) {
                expectedValuesCleaned = "[" + join + "]";
            } else {
                expectedValuesCleaned = null;
            }

        } else {
            expectedValuesCleaned = null;
        }
        return expectedValuesCleaned;
    }

    @JsonIgnore
    @Transient
    public String getLocalizedExpectedValues() {
        if (expectedValues != null && expectedValues.size() > 0) {
            final NotNullsLinkedHashSet<String> values = expectedValues.get(Locale.getDefault().getLanguage());
            return StringUtils.join(values, ", ");
        } else {
            return StringUtils.EMPTY;
        }
    }

}
