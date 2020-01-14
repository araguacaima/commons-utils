package com.araguacaima.commons.utils.builder;

import com.araguacaima.commons.utils.Constants;

/**
 * Created by Alejandro on 29/09/2015.
 */
public interface SpecialParamSplitter {

    Constants.SpecialQueryParams getSpecialQueryParam();

    String getCompleteParam();

    String getLeftSideParam();

    String getRightSideParam();
}
