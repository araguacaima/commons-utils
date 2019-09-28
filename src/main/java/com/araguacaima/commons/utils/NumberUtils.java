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

import com.araguacaima.commons.exception.core.Exceptions;
import com.araguacaima.commons.exception.core.Severity;
import com.araguacaima.commons.exception.core.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Clase utilitaria para ayudar al manejo de los numbers
 * Tenemos varias formas de formatear un number: una es usando los metodos
 * existentes en la clase Number y sus derivados (por ejemplo, al objeto
 * BigDecimal le puedes decir el number de decimales que tendra).  Otra es
 * usando la clase NumberFormat y sus hijas.  Cada una tiene puntos a favor y
 * puntos en contra:
 * - BigDecimal es mas rapida y necesita crear menos objetos, pero los
 * resultados los da como BigDecimal, no como String, por lo que aun pueden ser
 * modificados.  Ademas, no realiza ningun formateo Localizado (no usa el Locale
 * seleccionado).
 * - NumberFormat formatea de acuerdo al Locale del sistema, por lo que las
 * salidas son mas correctas.  Sin embargo, es significativamente mas lento que
 * el metodo nombrado anteriormente.
 * En general, esta clase brinda metodos que usan ambos enfoques.  Usaremos
 * Number (o BigDecimal) cuando queramos operar con numbers grandes, como montos
 * en alguna moneda.  Usaremos NumberFormat (y sus derivadas) cuando queramos
 * mostrar un resultado en pantalla.
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */

@Component
public class NumberUtils {

    //    public  final int FORMAT_NUMBER_WITHOUT_DOTS = 0;
    //    public  final int FORMAT_NUMBER_WITH_DOTS = 1;

    //    public  final int SCALE_BIG_DECIMAL = 4;
    public static final int EMPTY_VALUE = -1; // Valor "vacio" para numbers positivos
    //    public  final BigDecimal ZERO = (new BigDecimal(0.0)).setScale(SCALE_BIG_DECIMAL);
    //    public  final BigDecimal ONE = (new BigDecimal(1)).setScale(SCALE_BIG_DECIMAL);
    public static final int FRACTION_DIGITS = 2; // Decimales por defecto
    public static final int ROUND = BigDecimal.ROUND_HALF_UP;
    private static final String DEFAULT_DECIMAL_SEPARATOR = ",";
    private static final String DEFAULT_ORIGINAL_DECIMAL_SEPARATOR = ".";
    private static final String DEFAULT_SEPARATOR = StringUtils.EMPTY;
    //    public  final String ZERO_STRING = "0";
    //    public  final String ONE_STRING = "1";
    // En lugar de definir nuestros metodos, usaremos las constantes ROUND
    // de la clase BigDecimal.  Si acaso, las renombraremos aqui, por comodidad.
    public final int TRUNCATE = BigDecimal.ROUND_DOWN;
    private final String EMPTY_NUMBER = ""; // TODO: Que sea configurable si devuelve "" o null
    private final Logger log = LoggerFactory.getLogger(NumberUtils.class);
    public BigDecimal BIG_ONE = new BigDecimal(1); // Util para algunas comparaciones
    public BigDecimal BIG_ZERO = new BigDecimal(0); // Util para algunas comparaciones
    private NumberFormat decimalFormatter;
    private long serie = 0;
    private StringUtils stringUtils;

    {
        // TODO: Decidir que Locale y que Precision se usaran.
        // decimalFormatter = NumberFormat.getNumberInstance();
        // decimalFormatter.setMaximumFractionDigits(SystemInfo.getPrecision());
        decimalFormatter = NumberFormat.getNumberInstance(new Locale("es", "VE"));
        // TODO: Manejar el locale de VE, y el default, en la clase SystemInfo
        decimalFormatter.setMinimumFractionDigits(FRACTION_DIGITS);
        decimalFormatter.setMaximumFractionDigits(FRACTION_DIGITS);
    }

    @Autowired
    public NumberUtils(StringUtils stringUtils) {
        this.stringUtils = stringUtils;
    }

    /**
     * Calcula el resultado de sumar (+) dos BigDecimal aa y bb
     *
     * @param aa BigDecimal a sumar
     * @param bb BigDecimal a sumar
     * @return BigDecimal resultado de sumar aa y bb
     */
    public BigDecimal add(BigDecimal aa, BigDecimal bb) {
        return (null == aa && null == bb) ? null : ((null == aa) ? bb : ((null == bb) ? aa : aa.add(bb)));
    }

