package com.araguacaima.commons.utils;

public interface ByPassingTestingConstants {
    int BYPASSING_TESTING_CODE_MODE_BYPASSING = 0;//bypass
    int BYPASSING_TESTING_CODE_MODE_DEFAULT = BYPASSING_TESTING_CODE_MODE_BYPASSING;
    int BYPASSING_TESTING_CODE_MODE_DEPLOYMENT = 3;//deployment
    int BYPASSING_TESTING_CODE_MODE_DISABLED = 4;//bypassDisabled
    int BYPASSING_TESTING_CODE_MODE_FORCED = 5;//forceValue
    int BYPASSING_TESTING_CODE_MODE_PRODUCTION = 2;//production
    int BYPASSING_TESTING_CODE_MODE_TESTING = 1;//testing
    String BYPASSING_TESTING_CODE_PARAMETER_PREFIX = "bypassing.testing.code_";
}
