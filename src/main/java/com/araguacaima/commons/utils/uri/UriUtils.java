package com.araguacaima.commons.utils.uri;


import com.araguacaima.commons.utils.Constants;
import com.araguacaima.commons.utils.EnumsUtils;
import com.araguacaima.commons.utils.HttpMethodEnum;
import com.araguacaima.commons.utils.ReflectionUtils;
import com.araguacaima.commons.utils.builder.SpecialParamSplitter;
import com.araguacaima.commons.utils.builder.SpecialParamSplitterBuilder;
import com.araguacaima.commons.utils.filter.RestQueryStringUtil;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UriUtils {

    private static final Pattern INSIDE_CURLY_BRACKETS_PATTERN = Pattern.compile("\\{([^}]*)}");
    private static final ReflectionUtils reflectionUtils = new ReflectionUtils(null);
    private static final EnumsUtils<Object> enumsUtils = new EnumsUtils<>();

    public static PathAndQueryString extractPathAndQueryString(String uri) throws MalformedURLException {
        if (StringUtils.isNotBlank(uri)) {
            if (uri.startsWith("\"")) {
                uri = uri.substring(1);
            }
            if (uri.endsWith("\"")) {
                uri = uri.substring(0, uri.length() - 1);
            }
        }
        uri = StringUtils.trim(uri);
        String[] urlTokens = uri.split("\\?");
        String path = StringUtils.EMPTY;
        String queryString = StringUtils.EMPTY;
        if (urlTokens.length == 1) {
            path = urlTokens[0];
        } else if (urlTokens.length > 2) {
            throw new MalformedURLException("There is more than one '?' symbol, which is not permitted");
        } else if (urlTokens.length == 2) {
            path = urlTokens[0];
            queryString = urlTokens[1];
        }
        final List<String> httpMethods = enumsUtils.getValuesList(HttpMethodEnum.class);
        final String finalPath = path;
        String method = null;
        if (finalPath != null) {
            method = org.apache.commons.collections4.IterableUtils.find(httpMethods, (Predicate) object -> finalPath.trim().startsWith((String) object));
        }
        if (method != null) {
            path = path.replaceFirst(method, StringUtils.EMPTY);
        } else {
            throw new MalformedURLException(
                    "There is no HTTP Method declared. URL must starts by any of the following HTTP Methods: "
                            + StringUtils.join(enumsUtils.getValuesList(HttpMethodEnum.class), ", "));
        }
        path = path.trim();

        if (urlTokens.length == 1) {
            String pathTemp = path;
            path = StringUtils.split(path, " ")[0];
            queryString = StringUtils.replace(pathTemp, path, StringUtils.EMPTY).trim();
        }

        if (path.startsWith("\"/")) {
            path = path.substring(1);
        }
        if (path.endsWith("\"")) {
            path = path.substring(0, path.length() - 1);
        }
        path = path.trim();

        queryString = queryString.trim();
        if (queryString.startsWith("\"/")) {
            queryString = queryString.substring(1);
        }
        if (queryString.endsWith("\"")) {
            queryString = queryString.substring(0, queryString.length() - 1);
        }
        queryString = queryString.trim();
        PathAndQueryString pathQueryStr = new PathAndQueryString();
        pathQueryStr.setPath(path);
        pathQueryStr.setQueryString(queryString);
        pathQueryStr.buildTokenizedQueryParams();
        return pathQueryStr;
    }

    public static void validateQueryParams(String businessService, String queryString) {
//TODO AMM: Terminar
    }

    public static void validateFiqlExpresion(String businessService,
                                             PathAndQueryString queryString,
                                             String packageBase)
            throws ClassNotFoundException {
        Class<?> dtoExtClass = Class.forName(businessService);

        try {
            new RestQueryStringUtil(dtoExtClass.getClassLoader(),
                    packageBase).createNewBeanAndFillItByExtractingFiqlFilter(extractFilterQueryParam(queryString),
                    dtoExtClass);
        } catch (IllegalAccessException | InstantiationException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static String extractFilterQueryParam(PathAndQueryString pathAndQueryString) {
        try {
            Map<String, String> filterValueMap =
                    pathAndQueryString.getTokenizedQueryParams().get(Constants.SpecialQueryParams.FILTER);
            if (filterValueMap != null) {
                String filterValue = filterValueMap.keySet().iterator().next();
                if (StringUtils.isNotBlank(filterValue)) {
                    filterValue = filterValue.trim();
                    if (filterValue.startsWith("(")) {
                        return filterValue.substring(1, filterValue.length() - 1);
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    private static String decorateFullyQualifiedType(String origin, String type, String canonicalModelPackageNameBase) {
        type = type.trim();
        String importType = StringUtils.capitalize(type);
        String result = reflectionUtils.getFullyQualifiedJavaTypeOrNull(type, false);
        return StringUtils.isNotBlank(result) ? result : canonicalModelPackageNameBase.toLowerCase()
                + "."
                + origin
                + "."
                + importType;
    }

    public static List<String> getDataBetweenCurlyBracketsList(Collection<String> urlStr) {
        List<String> result = new ArrayList<>();
        for (String url : urlStr) {
            result.addAll(getDataBetweenCurlyBracketsList(url));
        }
        return result;
    }

    public static List<String> getDataBetweenCurlyBracketsList(String urlStr) {
        List<String> dataList = new ArrayList<>();
        if (StringUtils.isNotBlank(urlStr)) {
            Matcher m = INSIDE_CURLY_BRACKETS_PATTERN.matcher(urlStr);
            while (m.find()) {
                dataList.add(m.group(1));
            }
        }

        return dataList;
    }

    public static String getDataBetweenCurlyBrackets(String str) {
        String data = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(str)) {
            Pattern p = Pattern.compile("\\{([^}]*)}");
            Matcher m = p.matcher(str);

            while (m.find()) {
                data = m.group(1);
            }
        }
        return data;
    }

    public static class PathAndQueryString {
        String path = null;
        String queryString = null;
        final Map<Constants.SpecialQueryParams, Map<String, String>> tokenizedQueryParams = new HashMap<>();
        final Map<Constants.UrlParams, Collection<String>> traversedPathAndQueryParams = new HashMap<>();

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getQueryString() {
            return queryString;
        }

        public void setQueryString(String queryString) {
            this.queryString = queryString;
        }

        public Map<Constants.SpecialQueryParams, Map<String, String>> getTokenizedQueryParams() {
            return tokenizedQueryParams;
        }

        public Map<Constants.UrlParams, Collection<String>> getTraversedPathAndQueryParams() {
            return traversedPathAndQueryParams;
        }

        public void buildTokenizedQueryParams() {
            if (StringUtils.isNotBlank(queryString)) {
                String queryParams = queryString;
                if (queryString.startsWith("&")) {
                    queryParams = queryString.replaceFirst("&", StringUtils.EMPTY);
                } else if (queryString.startsWith("+")) {
                    queryParams = queryString.replaceFirst("\\+", StringUtils.EMPTY).trim();
                }
                List<String> listQueryParams = new ArrayList<>();
                Map<Constants.SpecialQueryParams, Collection<SpecialParamSplitter>> specialParams = SpecialParamSplitterBuilder.build(
                        queryParams);
                for (Map.Entry<Constants.SpecialQueryParams, Collection<SpecialParamSplitter>> param : specialParams.entrySet()) {
                    for (SpecialParamSplitter specialParamSplitter : param.getValue()) {
                        String paramKey = specialParamSplitter.getLeftSideParam();
                        String paramValue = specialParamSplitter.getRightSideParam();
                        if (specialParamSplitter.getSpecialQueryParam().equals(Constants.SpecialQueryParams.QUERY_PARAM)
                                || specialParamSplitter.getSpecialQueryParam().equals(Constants.SpecialQueryParams.FILTER)) {
                            listQueryParams.addAll(UriUtils.getDataBetweenCurlyBracketsList(paramValue));
                        }
                        if (StringUtils.isBlank(paramValue)) {
                            throw new IllegalArgumentException("El parámetro especial '" + paramKey + "' no tiene valores");
                        } else {
                            Map<String, String> map = new HashMap<>();
                            try {
                                if (StringUtils.isNotBlank(paramKey)) {
                                    map.put(paramValue, null);
                                    this.tokenizedQueryParams.put(specialParamSplitter.getSpecialQueryParam(), map);
                                } else {
                                    map.put(paramKey, paramValue);
                                    Map<String, String> existentMap =
                                            this.tokenizedQueryParams.get(Constants.SpecialQueryParams.QUERY_PARAM);
                                    if (existentMap == null) {
                                        this.tokenizedQueryParams.put(Constants.SpecialQueryParams.QUERY_PARAM, map);
                                    } else {
                                        existentMap.putAll(map);
                                    }
                                }
                            } catch (Throwable t) {
                                map.put(paramKey, paramValue);
                                Map<String, String> existentMap =
                                        this.tokenizedQueryParams.get(Constants.SpecialQueryParams.QUERY_PARAM);
                                if (existentMap == null) {
                                    this.tokenizedQueryParams.put(Constants.SpecialQueryParams.QUERY_PARAM, map);
                                } else {
                                    existentMap.putAll(map);
                                }
                            }
                        }

                    }
                }
                if (listQueryParams.size() > 0) {
                    traversedPathAndQueryParams.put(Constants.UrlParams.QUERY_PARAM, listQueryParams);
                }
            }
            List<String> listPathParams = UriUtils.getDataBetweenCurlyBracketsList(this.getPath());
            if (listPathParams.size() > 0) {
                traversedPathAndQueryParams.put(Constants.UrlParams.PATH, listPathParams);
            }
        }

    }
}
