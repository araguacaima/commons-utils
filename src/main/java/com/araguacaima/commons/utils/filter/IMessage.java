package com.araguacaima.commons.utils.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.beans.Transient;
import java.util.Map;

public interface IMessage {
    void addComment(String locale, String comment);

    void addRuleName(String locale, String comment);

    Map<String, String> getComments();

    void setComments(Map<String, String> comments);

    String getFieldName();

    void setFieldName(String fieldName);

    String getFileName();

    void setFileName(String fileName);

    @JsonIgnore
    @Transient
    String getLocalizedComment();

    @JsonIgnore
    @Transient
    String getLocalizedRuleName();

    Object getObject();

    void setObject(Object object);

    String getOrigin();

    String getReferences();

    Map<String, String> getRuleNames();

    void setRuleNames(Map<String, String> ruleNames);

    String getValues();
}