    /**
     * Calcula el resultado de sumar (+) dos Double aa y bb
     * TODO: Este metodo igual pasa el objeto a primitivo.  Solo existe para facilitar el pase a BigDecimal.  Debe
     * ser eliminado luego.
     *
     * @param aa Double a sumar
     * @param bb Double a sumar
     * @return Double resultado de sumar aa y bb
     * @deprecated Usar add(BigDecimal aa, BigDecimal bb)
     */
    public Double add(Double aa, Double bb) {
        return (null == aa && null == bb) ? null : ((null == aa) ? bb : ((null == bb) ? aa : new Double(aa + bb)));
    }

    /**
     * Convierte de BigDecimal a Double
     * TODO: Validar los usos de Double en la aplicacion.  Solo existe para facilitar el pase a BigDecimal.  Debe ser
     * eliminado luego.
     *
     * @param number BigDecimal to convert
     * @return Double with the value of number
     * @deprecated Mirar el ToDo
     */
    public Double bigDecimalToDouble(BigDecimal number) {
        return (null == number) ? null : number.doubleValue();
    }

    public int compare(int n1, int n2) {
        return n1 - n2;
    }

    /**
     * Formatea un Double number
     *
     * @param number Double con el number a formatear
     * @return String con bigValue formateado
     */
    public String decimalFormat(double number) {
        return formatNumber(number, "", ",", ".");
    }

    /**
     * Formatea un Double number para ser mostrado por pantalla
     *
     * @param number                   Double con el number a formatear
     * @param separator                String con el separador de miles a usar en el resultado
     * @param decimalSeparator         String con el separador de decimales a usar en el resultado
     * @param originalDecimalSeparator String con el separador de decimales usado en bigValue
     * @return String con bigValue formateado
     */
    public String formatNumber(Double number,
                               String separator,
                               String decimalSeparator,
                               String originalDecimalSeparator) {
        /*
          - Si el number tiene entre 7-8 digitos o menos (ej, 9999999) ambos
            parser funcionan perfectamente.
          - Si el number tiene entre 7-8 digitos enteros y 4 o mas decimales
            (ej, 9999999.9999), usar DecimalFormat da error de redondeo
            (devuelve 10.000.000).
          - Si el number tiene entre 8-9 digitos o mas (ej, 99999999) el
            formateo basado en String da error (devuelve 9,9999999E7)
         */
        if (number == null) {
            return EMPTY_NUMBER;
        }
        if (separator == null) {
            separator = DEFAULT_SEPARATOR;
        }
        if (decimalSeparator == null) {
            decimalSeparator = DEFAULT_DECIMAL_SEPARATOR;
        }
        if (originalDecimalSeparator == null) {
            originalDecimalSeparator = DEFAULT_ORIGINAL_DECIMAL_SEPARATOR;
        }
        double dValue = number;
        if (dValue > 999999999999999d) {
            // TODO: Refinar el valor de control.
            log.error("El number recibido es muy grande para una variable tipo Double.");
            log.error("Se debe usar BigDecimal para manejar este monto (" + number + ").");
            throw new TechnicalException(Exceptions.INVALID_PARAMETERS, Severity.ERROR, new Exception());
        } else {
            String value = number.toString();
            if (isThereAnyE(value)) {
                // DecimalFormat nf = (DecimalFormat) DecimalFormat.getInstance();
                return decimalFormatter.format(number);
            } else {
                // Validar si NumberFormat soporta el Locale usado
                // Locale[] locales = NumberFormat.getAvailableLocales();
                return formatNumber(number.toString(), separator, decimalSeparator, originalDecimalSeparator);
            }
            // TODO: Hay casos no contemplados aqui, segun el tamanio y los decimales del number.
            // Ir modificando el metodo a medida que se vean dichos casos.
        }
    }

    /**
     * Revisa si un String representa un number en notacion cientifica
     *
     * @param string String connteniendo el number a validar
     * @return True if the incoming string represents a number in scientific notation
     */
    public boolean isThereAnyE(String string) {
        return string.contains("E");
    }

