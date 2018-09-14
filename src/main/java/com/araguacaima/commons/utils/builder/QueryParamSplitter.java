package com.araguacaima.commons.utils.builder;

import com.araguacaima.commons.utils.Constants;

public class QueryParamSplitter implements SpecialParamSplitter {

    private Constants.SpecialQueryParams specialQueryParam = Constants.SpecialQueryParams.QUERY_PARAM;
    private String completeParam;
    private String rightSideParam;
    private String leftSideParam;

    private QueryParamSplitter() {
    }

    public QueryParamSplitter(String queryParamToStore) {
        try {
            completeParam = queryParamToStore;
            String[] tokens = queryParamToStore.split("=");
            leftSideParam = tokens[0];
            rightSideParam = tokens[1];
        } catch (Throwable ignored) {
            throw new IllegalArgumentException("The provided queryParam '" + queryParamToStore + "' is not of a simple query kind");
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
    public String getLeftSideParam() {
        return this.leftSideParam;
    }

    @Override
    public String getRightSideParam() {
        return this.rightSideParam;
    }


}
