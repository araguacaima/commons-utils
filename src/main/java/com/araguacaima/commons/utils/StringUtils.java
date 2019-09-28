/*
 * Copyright 2017 araguacaima
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.araguacaima.commons.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.atteo.evo.inflector.English;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Clob;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.CaseFormat.*;

@Component
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    public static final String AMPERSAND = "&";
    public static final String AMPERSAND_SYMBOL = "&";
    public static final String AND = "AND";
    public static final String AND_SYMBOL = AMPERSAND_SYMBOL + AMPERSAND_SYMBOL;
    public static final String ARROBA = "@";
    public static final String ASTERISK = "*";
    public static final String BACKSLASH = "\\";
    public static final String BLANK_SPACE = " ";
    public static final String CDATA_END = "]]>";
    public static final String CDATA_START = "<![CDATA[";
    public static final String COLON = ":";
    public static final String COLON_SYMBOL = ":";
    public static final String COMMA = ",";
    public static final String COMMA_SYMBOL = ",";
    public static final String DASH = "-";
    public static final String DASHES = DASH + DASH + DASH + DASH;
    public static final String DOLLAR = "$";
    public static final String DOT = ".";
    public static final String DOTS = DOT + DOT + DOT;
    public static final String DOUBLEBACKSLASH = BACKSLASH + BACKSLASH;
    public static final String DOUBLE_QUOTE = "\"";
    public static final char EMPTY_CHAR = ' ';
    public static final String EQUALS_SYMBOL = "=";
    public static final String DOUBLE_EQUALS_SYMBOL = EQUALS_SYMBOL + EQUALS_SYMBOL;
    public static final String EQUAL_SYMBOL = "=";
    public static final String FALSE = "false";
    public static final String GREATER_SYMBOL = ">";
    public static final String GREATER_EQUALS_SYMBOL = GREATER_SYMBOL + EQUALS_SYMBOL;
    public static final String GREATER_THAN_SYMBOL = ">";
    public static final String HTML_BLANK_SPACE = "&nbsp;";
    public static final List<String> HTML_COMPLETED_TAG_FIELDS = Collections.singletonList("font");
    public static final List<String> HTML_UNCOMPLETED_TAG_FIELDS = Collections.singletonList("img");
    public static final String LESS_SYMBOL = "<";
    public static final String LESS_EQUALS_SYMBOL = LESS_SYMBOL + EQUALS_SYMBOL;
    public static final String LESS_THAN_SYMBOL = "<";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    //No usar. Se deja por retro-compatibilidad
    public static final String LINE_JUMPED = LINE_SEPARATOR;
    public static final String NA = "N/A";
    public static final char NEW_LINE = '\n';
    public static final String NOT_SYMBOL = "!";
    public static final String NOT_EQUALS_SYMBOL = NOT_SYMBOL + EQUALS_SYMBOL;
    public static final String NULL = "null";
    public static final String OR = "OR";
    public static final String PERCENTAGE_SYMBOL = "%";
    public static final String PIPE = "|";
    public static final String OR_SYMBOL = PIPE + PIPE;
    public static final String PLUS = "+";
    public static final String QUESTION_SYMBOL = "?";
    public static final String QUOTE = "\'";
    public static final String SCORE = "-";
    public static final String SEMICOLON_SYMBOL = ";";
    public static final String SEMI_COLON = ";";
    public static final String SINGLE_QUOTE = "\'";
    public static final String SLASH = "/";
    public static final String SPACE = " ";
    public static final char TAB = '\t';
    public static final String TRUE = "true";
    public static final String UNDERSCORE = "_";
    public static final int UNICODE_HEX_LENGTH = 4;
    private final String[] DEFAULT_STOP_FILTERS = {"junit.framework.TestCase.runTest",
            "junit.framework.TestSuite.runTest"};
    private final String[] DEFAULT_TRACE_FILTERS = {"junit.framework.TestCase",
            "junit.framework.TestResult",
            "junit.framework.TestSuite",
            "junit.framework.Assert.",
            "junit.swingui.TestRunner",
            "junit.awtui.TestRunner",
            "junit.textui.TestRunner",
            "java.lang.reflect.Method.invoke(",
            "org.apache.tools.ant."};
    private final ExceptionUtils exceptionUtils;
    private final Logger log = LoggerFactory.getLogger(StringUtils.class);
    private final NotNullOrEmptyStringPredicate notNullOrEmptyStringPredicate;

    @Autowired
    public StringUtils(NotNullOrEmptyStringPredicate notNullOrEmptyStringPredicate, ExceptionUtils exceptionUtils) {
        this.notNullOrEmptyStringPredicate = notNullOrEmptyStringPredicate;
        this.exceptionUtils = exceptionUtils;
    }

    public static int firstIndexOf(String input, Collection tokens) {
        int firstIndexOf = input.length() + 1;
        for (Object token1 : tokens) {
            String token = (String) token1;
            int newIndexOf = input.indexOf(token);
            if (newIndexOf != -1 & newIndexOf <= firstIndexOf) {
                firstIndexOf = newIndexOf;
            }
        }
        return firstIndexOf > input.length() ? -1 : firstIndexOf;
    }

    /**
     * Trim characters in prefix
     *
     * @param str String
     * @param ch  character which has to be removed
     * @return null, if str is null, otherwise string will be returned
     * without character prefixed
     */
    public static String leftTrim(String str, char ch) {
        if (str == null) {
            return null;
        }
        int count = str.length();
        int len = str.length();
        int st = 0;
        int off = 0;
        char[] val = str.toCharArray();

        while ((st < len) && (val[off + st] == ch)) {
            st++;
        }

        return st > 0 ? str.substring(st, len) : str;
    }

    public static String getLastToken(String text, String separator) {
        return text.substring(text.lastIndexOf(separator) + 1);
    }

    /**
     * Dado un int countOfTabs, crea un String con dicho number de Tabs ('\t')
     *
     * @param countOfTabs int Numero de tabs a crear
     * @return String con countOfTabs Tabs
     */
    public String buildTabs(int countOfTabs) {
        StringBuilder tabs = new StringBuilder();
        for (int i = 0; i < countOfTabs; i++) {
            tabs.append("\t");
        }
        return tabs.toString();
    }

    public String centerTextFullFillingWithWithCharacter(String textToCenter,
                                                         int completeLengthToFullFilling,
                                                         char characterToFullFill) {
        StringBuilder result = new StringBuilder();
        if (textToCenter.length() >= completeLengthToFullFilling) {
            return textToCenter;
        } else {
            int lengthForFilling = completeLengthToFullFilling - textToCenter.length();
            int numbersOfCharacteresRight = lengthForFilling / 2;
            int numbersOfCharacteresLeft = lengthForFilling - numbersOfCharacteresRight;
            String charactersToAppendLeft = fullFillWithCharacter(numbersOfCharacteresLeft, characterToFullFill);
            String charactersToAppendRight = fullFillWithCharacter(numbersOfCharacteresRight, characterToFullFill);
            return charactersToAppendLeft.concat(textToCenter.concat(charactersToAppendRight));
        }
    }

    public String fullFillWithCharacter(int numberOfCharacteres, char characterToFullFill) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < numberOfCharacteres; i++) {
            result.append(characterToFullFill);
        }
        return result.toString();
    }

    /**
     * Convierte un Clob a un String.
     * Tomado de http://www.devx.com/tips/Tip/21397 (Diaz Jose Ramon)
     *
     * @param text Clob a ser transformado en String.
     * @return String con el contenido del Clob
     */
    public String clobToString(Clob text) {
        if (text == null) {
            return "";
        }
        try {
            StringBuilder strOut = new StringBuilder();
            String aux;
            // Accesamos como Stream para no usar CLOB.length()
            BufferedReader br = new BufferedReader(text.getCharacterStream());
            while ((aux = br.readLine()) != null) {
                strOut.append(aux);
            }
            return strOut.toString();
        } catch (Exception e) {
            log.error("Error traduciendo Clob a String", e);
            exceptionUtils.handleException(e);
            return null;
        }
    }

    public boolean contains(String input, Collection tokens) {
        for (Object token1 : tokens) {
            String token = (String) token1;
            if (input.equals(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indica si el String 'big' contiene exactamente al String 'lil',
     * asumiendo que 'big' sea una lista.
     *
     * @param big String
     * @param lil String
     * @param sep String
     * @return boolean
     */
    public boolean contains(String big, String lil, String sep) {
        return Arrays.asList(big.split(sep)).contains(lil);
    }

    /**
     * Valida si un String esta contenido en una Collection, sin importar la capitalizacion del
     * mismo
     *
     * @param list Collection a validar
     * @param str  String a buscar
     * @return boolean true si el String esta contenido, false si no.
     */
    public boolean containsIgnoreCase(Collection list, String str) {
        for (Object aList : list) {
            String text = (String) aList;
            if (text.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Dado un String input, retorna un String con sus caracteres en notacion html-unicode.
     * TODO: Sacar este metodo de aqui?
     *
     * @param input String a transformar.
     * @return String con los caracteres de input en notacion html-unicode.
     */
    public String convertToHtmlUnicode(String input) {
        StringBuilder htmlUnicode = new StringBuilder();
        char[] characters = input.toCharArray();
        for (char character : characters) {
            htmlUnicode.append("&#").append(Integer.toString(character)).append(";");
        }
        return htmlUnicode.toString();
    }

    /**
     * Dado un String input, retorna un String con sus caracteres en notacion hexadecimal-unicode.
     * Especialmente util para imprimir caracteres extranios.
     * TODO: Sacar este metodo de aqui?
     *
     * @param input String a transformar.
     * @return String con los caracteres de input en notacion hexadecimal-unicode.
     */
    public String convertToUnicode(String input) {
        StringBuilder unicode = new StringBuilder();
        char[] characters = input.toCharArray();
        for (char character : characters) {
            String code = Integer.toHexString(character);
            String codeNew = fillWithZeros(code);
            unicode.append("\\u").append(codeNew);
        }
        return unicode.toString();
    }

    /**
     * Dado un String, lo llena con ceros hasta el UNICODE_HEX_LENGTH.
     * Metodo de soporte para el convertToUnicode(String)
     * TODO: Sacar este metodo de aqui?
     *
     * @param code String a completar
     * @return String completado con ceros
     */
    private String fillWithZeros(String code) {
        if (code.length() < UNICODE_HEX_LENGTH) {
            StringBuilder codeBuilder = new StringBuilder(code);
            for (int i = 0; i < UNICODE_HEX_LENGTH - codeBuilder.length(); i++) {
                codeBuilder.insert(0, "0");
            }
            code = codeBuilder.toString();
        }
        return code;
    }

    public String decodeUTF8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String deleteAll(String input, Collection tokens) {
        return replaceAll(input, tokens, EMPTY);
    }

    public String replaceAll(String input, Collection tokens, String value) {
        for (Object token1 : tokens) {
            String token = (String) token1;
            input = input.replaceAll(encloseEachCharacterOnlyPrefix(token, BACKSLASH), value);
        }
        return input;
    }

    public String encloseEachCharacterOnlyPrefix(String input, String enclosePrefix) {
        char[] chars = input.toCharArray();
        StringBuilder result = new StringBuilder();

        for (char aChar : chars) {
            result.append(enclosePrefix).append(aChar);
        }
        return result.toString();
    }

    /**
     * Si el String es null, le setea como valor "".
     * Sobrecarga por comodidad de defaultIfNull(String, String)
     *
     * @param value String
     * @return String
     */
    public String emptyIfNull(String value) {
        return defaultIfNull(value, EMPTY);
    }

    /**
     * Si el String es null, le setea el valor de defaultValue
     *
     * @param value        String
     * @param defaultValue String
     * @return String
     */
    public String defaultIfNull(String value, String defaultValue) {
        return (value == null || value.equals("null") ? defaultValue : value);
    }

    public String emptyIfNull(Object value) {
        return null == value ? EMPTY : value.toString();
    }

    public String enclose(String input, String encloseIn, String encloseOut) {
        return encloseIn + input + encloseOut;
    }

    public String encloseOnlyPrefix(String input, String enclosePrefix) {
        return enclosePrefix + input;
    }

    public String encloseOnlySuffix(String input, String encloseSuffix) {
        return input + encloseSuffix;
    }

    public byte[] encodeUTF8(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    public String filterStack(String stack) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StringReader sr = new StringReader(stack);
        BufferedReader br = new BufferedReader(sr);
        do {
            try {
                String line;
                if ((line = br.readLine()) == null) {
                    break;
                }
                if (stopLine(line)) {
                    pw.println(line);
                    break;
                }
                if (!filterLine(line)) {
                    pw.println(line);
                }
            } catch (Exception IOException) {
                return stack;
            }
        } while (true);
        return sw.toString();
    }

    private boolean stopLine(String line) {
        for (String DEFAULT_STOP_FILTER : DEFAULT_STOP_FILTERS) {
            if (line.indexOf(DEFAULT_STOP_FILTER) > 0) {
                return true;
            }
        }

        return false;
    }

    private boolean filterLine(String line) {
        for (String DEFAULT_TRACE_FILTER : DEFAULT_TRACE_FILTERS) {
            if (line.indexOf(DEFAULT_TRACE_FILTER) > 0) {
                return true;
            }
        }

        return false;
    }

    public String firstToken(String input, Collection tokens) {
        for (Object token1 : tokens) {
            String token = (String) token1;
            if (input.startsWith(token)) {
                return token;
            }
        }
        return EMPTY;
    }

    /**
     * Elimina todos los '\n' del String recibido
     *
     * @param toFlat String
     * @return 'flattened' String
     */
    public String flatten(String toFlat) {
        return toFlat.replaceAll("[\n\r\f]", "");
    }

    public String fullFillWithBlankSpace(String textToFullFilling, int completeLengthToFullFilling) {
        return fullFillWithWithCharacter(textToFullFilling, completeLengthToFullFilling, BLANK_SPACE.toCharArray()[0]);
    }

    public String fullFillWithWithCharacter(String textToFullFilling,
                                            int completeLengthToFullFilling,
                                            char characterToFullFill) {
        StringBuilder result = new StringBuilder();
        if (textToFullFilling.length() >= completeLengthToFullFilling) {
            return textToFullFilling;
        } else {
            return textToFullFilling.concat(fullFillWithBlankSpace(completeLengthToFullFilling - textToFullFilling
                    .length()));
        }
    }

    public String fullFillWithBlankSpace(int numberOfBlackSpaces) {
        return fullFillWithCharacter(numberOfBlackSpaces, BLANK_SPACE.toCharArray()[0]);
    }

    public byte[] getBytesFromFile(File file)
            throws IOException {

        InputStream is = new FileInputStream(file); // Get the size of the file
        long length = file.length(); // You cannot create an array using a long type.

        //        if (length > Integer.MAX_VALUE) {
        //        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes

        int offset = 0;
        int numRead;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public String getSpecialStringWithDoubleTypeFormat(String value, int initDecimalIndex) {
        String integerValue = value.substring(0, initDecimalIndex);
        String decimals = value.substring(initDecimalIndex);
        return integerValue + DOT + decimals;
    }

    /**
     * Dado un String input de entrada, crea un InputStream que lo lee
     *
     * @param input    String a leer
     * @param encoding String con el encoding a usar para leer el input
     * @return InputStream con el contenido de input
     */
    public InputStream getStream(String input, String encoding) {
        ByteArrayInputStream stream = null;
        try {
            log.debug("encoding = " + encoding);
            if (isBlank(encoding)) {
                stream = new ByteArrayInputStream(input.getBytes());
            } else {
                stream = new ByteArrayInputStream(input.getBytes(encoding));
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error obteniendo un InputStream para '" + input + "'", e);
        }
        return stream;
    }

    public List getStringFromByteArray(byte[] byteFile) {
        return getStringFromByteArray(byteFile, true);
    }

    public List getStringFromByteArray(byte[] byteFile, boolean acceptEmptyLines) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        boolean newLineAdded = false;

        for (byte myByte : byteFile) {
            if (isNewLine(myByte)) {
                if (acceptEmptyLines) {
                    lines.add(line.toString());
                    line = new StringBuilder();
                } else {
                    if (!newLineAdded) {
                        lines.add(line.toString());
                        line = new StringBuilder();
                        newLineAdded = true;
                    }
                }
            } else {
                line.append((char) myByte);
                if (!acceptEmptyLines) {
                    newLineAdded = false;
                }
            }
        }
        lines.add(line.toString());
        return lines;
    }

    /**
     * @param byte_ The byte to inspect if it is a new line
     * @return Whether the provided byte is a new line or not
     */
    private boolean isNewLine(byte byte_) {
        //TODO revisar una mejor forma de implementar, usando el LINE_SEPARATOR
        char line = System.getProperty("line.separator").charAt(0);
        return byte_ == '\r' || byte_ == '\n' || byte_ == '\f' || byte_ == line;
    }

    public String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }

        return sb.toString();

    }

    public String getStringWithDoubleTypeFormat(String value) {
        String valueWithoutDots = value.replaceAll("\\.", "");
        return valueWithoutDots.replaceAll(",", ".");
    }

    /**
     * Valida que se reciba una cadena de solo letras y el espacio.
     *
     * @param wordWannabe String to validate if contains only characters and spaces
     * @return Whether the string has only characters and spaces or not
     */
    public boolean hasOnlyCharsAndSpace(String wordWannabe) {
        String patron = "^[a-zA-Z\\u00f1\\u00d1\\s]+$";
        //        String patron = "^[a-zA-Z]+(\\s[a-zA-Z]+)?$"; // 2 nombres
        //        String patron = "^[a-zA-Z]+(\\s[a-zA-Z]+){0,3}$"; // 4 nombres
        return (wordWannabe != null) && Pattern.compile(patron).matcher(wordWannabe).matches();
    }

    /**
     * Valida que se reciba una palabra, o n palabras separadas por un espacio.
     * Se le debe indicar la cantidad maxima de palabras que debe contener la cadena.
     *
     * @param wordWannabe String to validate
     * @param words       Maximum number of words the string should contain
     * @return Whether the string has only characters and spaces or not
     */
    public boolean hasOnlyCharsAndSpace(String wordWannabe, int words) {
        if (words < 1) {
            log.debug("Se recibio un parametro errado: " + words);
            return false;
        } else {
            String pattern = "^[a-zA-Z\\u00f1\\u00d1]+(\\s[a-zA-Z\\u00f1\\u00d1]+)*" + (words - 1) + "}$"; // n
            // palabras
            return (wordWannabe != null) && Pattern.compile(pattern).matcher(wordWannabe).matches();
        }
    }

    public String htmlText(String text) {
        StringBuilder sb = new StringBuilder();
        if (text == null) {
            return "";
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case 60: // '<'
                    sb.append("&lt;");
                    break;

                case 62: // '>'
                    sb.append("&gt;");
                    break;

                case 38: // '&'
                    sb.append("&amp;");
                    break;

                case 34: // '"'
                    sb.append("&quot;");
                    break;

                case 10: // '\n'
                    sb.append("<br>");
                    break;

                default:
                    sb.append(c);
                    break;
            }
        }

        return sb.toString();
    }

    public boolean isEmpty(Object string) {
        return (null == string) || (string instanceof String && EMPTY.equals(((String) string).trim()));
    }

    public boolean isEmptyOrNullValue(Object string) {
        if (null == string) {
            return true;
        } else if (string instanceof String) {
            String trimmedValue = ((String) string).trim();
            return EMPTY.equals(trimmedValue) || (NULL.equalsIgnoreCase(trimmedValue));
        } else {
            return false;
        }
    }

    /**
     * Retorna true si el String es un number entero valido, false si no lo es.
     *
     * @param string String a validar
     * @return true si string es un number entero valido, false si no lo es
     */
    public boolean isNumber(String string) {
        return !isEmpty(string) && string.matches("-?[0-9]+");
    }

    public boolean isOnlyChars(String wordWannabe) {
        return (wordWannabe != null) && Pattern.compile("^[a-zA-Z\\u00f1\\u00d1]+$").matcher(wordWannabe).matches();
    }

    /**
     * Revisa si el char quoteWannabe es una comilla doble de cualquier tipo.
     *
     * @param quoteWannabe char a validar si es una comilla o no
     * @return true si quoteWannabe es una comilla, false si no lo es
     */
    public boolean isQuote(char quoteWannabe) {
        return ((quoteWannabe == UnicodeUtils.COMILLA_DOBLE_1) || (quoteWannabe == UnicodeUtils.COMILLA_DOBLE_2) ||
                (quoteWannabe == UnicodeUtils.COMILLA_DOBLE_3));
    }

    /**
     * Agrega un char pad a un String, por la izquierda, hasta que llegue a numchar
     *
     * @param s       String
     * @param numchar int
     * @param pad     char
     * @return String
     */
    public String leftPadString(String s, int numchar, char pad) {
        if (null == s) {
            s = "";
        }
        int length = s.length();
        if (numchar <= length) {
            return s;
        } else {
            StringBuilder sb = new StringBuilder(numchar);
            sb.append(s);
            for (int i = 0; i < (numchar - length); i++) {
                sb.insert(0, pad);
            }
            return sb.toString();
        }
    }

    /**
     * Retorna null si el String es vacio, o el String original si no lo es.
     *
     * @param s The given string
     * @return null if s is empty, s otherwise
     */
    public String nullIfEmpty(String s) {
        return (isEmpty(s) ? null : s);
    }

    public String removeDigitsPrefixFromValue(String digit, String value) {
        return value.replaceAll("^" + digit + "*", "");
    }

    /**
     * Elimina los espacios en blanco en exceso en el String sentence.
     *
     * @param sentence String a ser trimeado.
     * @return String con solamente un espacio en blando entre cada dos palabras
     */
    public String removeExtraBlanks(String sentence) {
        String SPACES_PATTERN = "[\\s]+";
        Pattern p = Pattern.compile(SPACES_PATTERN);
        Matcher m = p.matcher(sentence);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, SPACE);
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
    }

    public boolean representADouble(String value) {
        try {
            new Double(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean representAFloat(String value) {
        try {
            new Float(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean representAnInteger(String value) {
        try {
            new Integer(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Agrega un char pad a un String, por la derecha, hasta que llegue a numchar
     *
     * @param s       String
     * @param numchar int
     * @param pad     char
     * @return String
     */
    public String rightPadString(String s, int numchar, char pad) {
        if (null == s) {
            s = "";
        }
        int length = s.length();
        if (numchar <= length) {
            return s.substring(0, numchar);
        } else {
            StringBuilder sb = new StringBuilder(numchar);
            sb.append(s);
            for (int i = 0; i < (numchar - length); i++) {
                sb.append(pad);
            }
            return sb.toString();
        }
    }

    /**
     * Trim characters in suffix
     *
     * @param str String
     * @param ch  character which has to be removed
     * @return null, if str is null, otherwise string will be returned
     * without character suffixed
     */
    public String rightTrim(String str, char ch) {
        if (str == null) {
            return null;
        }
        int count = str.length();
        int len = str.length();
        int st = 0;
        int off = 0;
        char[] val = str.toCharArray();

        while ((st < len) && (val[off + len - 1] == ch)) {
            len--;
        }
        return len < count ? str.substring(st, len) : str;
    }

    /**
     * Separa un String dado en tokens usando COMMA como delimitador.
     * Sobrecarga por comodidad de uso del metodo splitAsList(String, String).
     *
     * @param str String a separar.
     * @return un arreglo con los tokens obtenidos.  Si str es null, retorna una lista vacia.
     */
    public List splitAsList(String str) {
        return splitAsList(str, COMMA);
    }

    /**
     * Separa un String dado en tokens usando los delimitadores indicados.
     *
     * @param str       String a separar.
     * @param delimiter String con los caracteres a usar como delimitadores.
     * @return un arreglo con los tokens obtenidos.  Si str es null, retorna una lista vacia.
     */
    public List splitAsList(String str, String delimiter) {
        if (isEmpty(str)) {
            // log.warn("El String de entrada es null...");
            return new ArrayList<>();
        } else {
            return new ArrayList<>(Arrays.asList(str.split(delimiter)));
        }
    }

    // Inicio de metodos de SICAM
    // TODO: Sincerar que metodos son utiles

    public Collection splitBySeparators(String input, Collection separators) {
        if (isNotBlank(input)) {
            String separatorMask = String.valueOf(System.currentTimeMillis());
            for (Object separator1 : separators) {
                String separator = (String) separator1;
                input = input.replaceAll(encloseEachCharacterOnlyPrefix(separator, BACKSLASH), separatorMask);
            }
            Collection result = Arrays.asList(input.split(separatorMask));
            CollectionUtils.transform(result, o -> ((String) o).trim());
            return CollectionUtils.select(result, notNullOrEmptyStringPredicate);
        } else {
            return new ArrayList();
        }
    }

    public String substringIfLonger(String cadena, int limite) {
        return substringIfLonger(cadena, limite, "");
    }

    public String substringIfLonger(String cadena, int limite, String postFix) {
        if (null == cadena) {
            return null;
        } else if (limite < 0) {
            return cadena;
        } else if (cadena.length() > limite) {
            return cadena.substring(0, limite) + postFix;
        } else {
            return cadena;
        }
    }

    //    public  void main(String[] args) {
    //        String test = "<span class=\"colorLink\" >64280</span>";
    //        log.debug("args = " + test.replaceAll("<span class=\"colorLink\" >",
    // "").replaceAll("</span>", ""));
    //    }

    public String toParragraph(String s) {
        if (isBlank(s)) {
            return s;
        }
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    /**
     * Converts String[] to Vector&lt;String&gt;
     *
     * @param str String[]
     * @return Vector
     * @deprecated use {@link java.util.Arrays#asList(Object[])} instead. If multi-threading
     * support is needed, use {@link java.util.Collections#synchronizedList(java.util.List)}
     */
    public Vector toVector(String[] str) {
        Vector<String> result = new Vector<>();
        if (str != null) {
            int i = 0;
            while (i < str.length) {
                result.add(i, str[i]);
                i++;
            }
        }
        return result;
    }

    // Final de metodos de SICAM

    public String transformsCollectionIntoString(Collection<String> tokens) {
        final StringBuilder result = new StringBuilder();
        IterableUtils.forEach(tokens, result::append);
        return result.toString();
    }

    public String transformsCollectionIntoStringWithSeperator(Collection<Object> tokens, final String separator) {
        final StringBuffer result = new StringBuffer();
        IterableUtils.forEach(tokens, o -> result.append(o).append(separator));
        result.replace(result.length() - 1, result.length(), EMPTY);
        return result.toString();
    }

    public String transformsCollectionIntoStringWithSeperatorDemarcatedBy(Collection<Object> tokens,
                                                                          final String separator,
                                                                          final String demarcation) {
        final StringBuffer result = new StringBuffer();
        IterableUtils.forEach(tokens, o -> result.append(demarcation).append(o).append(demarcation).append(separator));
        result.replace(result.length() - 1, result.length(), EMPTY);
        return result.toString();
    }

    //    public  void main(String[] args) {
    //        log.debug("a = " + substringIfLonger(null, -1, "..."));
    //        log.debug("a = " + substringIfLonger("pepe", -1, "..."));
    //        log.debug("a = " + substringIfLonger("pepe", 0, "..."));
    //        log.debug("a = " + substringIfLonger("pepe", 1, "..."));
    //        log.debug("a = " + substringIfLonger("pepe", 4, "..."));
    //        log.debug("a = " + substringIfLonger("pepe", 5, "..."));
    ////        log.debug("a = " + "pepe".substring(0, 5));
    //    }

/*
    public   List readFileTextToList(String file){
      List lines = new ArrayList();

      try{

      FileInputStream fstream = new FileInputStream(file);
      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String strLine;

      while ((strLine = br.readLine()) != null)   {
        lines.add(strLine);
      }

      in.close();

      }catch (Exception e){
        log.error("Error: " + e.getMessage());
      }

      return lines;
      }
*/

    /**
     * Trim characters in prefix and suffix
     *
     * @param str String
     * @param ch  character which has to be removed
     * @return null, if str is null, otherwise string will be returned
     * without character prefixed/suffixed
     */
    public String trim(String str, char ch) {
        if (str == null) {
            return null;
        }
        int count = str.length();
        int len = str.length();
        int st = 0;
        int off = 0;
        char[] val = str.toCharArray();

        while ((st < len) && (val[off + st] == ch)) {
            st++;
        }
        while ((st < len) && (val[off + len - 1] == ch)) {
            len--;
        }
        return ((st > 0) || (len < count)) ? str.substring(st, len) : str;
    }

    /**
     * Dado un arreglo, crea un String con los elementos del mismo,
     * separados por el delimitador indicado.
     * NOTA: Si se les ocurre un mejor nombre que unSplit, por favor avisenme.
     *
     * @param elementos   Arreglo a usar
     * @param delimitador Separador para el String final
     * @return String
     */
    public String unsplit(Collection<Object> elementos, String delimitador) {
        return unsplit(elementos, delimitador, "", "");
    }

    /**
     * Dado un arreglo, crea un String con los elementos del mismo,
     * separados por el delimitador indicado.
     * NOTA: Si se les ocurre un mejor nombre que unSplit, por favor avisenme.
     * Copiamos aqui el codigo de unsplit(Object[], String, String, String)
     * para evitar cualquier posible problema de performance
     *
     * @param elementos   Arreglo a usar
     * @param delimitador Separador para el String final
     * @param prev        Prefijo a colocar antes del elemento (ej. comilla que abre)
     * @param post        Postfijo a colocar luego del elemento (ej. comilla que cierra)
     * @return String
     */
    public String unsplit(Collection elementos, String delimitador, String prev, String post) {
        if (elementos == null || elementos.size() == 0) {
            return "";
        }
        if (delimitador == null) {
            delimitador = "";
        }
        // StringBuilder result = new StringBuilder("");
        StringBuilder result = new StringBuilder();

        iterateElements(elementos, delimitador, prev, post, result);
        return result.toString();
    }

    private void iterateElements(Collection elements,
                                 String delimiter,
                                 String prev,
                                 String post,
                                 StringBuilder result) {
        for (Object element : elements) {
            result.append(prev).append(element).append(post);
        }
    }

    public String xmlText(String text) {
        StringBuilder sb = new StringBuilder();
        if (text == null) {
            return "";
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case 60: // '<'
                    sb.append("&lt;");
                    break;

                case 62: // '>'
                    sb.append("&gt;");
                    break;

                case 38: // '&'
                    sb.append("&amp;");
                    break;

                case 34: // '"'
                    sb.append("&quot;");
                    break;

                default:
                    sb.append(c);
                    break;
            }
        }

        return sb.toString();
    }

    public String singularizeTypeName(Class clazz) {
        String typeName = cleanClassName(clazz);
        return singularizeTypeName(typeName);
    }

    public String pluralizeTypeName(Class clazz) {
        String typeName = cleanClassName(clazz);
        return pluralizeTypeName(typeName);
    }

    @SuppressWarnings("DuplicateExpressions")
    public String singularizeTypeName(String clazz) {
        String typeName = cleanClassName(clazz);

        if (typeName.endsWith("ses")) {
            typeName = typeName.substring(0, typeName.length() - 3);
        } else if (typeName.endsWith("s")) {
            typeName = typeName.substring(0, typeName.length() - 1);
        } else if (typeName.endsWith("ees")) {
            typeName = typeName.substring(0, typeName.length() - 3);
        } else {
            typeName = English.plural(typeName, 1);
        }
        return typeName;
    }

    public String pluralizeTypeName(String clazz) {
        String typeName = cleanClassName(clazz);

        if (!typeName.endsWith("ses") && !typeName.endsWith("ees") && !typeName.endsWith("s")) {
            if (!typeName.endsWith("data") && !typeName.endsWith("Data")) {
                typeName = English.plural(typeName, 2);
            }
        }

        return typeName;
    }

    private String cleanClassName(String simpleName) {
        if (simpleName.startsWith("Dto") || simpleName.startsWith("dto")) {
            simpleName = simpleName.substring(3);
        }
        if (simpleName.endsWith("Dto")) {
            simpleName = simpleName.substring(0, simpleName.length() - 3);
        }
        return StringUtils.uncapitalize(simpleName);
    }

    private String cleanClassName(Class clazz) {
        String simpleName = clazz.getSimpleName();
        return cleanClassName(simpleName);
    }

    public String toSnake(String input) {
        return toSnake(input, true);
    }

    public String toSnake(String input, boolean forceUncapitalized) {
        String snakedInput = LOWER_CAMEL.to(LOWER_HYPHEN, input);
        if (forceUncapitalized) {
            snakedInput = StringUtils.uncapitalize(snakedInput);
        }
        return snakedInput;
    }

    public String splitWords(String input) {
        return splitWords(input, true);
    }

    public String splitWords(String input, boolean forceUncapitalized) {
        String[] underScoredInputList = input.split("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
        StringBuilder underScoredInput = new StringBuilder(StringUtils.EMPTY);
        for (String str : underScoredInputList) {
            underScoredInput.append(" ").append(forceUncapitalized ? StringUtils.uncapitalize(str) : str);
        }
        return underScoredInput.toString();
    }

    public String toUnderscore(String input) {
        return toUnderscore(input, true, true);
    }

    public String toUnderscore(String input, boolean forceUncapitalized, boolean uppercase) {
        String underScoredInput = LOWER_CAMEL.to((uppercase ? UPPER_UNDERSCORE : LOWER_UNDERSCORE), input);
        if (forceUncapitalized) {
            underScoredInput = StringUtils.uncapitalize(underScoredInput);
        }
        return underScoredInput;
    }

    public String fromUnderscore(String input) {
        return fromUnderscore(input, false, false);
    }

    public String fromUnderscore(String input, boolean forceUncapitalized, boolean uppercase) {
        String underScoredInput = UPPER_UNDERSCORE.to((uppercase ? UPPER_CAMEL : LOWER_CAMEL), input);
        if (forceUncapitalized) {
            underScoredInput = StringUtils.uncapitalize(underScoredInput);
        } else {
            underScoredInput = StringUtils.capitalize(underScoredInput);
        }
        return underScoredInput;
    }


}