    /**
     * Formatea un BigDecimal bigValue para ser mostrado por pantalla
     *
     * @param number                   String sin separadores, con el number a formatear
     * @param separator                String con el separador de miles a usar en el resultado
     * @param decimalSeparator         String con el separador de decimales a usar en el resultado
     * @param originalDecimalSeparator String con el separador de decimales usado en bigValue
     * @return String con bigValue formateado
     */
    public String formatNumber(String number,
                               String separator,
                               String decimalSeparator,
                               String originalDecimalSeparator) {
        return formatNumber(number, separator, decimalSeparator, originalDecimalSeparator, FRACTION_DIGITS);
    }

    /**
     * Formatea un BigDecimal bigValue para ser mostrado por pantalla
     * TODO: Agregar el metodo de redondeo.
     *
     * @param number                   String sin separadores, con el number a formatear
     * @param separator                String con el separador de miles a usar en el resultado
     * @param decimalSeparator         String con el separador de decimales a usar en el resultado
     * @param originalDecimalSeparator String con el separador de decimales usado en bigValue
     * @param decimalDigits            int con la cantidad de decimales a mostrar
     * @return String con bigValue formateado
     */
    public String formatNumber(String number,
                               String separator,
                               String decimalSeparator,
                               String originalDecimalSeparator,
                               int decimalDigits) {
        if (StringUtils.isEmpty(number) || StringUtils.isEmpty(decimalSeparator) || StringUtils.isEmpty(
                originalDecimalSeparator)) {
            return (null == number) ? EMPTY_NUMBER : number;
        }

        int indexOfDot = number.indexOf(originalDecimalSeparator);
        boolean haveDecimal = indexOfDot != -1;
        boolean isNegativo;

        String parteEntera;
        String parteDecimal;

        if (haveDecimal) {
            parteEntera = number.substring(0, indexOfDot);
            //            log.debug("parteEntera = " + parteEntera);
            parteDecimal = number.substring(indexOfDot + 1);
            //            log.debug("haveDecimal = " + haveDecimal);
        } else {
            parteEntera = number;
            parteDecimal = "";
            // parteDecimal = StringUtils.leftPadString("", decimalDigits, '0');
        }

        isNegativo = ((new BigDecimal(parteEntera)).compareTo(new BigDecimal("0")) < 0);
        StringBuilder result = new StringBuilder();
        //        log.debug("number = " + number);
        if (isNegativo) {
            parteEntera = parteEntera.substring(1);
        }

        int length = parteEntera.length();

        for (int i = 0; i < length; i++) {
            int count = i % 3;
            //           log.debug("count = " + i + "/" + count);
            if (count == 0 && i != 0) {
                result.insert(0, separator);
            }
            //            log.debug("parteEntera.charAt(i - 1) = " + parteEntera.charAt(length - i - 1));
            result.insert(0, parteEntera.charAt(length - i - 1));
        }

        //        log.debug("result = " + result);
        int numberDecimales = Math.min(decimalDigits, parteDecimal.length());
        //        log.debug("numberDecimales = " + numberDecimales);
        //        log.debug("haveDecimal = " + haveDecimal);
        // TODO: Completar con ceros si number.lenght() > decimalDigits
        //        return result + (haveDecimal ? decimalSeparator + parteDecimal.substring(0, numberDecimales) : "");
        result.append(haveDecimal ? decimalSeparator + parteDecimal.substring(0,
                numberDecimales) : decimalSeparator + stringUtils.leftPadString("", decimalDigits, '0'));
        if (isNegativo) {
            result.insert(0, "-");
        }
        return result.toString();
    }

    /**
     * Calcula el resultado de dividir (/) un BigDecimal entre un Double
     * TODO: El Double puede traer problemas debido a sus limitaciones de tamanio y exactitud.  Solo existe para
     * facilitar el pase a BigDecimal.  Debe ser eliminado luego.
     *
     * @param dividendo BigDecimal a usar como dividendo
     * @param divisor   Double a usar como divisor
     * @return Double resultado de dividir dividendo entre divisor
     * @deprecated Usar divide(BigDecimal dividendo, BigDecimal divisor)
     */
    public BigDecimal divide(BigDecimal dividendo, Double divisor) {
        //        return (null == dividendo && null == divisor) ? null :
        //                ((null == dividendo) ? new BigDecimal(1 / divisor.doubleValue()) : ((null == divisor) ?
        // dividendo :
        //                        new BigDecimal(dividendo.doubleValue() / divisor.doubleValue())));
        return divide(dividendo, new BigDecimal(divisor));
    }

