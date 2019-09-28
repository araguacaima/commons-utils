package com.araguacaima.commons.utils.builder;

import com.araguacaima.commons.utils.Constants;
import org.apache.commons.lang3.StringUtils;

public class SortParamSplitter implements SpecialParamSplitter {

    private Constants.SpecialQueryParams specialQueryParam = Constants.SpecialQueryParams.SORT;
    private String completeParam;
    private String rightSideParam;

    private SortParamSplitter() {
    }

    public SortParamSplitter(String queryParamToStore) {
        String value = specialQueryParam.value();
        if (queryParamToStore.startsWith(value + "=")) {
            completeParam = queryParamToStore;
            rightSideParam = StringUtils.replace(queryParamToStore, value + "=", StringUtils.EMPTY).trim();
            if (rightSideParam.startsWith("(") && rightSideParam.endsWith(")")) {
                String s = rightSideParam.replaceFirst("\\(", StringUtils.EMPTY);
                rightSideParam = s.substring(0, s.length() - 1);
            } else {
                throw new IllegalArgumentException("The provided queryParam '" + queryParamToStore + "' is intended to be of a sort kind but it has no the required format. The content must start with '(' and ends with ')'");
            }
        } else {
            throw new IllegalArgumentException("The provided queryParam '" + queryParamToStore + "' is not of a sort kind");
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
        return specialQueryParam.value();
    }


}
