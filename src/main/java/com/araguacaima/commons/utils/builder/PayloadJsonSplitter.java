package com.araguacaima.commons.utils.builder;

import com.araguacaima.commons.utils.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayloadJsonSplitter implements SpecialParamSplitter {

    private Constants.SpecialQueryParams specialQueryParam = Constants.SpecialQueryParams.PAYLOAD;
    private String completeParam;
    private String rightSideParam;
    private String leftSideParam;

    private PayloadJsonSplitter() {
    }

    public PayloadJsonSplitter(String queryParamToStore) {
        String value = specialQueryParam.value();
        Pattern p = Pattern.compile("(PAYLOAD[ ]+JSON)(.+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = p.matcher(queryParamToStore);
        completeParam = queryParamToStore;
        if (matcher.find()) {
            queryParamToStore = matcher.group(2);
            leftSideParam = matcher.group(1);
            rightSideParam = StringUtils.replace(queryParamToStore, value, StringUtils.EMPTY).replaceFirst("=", StringUtils.EMPTY).trim();
            if (queryParamToStore.startsWith("(") && queryParamToStore.endsWith(")")) {
                String s = queryParamToStore.replaceFirst("\\(", StringUtils.EMPTY);
                rightSideParam = s.substring(0, s.length() - 1);
            } else {
                throw new IllegalArgumentException("The provided queryParam '" + queryParamToStore + "' is intended to be of a payload JSON kind but it has no the required format. The content must start with '(' and ends with ')'");
            }
        } else {
            throw new IllegalArgumentException("The provided queryParam '" + queryParamToStore + "' is not of a json payload kind");
        }
    }

    @Override
    public Constants.SpecialQueryParams getSpecialQueryParam() {
        return this.specialQueryParam;
    }

    @Override
    public String getCompleteParam() {
        return this.completeParam;
    }

    @Override
    public String getRightSideParam() {
        return this.rightSideParam;
    }

    @Override
    public String getLeftSideParam() {
        return this.leftSideParam;
    }


}