    /**
     * Calcula el resultado de dividir (/) dos BigDecimal, dividendo entre divisor
     * NOTA: El resultado se devuelve con la misma scale que los datos originales, lo que puede dar resultados no
     * deseados.
     *
     * @param dividendo BigDecimal a usar como minuendo
     * @param divisor   BigDecimal a usar como substraendo
     * @return BigDecimal resultado de dividir dividendo entre divisor
     */
    public BigDecimal divide(BigDecimal dividendo, BigDecimal divisor) {
        return (null == dividendo && null == divisor) ? null : ((null == dividendo) ? new BigDecimal(1).divide(divisor,
                BigDecimal.ROUND_DOWN) : ((null == divisor) ? dividendo : dividendo.divide(divisor,
                BigDecimal.ROUND_DOWN)));
    }

    // TODO: Analizar si sobrecargamos este metodo para los demas primitivos

    /**
     * Calcula el resultado de dividir (/) dos Double, dividendo entre divisor
     * TODO: Este metodo igual pasa el objeto a primitivo.  Solo existe para facilitar el pase a BigDecimal.  Debe
     * ser eliminado luego.
     *
     * @param dividendo Double a usar como dividendo
     * @param divisor   Double a usar como divisor
     * @return Double resultado de dividir dividendo entre divisor
     * @deprecated Usar divide(BigDecimal dividendo, BigDecimal divisor)
     */
    public Double divide(Double dividendo, Double divisor) {
        return (null == dividendo && null == divisor) ? null : ((null == dividendo) ? new Double(1 / divisor) : (
                (null == divisor) ? dividendo : new Double(
                        dividendo / divisor)));
    }

    /**
     * Convierte de Double a BigDecimal
     *
     * @param number Double to convert
     * @return BigDecimal with the value of number
     */
    public BigDecimal doubleToBigDecimal(Double number) {
        return (null == number) ? null : new BigDecimal(number);
    }

