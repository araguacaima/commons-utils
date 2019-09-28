package com.araguacaima.commons.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

public class ParameterFinder {

    public final static int REQUEST_ATTRIBUTE_PARAMETER_SESSION_SEARCH_ORDER = 1;
    public final static int REQUEST_ONLY_SEARCH_ORDER = 5;
    public final static int REQUEST_PARAMETER_ATTRIBUTE_SESSION_SEARCH_ORDER = 0;
    public final static int DEFAULT_ORDER = REQUEST_PARAMETER_ATTRIBUTE_SESSION_SEARCH_ORDER;
    public final static int SESSION_ONLY_SEARCH_ORDER = 4;
    public final static int SESSION_REQUEST_ATTRIBUTE_PARAMETER_SEARCH_ORDER = 2;
    public final static int SESSION_REQUEST_PARAMETER_ATTRIBUTE_SEARCH_ORDER = 3;
    public final static String TYPE_ATTRIBUTE = "Attribute";
    public final static String TYPE_PARAMETER = "Parameter";
    public final static String TYPE_REQUEST = "Request";
    public final static String TYPE_SESSION = "Session";
    private final static String className = ParameterFinder.class.getName();
    private static final Logger log = LoggerFactory.getLogger(ParameterFinder.className);
    private HttpServletRequest request;
    private HttpSession session;

    private ParameterFinder() {

    }

    public ParameterFinder(HttpServletRequest request) {
        this.request = request;
        this.session = request.getSession();
    }

    public Object getAttribute(String attributeName) {
        return getAttribute(attributeName, DEFAULT_ORDER);
    }

    public Object getAttribute(String attributeName, int order) {
        String metodo = className + " - getAttribute: ";
        Object object;
        switch (order) {
            case REQUEST_PARAMETER_ATTRIBUTE_SESSION_SEARCH_ORDER:
                object = this.getRequest().getParameter(attributeName);
                if (object == null) {
                    object = this.getRequest().getAttribute(attributeName);
                    if (object == null) {
                        log.debug(metodo + "Parameter '" + attributeName + "' found as " + TYPE_SESSION + "-" +
                                TYPE_ATTRIBUTE);
                        return this.getSession().getAttribute(attributeName);
                    } else {
                        log.debug(metodo + "Parameter '" + attributeName + "' found as " + TYPE_REQUEST + "-" +
                                TYPE_ATTRIBUTE);
                        return object;
                    }
                } else {
                    log.debug(metodo + "Parameter '" + attributeName + "' found as " + TYPE_REQUEST + "-" +
                            TYPE_PARAMETER);
                    return object;
                }
            case REQUEST_ATTRIBUTE_PARAMETER_SESSION_SEARCH_ORDER:
                object = this.getRequest().getAttribute(attributeName);
                if (object == null) {
                    object = this.getRequest().getParameter(attributeName);
                    if (object == null) {
                        log.debug(metodo + "Parameter '" + attributeName + "' found as " + TYPE_SESSION + "-" +
                                TYPE_ATTRIBUTE);
                        return this.getSession().getAttribute(attributeName);
                    } else {
                        log.debug(metodo + "Parameter '" + attributeName + "' found as " + TYPE_REQUEST + "-" +
                                TYPE_PARAMETER);
                        return object;
                    }
                } else {
                    log.debug(metodo + "Parameter '" + attributeName + "' found as " + TYPE_REQUEST + "-" +
                            TYPE_ATTRIBUTE);
                    return object;
                }
            case SESSION_REQUEST_ATTRIBUTE_PARAMETER_SEARCH_ORDER:
                object = this.getSession().getAttribute(attributeName);
                if (object == null) {
                    object = this.getRequest().getAttribute(attributeName);
                    if (object == null) {
                        log.debug(metodo + "Parameter '" + attributeName + "' found as " + TYPE_REQUEST + "-" +
                                TYPE_PARAMETER);
                        return this.getRequest().getParameter(attributeName);
                    } else {
                        log.debug(metodo + "Parameter '" + attributeName + "' found as " + TYPE_REQUEST + "-" +
                                TYPE_ATTRIBUTE);
                        return object;
                    }
                } else {
                    log.debug(metodo + "Parameter '" + attributeName + "' found as " + TYPE_SESSION + "-" +
                            TYPE_ATTRIBUTE);
                    return object;
                }

            default:
                return null;
        }
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpSession getSession() {
        return session;
    }

    public Object getAttributeDefaultValue(String attributeName, Object defaultValue) {
        Object object = this.getAttribute(attributeName, DEFAULT_ORDER);
        return object == null ? defaultValue : object;
    }

    public Object getAttributeDefaultValue(String attributeName, Object defaultValue, int order) {
        Object object = this.getAttribute(attributeName, order);
        return object == null ? null : defaultValue;
    }

    public Map<Object, Object> getBothParametersAndAttributes() {
        Map<Object, Object> map = getAllParameters();
        map.putAll(getAllAttributes());
        return map;
    }

    public Map<Object, Object> getAllParameters() {
        final Map<Object, Object> map = new HashMap<>();
        final HttpServletRequest req = this.getRequest();
        Collection<Object> parameters = new ArrayList<>();
        CollectionUtils.addAll(parameters, req.getParameterNames());
        IterableUtils.forEach(parameters, o -> {
            String[] paramValues = req.getParameterValues((String) o);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() == 0) {
                    map.put(o, null);
                } else {
                    map.put(o, req.getParameter((String) o));
                }
            } else {
                Collection<String> paramValuesCollection = new ArrayList<>();
                paramValuesCollection.addAll(Arrays.asList(paramValues));
                map.put(o, paramValuesCollection);
            }
        });
        return map;
    }

    public Map<Object, Object> getAllAttributes() {
        final Map<Object, Object> map = new HashMap<>();
        final HttpServletRequest req = this.getRequest();
        Collection<Object> attributes = new ArrayList<>();
        CollectionUtils.addAll(attributes, req.getAttributeNames());
        IterableUtils.forEach(attributes, o -> map.put(o, req.getAttribute((String) o)));
        return map;
    }
}
