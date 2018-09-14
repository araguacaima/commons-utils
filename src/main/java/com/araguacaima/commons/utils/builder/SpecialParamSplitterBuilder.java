package com.araguacaima.commons.utils.builder;

import com.araguacaima.commons.utils.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alejandro on 29/09/2015.
 */
public class SpecialParamSplitterBuilder {

    public static Map<Constants.SpecialQueryParams, Collection<SpecialParamSplitter>> build(String params) {
        HashMap<Constants.SpecialQueryParams, Collection<SpecialParamSplitter>> specialParamSplitters = new HashMap<>();
        build(specialParamSplitters, params);
        return specialParamSplitters;
    }

    private static void build(
            Map<Constants.SpecialQueryParams, Collection<SpecialParamSplitter>> specialParamSplitters,
            String params) {

        Constants.SpecialQueryParams[] specialQueryParams = Constants.SpecialQueryParams.values();
        if (StringUtils.isNotBlank(params)) {
            params = params.trim();
            if (params.startsWith("&")) {
                params = params.replaceFirst("&", StringUtils.EMPTY);
            }
            for (Constants.SpecialQueryParams specialQueryParam : specialQueryParams) {
                SpecialParamSplitter specialParamSplitter = SpecialParamSplitterFactory.getSpecialParamSplitters(specialQueryParam, params);
                if (specialParamSplitter != null) {
                    Collection<SpecialParamSplitter> specialParamSplitterList = specialParamSplitters.get(specialQueryParam);
                    if (specialParamSplitterList == null) {
                        specialParamSplitterList = new ArrayList<>();
                    }
                    specialParamSplitterList.add(specialParamSplitter);
                    specialParamSplitters.put(specialQueryParam, specialParamSplitterList);
                    build(specialParamSplitters,
                            StringUtils.replace(params, specialParamSplitter.getCompleteParam(), StringUtils.EMPTY));
                    break;
                }
            }
        }
    }
}
