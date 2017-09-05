package org.araguacaima.utils;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * Clase utilitaria para el manejo de Request y Sesiones Http
 * Clase: HttpUtil.java <br>
 * Changes:<br>
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class HttpUtils {

    private static Logger log = LoggerFactory.getLogger(HttpUtils.class);
    public final String DATA = "data";
    public final String FORM = "form";
    public final String INCLUDE_PARAMS = "javax.servlet.include.query_string";
    public final String NULL = "null";
    public final List chars = Arrays.asList(StringUtils.PERCENTAGE_SYMBOL,
            StringUtils.AMPERSAND_SYMBOL,
            StringUtils.PLUS,
            StringUtils.QUESTION_SYMBOL,
            StringUtils.DOLLAR,
            StringUtils.COMMA,
            StringUtils.SLASH,
            StringUtils.COLON,
            StringUtils.SEMI_COLON,
            StringUtils.EQUALS_SYMBOL,
            StringUtils.ARROBA,
            StringUtils.QUOTE,
            StringUtils.DOUBLE_QUOTE);
    public final List codes = Arrays.asList("_porc_",
            "_amper_",
            "_plus_",
            "_ques_",
            "_dola_",
            "_comm_",
            "_slash_",
            "_colo_",
            "_semi_",
            "_equa_",
            "_arob_",
            "_squot_",
            "_quot_");
    private HttpClient httpClient = new HttpClient();
    private StringUtils stringUtils;

    public HttpUtils(String host, int port, String username, String password) {
        this(host, port, username, password, null, null, null, null, 0, 0);
    }

    public HttpUtils(String host,
                     Integer port,
                     String username,
                     String password,
                     String proxyHost,
                     Integer proxyPort,
                     String proxyUsername,
                     String proxyPassword,
                     Integer connectionTimeout,
                     Integer readTimeout) {

        if (httpClient == null) {
            httpClient = new HttpClient();
        }
        // set host
        if (port != null && host != null) {
            httpClient.getHostConfiguration().setHost(host, port);
        }

        // set proxy
        if (proxyHost != null && proxyPort != null) {
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
        }
        // set proxy username & password

        if (proxyPassword != null && proxyUsername != null) {
            final Credentials creds = new UsernamePasswordCredentials(proxyUsername, proxyPassword);
            httpClient.getState().setProxyCredentials(AuthScope.ANY, creds);
        }
        // set username & password

        if (username != null && password != null) {
            final Credentials creds = new UsernamePasswordCredentials(username, password);
            httpClient.getState().setCredentials(AuthScope.ANY, creds);
        }
        // set connection/read timeout
        final HttpConnectionManagerParams managerParams = httpClient.getHttpConnectionManager().getParams();
        if (connectionTimeout != null) {
            managerParams.setConnectionTimeout(connectionTimeout);
        }

        if (readTimeout != null) {
            managerParams.setSoTimeout(readTimeout);
        }

    }

    public HttpUtils(String host, int port, String username, String password, int connectionTimeout, int readTimeout) {
        this(host, port, username, password, null, null, null, null, connectionTimeout, readTimeout);
    }

    @Autowired
    public HttpUtils(StringUtils stringUtils) {
        this.stringUtils = stringUtils;
    }

    @Autowired
    public HttpUtils(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Decodifica cierto String al recibirlo via http
     *
     * @param value  String a decodificar
     * @param format String con el formato a aplicar
     * @return String con el value decodificado usando el format
     */
    public String decode(String value, String format) {
        if (org.apache.commons.lang3.StringUtils.isBlank(value)) {
            return "";
        }
        try {
            String temp = value;
            //Validating the reserved characteres for specified format...
            for (int i = 0; i < codes.size(); i++) {
                temp = temp.replaceAll(StringUtils.DOUBLE_QUOTE + codes.get(i) + StringUtils.DOUBLE_QUOTE,
                        StringUtils.DOUBLE_QUOTE + chars.get(i) + StringUtils.DOUBLE_QUOTE);
            }

            //Decoding the value...
            return URLDecoder.decode(temp, format);
        } catch (Exception e) {
            log.error("Error in DECODE, decoding: <" + value + ">. ", e);
            return value; // "" ?
        }
    }

    /**
     * Codifica cierto String para enviarlo con seguridad por http
     *
     * @param value  String a codificar
     * @param format String con el formato a aplicar
     * @return String con el value codificado usando el format
     */
    public String encode(String value, String format) {
        if (org.apache.commons.lang3.StringUtils.isBlank(value)) {
            return "";
        }
        try {
            String temp = value;
            //Validating the reserved characteres for specified format...
            for (int i = 0; i < chars.size(); i++) {
                temp = temp.replaceAll(StringUtils.DOUBLE_QUOTE + chars.get(i) + StringUtils.DOUBLE_QUOTE,
                        StringUtils.DOUBLE_QUOTE + codes.get(i) + StringUtils.DOUBLE_QUOTE);
            }

            //Encoding the value...
            return URLEncoder.encode(temp, format);
        } catch (Exception e) {
            log.error("Error in ENCODE, encoding: <" + value + ">. ", e);
            return value; // "" ?
        }
    }

    /**
     * Metodo utilitario a usar para "traducir" ciertos Strings "problematicos"
     *
     * @param character String a "traducir"
     * @return String con la traduccion
     */
    public String getCodesByChars(String character) {
        int index = chars.indexOf(character);
        return (index == -1) ? "" : (String) codes.get(index);
    }

    public String getContent(String url)
            throws Exception {
        return getContent(url, "", "text/plain", "UTF-8", "text/plain", "UTF-8", METHOD_TYPE.GET);
    }

    public String getContent(String url,
                             String request,
                             String requestContentType,
                             String requestCharset,
                             String suggestedResponseContentType,
                             String suggestedResponseCharset,
                             METHOD_TYPE methodType)
            throws Exception {
        if (httpClient == null) {
            throw new IllegalStateException("httpClient has not been setted. Please initialize HttpUtil instance " +
                    "thru" + " " + "the appropiate Constructor before invoke this method. For example: public " +
                    "HttpUtil(String " + "" + "proxyHost, int proxyPort, String username, String password, String " +
                    "proxyUsername, String " + "proxyPassword, int connectionTimeout, int readTimeout);");
        }
        HttpMethod httpMethod = HttpMethodFactory.get(methodType, url);

        if (httpMethod != null) {
            httpMethod.setRequestHeader("MimeType", requestContentType);
            httpMethod.setRequestHeader("Charset", requestCharset);
            httpMethod.setRequestHeader("Accept", suggestedResponseContentType);
            httpMethod.setRequestHeader("Accept-Charset", suggestedResponseCharset);

            httpMethod.setQueryString(request);
            try {
                httpClient.executeMethod(httpMethod);
                return httpMethod.getResponseBodyAsString();
            } catch (final Exception e) {
                throw new Exception("Status Code: " + String.valueOf(httpMethod.getStatusCode()) + " | " + e
                        .getMessage());
            } finally {
                httpMethod.releaseConnection();
            }
        }
        return null;
    }

    public String getContent(String url, String request)
            throws Exception {
        return getContent(url, request, "text/plain", "UTF-8", "text/plain", "UTF-8", METHOD_TYPE.GET);
    }

    public String getContent(String url, String request, String requestContentType)
            throws Exception {
        return getContent(url, request, requestContentType, "UTF-8", "text/plain", "UTF-8", METHOD_TYPE.GET);
    }

    public String getContent(String url, String request, String requestContentType, String requestCharset)
            throws Exception {
        return getContent(url, request, requestContentType, requestCharset, "text/plain", "UTF-8", METHOD_TYPE.GET);
    }

    public InputStream getContentAsStream(String url)
            throws Exception {
        return getContentAsStream(url, "", "text/plain", "UTF-8", "text/plain", "UTF-8", METHOD_TYPE.GET);
    }

    public InputStream getContentAsStream(String url,
                                          String request,
                                          String requestContentType,
                                          String requestCharset,
                                          String suggestedResponseContentType,
                                          String suggestedResponseCharset,
                                          METHOD_TYPE methodType)
            throws Exception {
        if (httpClient == null) {
            throw new IllegalStateException("httpClient has not been setted. Please initialize HttpUtil instance " +
                    "thru" + " " + "the appropiate Constructor before invoke this method. For example: public " +
                    "HttpUtil(String " + "" + "proxyHost, int proxyPort, String username, String password, String " +
                    "proxyUsername, String " + "proxyPassword, int connectionTimeout, int readTimeout);");
        }
        HttpMethod httpMethod = HttpMethodFactory.get(methodType, url);

        if (httpMethod != null) {
            httpMethod.setRequestHeader("MimeType", requestContentType);
            httpMethod.setRequestHeader("Charset", requestCharset);
            httpMethod.setRequestHeader("Accept", suggestedResponseContentType);
            httpMethod.setRequestHeader("Accept-Charset", suggestedResponseCharset);

            httpMethod.setQueryString(request);
            try {
                httpClient.executeMethod(httpMethod);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                IOUtils.copy(httpMethod.getResponseBodyAsStream(), outputStream);
                return new ByteArrayInputStream(outputStream.toByteArray());
            } catch (final Exception e) {
                throw new Exception("Status Code: " + String.valueOf(httpMethod.getStatusCode()) + " | " + e
                        .getMessage());
            } finally {
                // release connection
                httpMethod.releaseConnection();
            }
        }
        return null;
    }

    public InputStream getContentAsStream(String url, String request)
            throws Exception {
        return getContentAsStream(url, request, "text/plain", "UTF-8", "text/plain", "UTF-8", METHOD_TYPE.GET);
    }

    public InputStream getContentAsStream(String url, String request, String requestContentType)
            throws Exception {
        return getContentAsStream(url, request, requestContentType, "UTF-8", "text/plain", "UTF-8", METHOD_TYPE.GET);
    }

    public InputStream getContentAsStream(String url, String request, String requestContentType, String requestCharset)
            throws Exception {
        return getContentAsStream(url,
                request,
                requestContentType,
                requestCharset,
                "text/plain",
                "UTF-8",
                METHOD_TYPE.GET);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Retorna el valor de cierto parametro o atributo del request http
     * Busca en el siguiente orden:
     * - Parametros del Request
     * - Atributos del Request
     * - Atributos de la Session
     *
     * @param request HttpServletRequest a revisar
     * @param session HttpSession a revisar
     * @param key     String clave del parametro o atributo a buscar
     * @return String con el valor del parametro indicado
     */
    public String getValue(HttpServletRequest request, HttpSession session, String key) {
        try {
            String value = request.getParameter(key);
            value = (value == null) ? (String) request.getAttribute(key) : value;
            return (value == null) ? (String) session.getAttribute(key) : value;
        } catch (Exception e) {
            log.error("Error getting value of " + key, e);
            return null; // "" ?
        }
    }

    /**
     * Retorna el valor de cierto parametro o atributo del request http
     * Busca en los parametros, y luego en los atributos, en ese orden.
     *
     * @param request HttpServletRequest a revisar
     * @param key     String clave del parametro o atributo a buscar
     * @return String con el valor del parametro indicado
     */
    public String getValueFromRequest(HttpServletRequest request, String key) {
        try {
            String value = request.getParameter(key);
            return (value == null) ? (String) request.getAttribute(key) : value;
        } catch (Exception e) {
            log.error("Error getting value of " + key, e);
            return null; // "" ?
        }
    }

    /**
     * Llena un HttpServletRequest con la informacion suministrada en el mapa
     *
     * @param request HttpServletRequest a ser llenado
     * @param has     Hashtable con la data a usar
     * @return HttpServletRequest original con la data agregada
     */
    public HttpServletRequest populateRequest(HttpServletRequest request, Hashtable has) {
        for (Object o : has.keySet()) {
            String key = (String) o;
            String value = (String) has.get(key);
            request.setAttribute(key, value);
        }
        return request;
    }

    /**
     * Imprime por log la informacion en el request y la sesion http
     *
     * @param request HttpServletRequest to be logged
     * @param session HttpSession to be logged
     * @deprecated This method is for debug purposes only! Don't integrate your changes while using it.
     */
    public void printRequest(HttpServletRequest request, HttpSession session) {
        printRequest(request, session, null);
    }

    /**
     * Imprime por log la informacion en el request y la sesion http
     *
     * @param request HttpServletRequest to be logged
     * @param session HttpSession to be logged
     * @param log     Logger to be used
     * @deprecated This method is for debug purposes only! Don't integrate your changes while using it.
     */
    public void printRequest(HttpServletRequest request, HttpSession session, Logger log) {
        try {
            if (HttpUtils.log == null) {
                HttpUtils.log = log;
                log.debug("Logger not found.  Using default logger to " + HttpUtils.class.getName());
            }
            if (request == null) {
                log.debug("Request not found or null.");
            } else {

                // Attributes
                Enumeration en = request.getAttributeNames();
                while (en.hasMoreElements()) {
                    String o = (String) en.nextElement();
                    log.debug("Attribute (r) '" + o + "' = '" + request.getAttribute(o) + "' (" + (request.getAttribute(
                            o) == null ? "" : "not ") + "is null         )");
                }

                // Parameters
                en = request.getParameterNames();
                while (en.hasMoreElements()) {
                    String o = (String) en.nextElement();
                    log.debug("Parameter (r) '" + o + "' = '" + request.getParameter(o) + "' (" + (request.getParameter(
                            o) == null ? "" : "not ") + "is null)");
                }
            }
        } catch (Exception e) {
            log.debug("Error printing request data", e);
        }

        try {
            if (session == null) {
                log.debug("Session not found or null.");
                log.debug("Looking for Request's Session.");
                session = request != null ? request.getSession() : null;
            }

            if (session == null) {
                log.debug("Session not found or null.");
            } else {

                // Attributes
                Enumeration en = session.getAttributeNames();
                while (en.hasMoreElements()) {
                    String o = (String) en.nextElement();
                    log.debug("Attribute (s) '" + o + "' = '" + session.getAttribute(o) + "' (" + (session.getAttribute(
                            o) == null ? "" : "not ") + "is null)");
                }
            }
        } catch (Exception e) {
            log.debug("Error printing session data", e);
        }
    }

    public enum METHOD_TYPE {
        OPTIONS,
        GET,
        HEAD,
        POST,
        PUT,
        DELETE,
        TRACE,
        CONNECT
    }

    private static class HttpMethodFactory {

        public static HttpMethod get(METHOD_TYPE methodType, String uri) {
            if (METHOD_TYPE.OPTIONS.equals(methodType)) {
                return new OptionsMethod(uri);
            } else if (METHOD_TYPE.GET.equals(methodType)) {
                return new GetMethod(uri);
            } else if (METHOD_TYPE.HEAD.equals(methodType)) {
                return new HeadMethod(uri);
            } else if (METHOD_TYPE.POST.equals(methodType)) {
                return new PostMethod(uri);
            } else if (METHOD_TYPE.PUT.equals(methodType)) {
                return new PutMethod(uri);
            } else if (METHOD_TYPE.DELETE.equals(methodType)) {
                return new DeleteMethod(uri);
            } else if (METHOD_TYPE.TRACE.equals(methodType)) {
                return new TraceMethod(uri);
            } else {
                return null;
            }

        }
    }
}