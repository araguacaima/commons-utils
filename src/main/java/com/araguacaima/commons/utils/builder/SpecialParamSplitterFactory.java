package com.araguacaima.commons.utils.builder;

import com.araguacaima.commons.utils.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Alejandro Mendez on 29/09/2015.
 */
public class SpecialParamSplitterFactory {
    public static SpecialParamSplitter getSpecialParamSplitters(Constants.SpecialQueryParams specialQueryParam, String queryParam) {
        String value = specialQueryParam.value();
        if (StringUtils.isBlank(value)) {
            try {
                return new PayloadJsonSplitter(queryParam);
            } catch (Throwable ignored) {
                return new QueryParamSplitter(queryParam.split("&")[0]);
            }
        } else if (queryParam.startsWith(value)) {
            String queryParamToStore = queryParam.split("&")[0];
            switch (specialQueryParam) {
                case FILTER:
                    return new FilterParamSplitter(queryParamToStore);
                case EXPANDS:
                    return new ExpandsParamSplitter(queryParamToStore);
                case FIELDS:
                    return new FieldsParamSplitter(queryParamToStore);
                case SORT:
                    return new SortParamSplitter(queryParamToStore);
                case SHOW_SENSITIVE_DATA:
                    return new ShowSensitiveDataParamSplitter(queryParamToStore);
                case PAYLOAD:
                    return new PayloadJsonSplitter(queryParamToStore);
                default:
                    return new QueryParamSplitter(queryParamToStore);
            }
        }
        return null;
    }
}