    public BigDecimal fix(BigDecimal number, int precision) {
        return number.setScale(precision, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Formatea un Double number para ser mostrado por pantalla
     *
     * @param number Double con el number a formatear
     * @return String con bigValue formateado
     */
    public String formatNumber(Double number) {
        // Validar si NumberFormat soporta el Locale usado
        // Locale[] locales = NumberFormat.getAvailableLocales();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(SystemInfo.getLocale());
        return formatNumber(number,
                symbols.getGroupingSeparator() + "",
                symbols.getDecimalSeparator() + "",
                StringUtils.DOT);
    }

    /**
     * Formatea un Double number para ser mostrado por pantalla
     *
     * @param number Double con el number a formatear
     * @param locale Locale a usar
     * @return String con bigValue formateado
     */
    public String formatNumber(Double number, Locale locale) {
        // Validar si NumberFormat soporta el Locale usado
        // Locale[] locales = NumberFormat.getAvailableLocales();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        return formatNumber(number,
                symbols.getGroupingSeparator() + "",
                symbols.getDecimalSeparator() + "",
                StringUtils.DOT);
    }

    /**
     * Formatea un BigDecimal number para ser mostrado por pantalla con precision por defecto de 2.
     *
     * @param number BigDecimal con el number a formatear
     * @return String con bigValue formateado
     */
    public String formatNumber(BigDecimal number) {
        return formatNumber(number, FRACTION_DIGITS);
    }

    /**
     * Formatea un BigDecimal number para ser mostrado por pantalla indicandole la cantidad de decimales a mostrar
     *
     * @param number    BigDecimal con el number a formatear
     * @param precision Precision to format the incoming number
     * @return String con bigValue formateado
     */
    public String formatNumber(BigDecimal number, int precision) {
        // Validar si NumberFormat soporta el Locale usado
        // Locale[] locales = NumberFormat.getAvailableLocales();
        BigDecimal number_ = formatBigDecimal(number, precision);
        Locale lDefault = SystemInfo.getLocale();
        log.debug("lDefault = " + lDefault);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(lDefault);
        return formatNumber(number_,
                symbols.getGroupingSeparator() + "",
                symbols.getDecimalSeparator() + "",
                StringUtils.DOT,
                number_.scale());
    }

    /**
     * Formatea un <code>BigDecimal</code> a la precision decimal deseada
     *
     * @param number    BigDecimal a formatear
     * @param precision int con el number de decimales a mostrar
     * @return <code>BigDecimal</code> con el number de decimales indicado
     */
    public BigDecimal formatBigDecimal(BigDecimal number, int precision) {
        if (number == null) {
            return null;
        }
        return number.setScale(precision, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Formatea un BigDecimal bigValue para ser mostrado por pantalla
     * TODO: Agregar el metodo de redondeo.
     *
     * @param number                   BigDecimal con el number a formatear
     * @param separator                String con el separador de miles a usar en el resultado
     * @param decimalSeparator         String con el separador de decimales a usar en el resultado
     * @param originalDecimalSeparator String con el separador de decimales usado en bigValue
     * @param decimalDigits            int con la cantidad de decimales a mostrar
     * @return String con bigValue formateado
     */
    public String formatNumber(BigDecimal number,
                               String separator,
                               String decimalSeparator,
                               String originalDecimalSeparator,
                               int decimalDigits) {
        if (null == number) {
            return EMPTY_NUMBER;
        } else {
            return formatNumber(number.toString(),
                    separator,
                    decimalSeparator,
                    originalDecimalSeparator,
                    decimalDigits);
        }
    }

    // Inicio de Metodos de NumberUtil de SICAM ----------------- ELIMINAR
    // TODO: Sincerar los metodos y usar el mismo formateador y formato

    /**
     * Formatea un BigDecimal number para ser mostrado por pantalla
     *
     * @param number                   BigDecimal con el number a formatear
     * @param separator                String con el separador de miles a usar en el resultado
     * @param decimalSeparator         String con el separador de decimales a usar en el resultado
     * @param originalDecimalSeparator String con el separador de decimales usado en bigValue
     * @return String con bigValue formateado
     * @deprecated No hay garantia del number de decimales.  Usar en lugar de este metodo el metodo
     * formatNumber(String, String, String, String, int)
     */
    public String formatNumber(BigDecimal number,
                               String separator,
                               String decimalSeparator,
                               String originalDecimalSeparator) {
        if (number == null) {
            return EMPTY_NUMBER;
        }
        // TODO: Si el number tiene scale 0, no pintara los decimales, pero si el separador.
        // Si forzamos a usar FRACTION_DIGITS servira siempre, pero siempre usara esos decimales a juro.
        // Validar si lo correcto es leer FRACTION_DIGITS de un archivo de propiedades, o deprecar este metodo.
        return formatNumber(number.toString(), separator, decimalSeparator, originalDecimalSeparator, number.scale());
    }

    /**
     * Formatea un BigDecimal number para ser mostrado por pantalla
     *
     * @param number BigDecimal con el number a formatear
     * @param locale Locale a usar
     * @return String con bigValue formateado
     */
    public String formatNumber(BigDecimal number, Locale locale) {
        // Validar si NumberFormat soporta el Locale usado
        // Locale[] locales = NumberFormat.getAvailableLocales();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        return formatNumber(number,
                symbols.getGroupingSeparator() + "",
                symbols.getDecimalSeparator() + "",
                StringUtils.DOT,
                number.scale());
    }

    /**
     * Formatea un BigDecimal bigValue para ser mostrado por pantalla
     *
     * @param number String sin separadores, con el number a formatear
     * @return String con bigValue formateado
     */
    public String formatNumber(String number) {
        // Validar si NumberFormat soporta el Locale usado
        // Locale[] locales = NumberFormat.getAvailableLocales();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(SystemInfo.getLocale());
        return formatNumber(number,
                symbols.getGroupingSeparator() + "",
                symbols.getDecimalSeparator() + "",
                StringUtils.DOT);
    }

    /**
     * Formatea un BigDecimal bigValue para ser mostrado por pantalla
     *
     * @param number String sin separadores, con el number a formatear
     * @param locale Locale a usar
     * @return String con bigValue formateado
     */
    public String formatNumber(String number, Locale locale) {
        // Validar si NumberFormat soporta el Locale usado
        // Locale[] locales = NumberFormat.getAvailableLocales();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        return formatNumber(number,
                symbols.getGroupingSeparator() + "",
                symbols.getDecimalSeparator() + "",
                StringUtils.DOT);
    }

    /**
     * Returns an unique number.
     * To generate an unique number, we add:
     * - A Random number.
     * - An Unique number for this JVM instance.
     * - A Date-related number.
     *
     * @return long with an unique number
     */
    public long getUniqueNumber() {
        return Long.parseLong(new Date().getTime() + new Random().nextLong() + serie++ + "");
    }

    /**
     * Compares two <code>BigDecimal</code> objects to determinate if one is greater or equal than (&gt;=) the other
     *
     * @param value1 left value
     * @param value2 right value
     * @return true when value1 is numerically greater or equal than value2
     */
    public boolean greaterEqualThan(BigDecimal value1, BigDecimal value2) {
        int compare = value1.compareTo(value2);
        return compare >= 0;
    }

    /**
     * Compares two <code>BigDecimal</code> objects to determinate if one is greater than (&gt;) the other
     *
     * @param value1 left value
     * @param value2 right value
     * @return true when value1 is numerically greater than value2
     */
    public boolean greaterThan(BigDecimal value1, BigDecimal value2) {
        return value1.compareTo(value2) > 0;
    }

    // TODO: Definir una RegExp para manejarlo igual que el metodo isAnInteger(...)
    public boolean isANumber(String numberWannabe) {
        try {
            Double.parseDouble(numberWannabe);
            //            new BigDecimal(numberWannabe);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAnInteger(String numberWannabe) {
        return (numberWannabe != null) && Pattern.compile("^[0-9]+$").matcher(numberWannabe).matches();
    }

    /**
     * Compares two <code>BigDecimal</code> objects to determinate if one is lesser or equal than (&lt;=) the other
     *
     * @param value1 left value
     * @param value2 right value
     * @return true when value1 is numerically lesser or equal than value2
     */
    public boolean lesserEqualThan(BigDecimal value1, BigDecimal value2) {
        int compare = value1.compareTo(value2);
        return compare <= 0;
    }

    /**
     * Compares two <code>BigDecimal</code> objects to determinate if one is lesser than (&lt;) the other
     *
     * @param value1 left value
     * @param value2 right value
     * @return true when value1 is numerically less than value2
     */
    public boolean lesserThan(BigDecimal value1, BigDecimal value2) {
        return value1.compareTo(value2) < 0;
    }

    /**
     * Calcula el resultado de multiplicar (x, .) dos BigDecimal aa y bb
     *
     * @param aa BigDecimal a multiplicar
     * @param bb BigDecimal a multiplicar
     * @return BigDecimal resultado de multiplicar aa y bb
     */
    public BigDecimal multiply(BigDecimal aa, BigDecimal bb) {
        return (null == aa && null == bb) ? null : ((null == aa) ? bb : ((null == bb) ? aa : aa.multiply(bb)));
    }

    /**
     * Calcula el resultado de multiplicar (x, .) dos Double aa y bb
     * TODO: Este metodo igual pasa el objeto a primitivo.  Solo existe para facilitar el pase a BigDecimal.  Debe
     * ser eliminado luego.
     *
     * @param aa Double a multiplicar
     * @param bb Double a multiplicar
     * @return Double resultado de multiplicar aa y bb
     * @deprecated Usar multiply(BigDecimal aa, BigDecimal bb)
     */
    public Double multiply(Double aa, Double bb) {
        return (null == aa && null == bb) ? null : ((null == aa) ? bb : ((null == bb) ? aa : new Double(aa * bb)));
    }

    /**
     * Calcula el resultado de multiplicar (x, .) el BigDecimal aa y el double bb
     * TODO: El double puede traer problemas debido a sus limitaciones de tamanio y exactitud.  Solo existe para
     * facilitar el pase a BigDecimal.  Debe ser eliminado luego.
     *
     * @param aa BigDecimal a multiplicar
     * @param bb double a multiplicar
     * @return BigDecimal resultado de multiplicar aa y bb
     * @deprecated Usar multiply(BigDecimal aa, BigDecimal bb)
     */
    public BigDecimal multiply(BigDecimal aa, double bb) {
        return ((null == aa) ? new BigDecimal(bb) : aa.multiply(new BigDecimal(bb)));
    }

    /**
     * Calcula el resultado de multiplicar (x, .) el Double aa y el double bb
     *
     * @param aa Double a multiplicar
     * @param bb double a multiplicar
     * @return Double resultado de multiplicar aa y bb
     * @deprecated Usar multiply(BigDecimal aa, BigDecimal bb)
     */
    public Double multiply(Double aa, double bb) {
        return ((null == aa) ? new Double(bb) : (new Double(aa * bb)));
    }

    /**
     * Crea un BigDecimal a partir de un String
     *
     * @param seed String con la semilla del BigDecimal a mostrar
     * @return BigDecimal creado a partir de la semilla seed
     */
    public BigDecimal parseBigDecimal(String seed) {
        return parseBigDecimal(seed, FRACTION_DIGITS);
    }

    /**
     * Crea un BigDecimal a partir de un String
     *
     * @param seed           String con la semilla del BigDecimal a mostrar
     * @param fractionDigits Amount of digit of the fractional part of the outgoing number
     * @return BigDecimal creado a partir de la semilla seed
     */
    public BigDecimal parseBigDecimal(String seed, int fractionDigits) {
        BigDecimal bigDecimal;
        if (isThereAnyDot(seed)) {
            if (isThereAnyComma(seed)) {
                String formateado = seed.replaceAll("\\.", "");
                formateado = formatNumber(formateado, "", ".", ",");
                bigDecimal = new BigDecimal(formateado);
            } else {
                // log.error("El number que se obtiene no deberia tener puntos ya que por pantalla no se permite
                // ingresarlo");
                // throw new TechnicalException(Exceptions.INVALID_PARAMETERS, Severity.ERROR, new Exception());
                bigDecimal = new BigDecimal(seed);
            }
        } else {
            String formateado = formatNumber(seed, "", ".", ",");
            bigDecimal = new BigDecimal(formateado);
        }
        if (fractionDigits > -1) {
            bigDecimal = bigDecimal.setScale(fractionDigits, BigDecimal.ROUND_UNNECESSARY);
        }
        return bigDecimal;
    }

    /**
     * Revisa si un String tiene contenido un punto
     *
     * @param string String conteniendo el number a validar
     * @return boolean true si string contiene un punto, false si no
     */
    public boolean isThereAnyDot(String string) {
        return string.contains("."); // 1.4
        // return string.contains("."); // 1.6
    }

    /**
     * Revisa si un String tiene contenido una coma
     *
     * @param string String conteniendo el number a validar
     * @return boolean true si string contiene una coma, false si no
     */
    public boolean isThereAnyComma(String string) {
        return string.contains(","); // 1.4
        // return string.contains(","); // 1.6
    }

    /**
     * It transforms the given BigDecimal number into a rounded BigDecimal
     * value according to the given precision type (truncation or roundig)
     *
     * @param number    the value that will be rounded
     * @param precision the decimal positions to use
     * @param roundType the kind of precision (rounding or truncation)
     * @return The modified value of the given number
     */
    public BigDecimal round(BigDecimal number, int precision, int roundType) {
        return number.setScale(precision, roundType);
    }

    /**
     * Indica un nuevo formateador para ser usado en el sistema.  Esto es en el
     * caso, por ejemplo, que queramos que nuestros numbers no usen 2 decimales.
     * NOTA: Para cambiar los decimales de todo el sistema, usar el metodo
     * SystemInfo.setPrecision(...)
     *
     * @param _decimalFormatter NumberFormat con el formateador a usar.
     */
    public void setDecimalFormatter(NumberFormat _decimalFormatter) {
        decimalFormatter = _decimalFormatter;
    }

    /**
     * Calcula el resultado de restar (-) dos BigDecimal, mm menos ss
     *
     * @param mm BigDecimal a usar como minuendo
     * @param ss BigDecimal a usar como substraendo
     * @return BigDecimal resultado de restar mm menos s
     */
    public BigDecimal subtract(BigDecimal mm, BigDecimal ss) {
        return (null == mm && null == ss) ? null : ((null == mm) ? new BigDecimal(0).subtract(ss) : ((null == ss) ?
                mm : mm.subtract(
                ss)));
    }

    /**
     * Calcula el resultado de restar (-) dos Double, mm menos ss
     * TODO: Este metodo igual pasa el objeto a primitivo.  Solo existe para facilitar el pase a BigDecimal.  Debe
     * ser eliminado luego.
     *
     * @param mm Double a usar como minuendo
     * @param ss Double a usar como substraendo
     * @return Double resultado de restar mm menos ss
     * @deprecated Usar subtract(BigDecimal mm, BigDecimal ss)
     */
    public Double subtract(Double mm, Double ss) {
        return (null == mm && null == ss) ? null : ((null == mm) ? new Double(-ss) : ((null == ss) ? mm : new Double
                (mm - ss)));
    }

    public BigDecimal truncate(BigDecimal number) {
        return truncate(number, FRACTION_DIGITS);
    }

    public BigDecimal truncate(BigDecimal number, int precision) {
        return number.setScale(precision, BigDecimal.ROUND_DOWN);
    }
}