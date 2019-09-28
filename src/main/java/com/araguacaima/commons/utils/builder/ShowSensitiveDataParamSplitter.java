package com.araguacaima.commons.utils.builder;

import com.araguacaima.commons.utils.Constants;
import org.apache.commons.lang3.StringUtils;

public class ShowSensitiveDataParamSplitter implements SpecialParamSplitter {

    private Constants.SpecialQueryParams specialQueryParam = Constants.SpecialQueryParams.SHOW_SENSITIVE_DATA;
    private String completeParam;
    private String rightSideParam;

    private ShowSensitiveDataParamSplitter() {
    }

    public ShowSensitiveDataParamSplitter(String queryParamToStore) {
        String value = specialQueryParam.value();
        if (queryParamToStore.startsWith(value + "=")) {
            completeParam = queryParamToStore;
            rightSideParam = StringUtils.replace(queryParamToStore, value + "=", StringUtils.EMPTY).trim();
        } else {
            throw new IllegalArgumentException("The provided queryParam '" + queryParamToStore + "' is not of a showSensitiveData kind");
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
