package com.araguacaima.commons.utils;

/**
 * Clase Utilitaria que efectúa conversiones entre números de base 10 a otras bases. Los numeros decimales de entrada
 * y salida de los métodos estaticos de esta utilería estan enmarcados dentro del rango del dominio double es decir:
 * (4.94065645841246544e-324, 1.79769313486231570e+308], por lo tanto podrá operar con números de hasta 307 dígitos
 * <br>
 * Para ello se tiene un máximo de 62 caracteres para representar tales numeros en base 10 (double), por lo que la
 * máxima
 * conversión posible será en base 62. Esto restringe el conjunto posible de valores únicamente a los caracateres
 * alfanuméricos. Esta decisión se hace por simplicidad y conveniencia para evitar eventuales problemas de codificación
 * de caracteres especiales o de diferentes charsets. Al final serán sólo caracteres ASCII los que sean utilizados como
 * entrada y salida en los procesos de conversión de bases provistos. La mínima base aceptable será 2, por no estar
 * definida la representación de un número en un radio menor a dos (es imposible representar un número cualquiera, de
 * cualquier base, con menos de dos caracteres)
 * <br>
 * Created by IntelliJ IDEA 10.5.1.
 * User: Alejandro Manuel Méndez Araguacaima (AMMA)
 * Date: 19/08/11 12:08 PM
 */

public class BaseConverterUtils {

    /**
     * Conjunto de caracteres que se utilizan para hacer la conversión, transformación y asociación producto del cambio
     * de base entre el numero decimal y el represnetado an la base X (que deberá ser menor o igual a 62)
     */
    final static char[] digits = {

            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F',
            'G',
            'H',
            'I',
            'J',
            'K',
            'L',
            'M',
            'N',
            'O',
            'P',
            'Q',
            'R',
            'S',
            'T',
            'U',
            'V',
            'W',
            'X',
            'Y',
            'Z',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'i',
            'j',
            'k',
            'l',
            'm',
            'n',
            'o',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z'};

    private static final BaseConverterUtils INSTANCE = new BaseConverterUtils();

    private BaseConverterUtils() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static BaseConverterUtils getInstance() {
        return INSTANCE;
    }

    public static long fromBase16ToDecimal(String base16Number)
            throws NumberFormatException {
        return fromBaseXToDecimal(base16Number, 16);
    }

    /**
     * Obtiene la representación en base decimal del numero en base X, representado por el parámetro de entrada 'radix'
     *
     * @param stringRepresentationOfNumberInBaseX El número en base X, representado por el parámetro de entrada 'radix',
     *                                            al cual se le desea obtener su representación en base decimal
     * @param radix                               La base en la cual está el parámetro de entrada
     *                                            'stringRepresentationOfNumberInBaseX'
     * @return Un double con la representación del número de entrada en base X, representado por el parámetro de entrada
     * 'radix', convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base X, o si la representación en base X del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBaseXToDecimal(String stringRepresentationOfNumberInBaseX, int radix)
            throws NumberFormatException {
        if (stringRepresentationOfNumberInBaseX == null) {
            throw new NumberFormatException("null");
        }

        if (radix > digits.length) {
            throw new NumberFormatException(
                    "Is not possible to convert from decimal base to any other base greather than " + digits.length +
                            ", because of the defined set of characters to represent the returning value has fixed " +
                            "to" + " the following " + digits.length + " characteres: " + new String(
                            digits));
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException(
                    "Is not possible to convert from decimal base to any other base lesser than " + Character
                            .MIN_RADIX);
        }

        long result = 0;
        boolean negative = false;
        int i = 0, max = stringRepresentationOfNumberInBaseX.length();
        long limit;
        long multmin;
        int digit;
        String digitsString = new String(digits);

        if (max > 0) {
            if (stringRepresentationOfNumberInBaseX.charAt(0) == '-') {
                negative = true;
                limit = Long.MIN_VALUE;
                i++;
            } else {
                limit = -Long.MAX_VALUE;
            }
            multmin = limit / radix;
            if (i < max) {
                digit = digitsString.indexOf(stringRepresentationOfNumberInBaseX.charAt(i++));
                if (digit < 0) {
                    throw new NumberFormatException("For input string: \"" + stringRepresentationOfNumberInBaseX +
                            "\"");
                } else {
                    result = -digit;
                }
            }
            while (i < max) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = digitsString.indexOf(stringRepresentationOfNumberInBaseX.charAt(i++));
                if (digit < 0) {
                    throw new NumberFormatException("For input string: \"" + stringRepresentationOfNumberInBaseX +
                            "\"");
                }
                if (result < multmin) {
                    throw new NumberFormatException("For input string: \"" + stringRepresentationOfNumberInBaseX +
                            "\"");
                }
                result *= radix;
                if (result < limit + digit) {
                    throw new NumberFormatException("For input string: \"" + stringRepresentationOfNumberInBaseX +
                            "\"");
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException("For input string: \"" + stringRepresentationOfNumberInBaseX + "\"");
        }
        if (negative) {
            if (i > 1) {
                return result;
            } else {    /* Only got "-" */
                throw new NumberFormatException("For input string: \"" + stringRepresentationOfNumberInBaseX + "\"");
            }
        } else {
            return -result;
        }
    }

    /**
     * Obtiene la representación en base decimal del numero en base 16 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los caracteres identificados por el parametro de entrada 'characterToLeftTriming'
     * que existan del lado izquierdo de parametro de entrada, antes de efectuar la conversión
     *
     * @param base16Number           El número en base decimal al cual se le desea obtener su representación en base 16
     * @param characterToLeftTriming El caracter que se quiere eliminar a la izquierda del numero en base 16
     * @return Un double con la representación del número de entrada en base 16 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 16, o si la representación en base 16 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase16ToDecimalLeftTrimmed(String base16Number, char characterToLeftTriming)
            throws NumberFormatException {
        return fromBaseXToDecimalLeftTrimmed(base16Number, 16, characterToLeftTriming);
    }

    /**
     * Obtiene la representación en base decimal del numero en base X, representado por el parámetro de entrada 'radix'
     * al cual se le suprimen todas las apariciones del caracter descrito por el parametro 'paddingCharacter' antes de
     * realizar la conversión
     *
     * @param stringRepresentationOfNumberInBaseX El número en base X, representado por el parámetro de entrada 'radix',
     *                                            al cual se le desea obtener su representación en base decimal
     * @param radix                               La base en la cual está el parámetro de entrada
     *                                            'stringRepresentationOfNumberInBaseX'
     * @param paddingCharacter                    El caracter que se queire sufrimir de la izquierda del paramétro de
     *                                            entrada
     *                                            'stringRepresentationOfNumberInBaseX'
     * @return Un double con la representación del número de entrada en base X, representado por el parámetro de entrada
     * 'radix', convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base X, o si la representación en base X del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBaseXToDecimalLeftTrimmed(String stringRepresentationOfNumberInBaseX,
                                                     int radix,
                                                     char paddingCharacter)
            throws NumberFormatException {
        String trimmedNumberInBaseX = StringUtils.leftTrim(stringRepresentationOfNumberInBaseX, paddingCharacter);
        return fromBaseXToDecimal(trimmedNumberInBaseX, radix);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 16 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los ceros que existan del lado izquierdo de parametro de entrada, antes de
     * efectuar la conversión
     *
     * @param base16Number El número en base decimal al cual se le desea obtener su representación en base 16
     * @return Un double con la representación del número de entrada en base 16 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 16, o si la representación en base 16 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase16ToDecimalZeroLeftTrimmed(String base16Number)
            throws NumberFormatException {
        return fromBaseXToDecimalZeroLeftTrimmed(base16Number, 16);
    }

    /**
     * Obtiene la representación en base decimal del numero en base X, representado por el parámetro de entrada 'radix'
     * al cual se le suprimen todos los 0's antes de realizar la conversión
     *
     * @param stringRepresentationOfNumberInBaseX El número en base X, representado por el parámetro de entrada 'radix',
     *                                            al cual se le desea obtener su representación en base decimal
     * @param radix                               La base en la cual está el parámetro de entrada
     *                                            'stringRepresentationOfNumberInBaseX'
     * @return Un double con la representación del número de entrada en base X, representado por el parámetro de entrada
     * 'radix', convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base X, o si la representación en base X del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBaseXToDecimalZeroLeftTrimmed(String stringRepresentationOfNumberInBaseX, int radix)
            throws NumberFormatException {
        return fromBaseXToDecimalLeftTrimmed(stringRepresentationOfNumberInBaseX, radix, '0');
    }

    /**
     * Obtiene la representación en base decimal del numero en base 2 representado por el parámetro de entrada
     *
     * @param base2Number El número en base decimal al cual se le desea obtener su representación en base 2
     * @return Un double con la representación del número de entrada en base 2 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 2, o si la representación en base 2 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase2ToDecimal(String base2Number)
            throws NumberFormatException {
        return fromBaseXToDecimal(base2Number, 2);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 2 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los caracteres identificados por el parametro de entrada 'characterToLeftTriming'
     * que existan del lado izquierdo de parametro de entrada, antes de efectuar la conversión
     *
     * @param base2Number            El número en base decimal al cual se le desea obtener su representación en base 2
     * @param characterToLeftTriming El caracter que se quiere eliminar a la izquierda del numero en base 2
     * @return Un double con la representación del número de entrada en base 2 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 2, o si la representación en base 2 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase2ToDecimalLeftTrimmed(String base2Number, char characterToLeftTriming)
            throws NumberFormatException {
        return fromBaseXToDecimalLeftTrimmed(base2Number, 2, characterToLeftTriming);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 2 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los ceros que existan del lado izquierdo de parametro de entrada, antes de
     * efectuar la conversión
     *
     * @param base2Number El número en base decimal al cual se le desea obtener su representación en base 2
     * @return Un double con la representación del número de entrada en base 2 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 2, o si la representación en base 2 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase2ToDecimalZeroLeftTrimmed(String base2Number)
            throws NumberFormatException {
        return fromBaseXToDecimalZeroLeftTrimmed(base2Number, 2);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 36 representado por el parámetro de entrada
     *
     * @param base36Number El número en base decimal al cual se le desea obtener su representación en base 36
     * @return Un double con la representación del número de entrada en base 36 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 36, o si la representación en base 36 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase36ToDecimal(String base36Number)
            throws NumberFormatException {
        return fromBaseXToDecimal(base36Number, 36);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 36 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los caracteres identificados por el parametro de entrada 'characterToLeftTriming'
     * que existan del lado izquierdo de parametro de entrada, antes de efectuar la conversión
     *
     * @param base36Number           El número en base decimal al cual se le desea obtener su representación en base 36
     * @param characterToLeftTriming El caracter que se quiere eliminar a la izquierda del numero en base 36
     * @return Un double con la representación del número de entrada en base 36 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 36, o si la representación en base 36 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase36ToDecimalLeftTrimmed(String base36Number, char characterToLeftTriming)
            throws NumberFormatException {
        return fromBaseXToDecimalLeftTrimmed(base36Number, 36, characterToLeftTriming);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 36 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los ceros que existan del lado izquierdo de parametro de entrada, antes de
     * efectuar la conversión
     *
     * @param base36Number El número en base decimal al cual se le desea obtener su representación en base 36
     * @return Un double con la representación del número de entrada en base 36 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 36, o si la representación en base 36 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase36ToDecimalZeroLeftTrimmed(String base36Number)
            throws NumberFormatException {
        return fromBaseXToDecimalZeroLeftTrimmed(base36Number, 36);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 40 representado por el parámetro de entrada
     *
     * @param base40Number El número en base decimal al cual se le desea obtener su representación en base 40
     * @return Un double con la representación del número de entrada en base 40 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 40, o si la representación en base 40 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase40ToDecimal(String base40Number)
            throws NumberFormatException {
        return fromBaseXToDecimal(base40Number, 40);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 40 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los caracteres identificados por el parametro de entrada 'characterToLeftTriming'
     * que existan del lado izquierdo de parametro de entrada, antes de efectuar la conversión
     *
     * @param base40Number           El número en base decimal al cual se le desea obtener su representación en base 40
     * @param characterToLeftTriming El caracter que se quiere eliminar a la izquierda del numero en base 40
     * @return Un double con la representación del número de entrada en base 40 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 40, o si la representación en base 40 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase40ToDecimalLeftTrimmed(String base40Number, char characterToLeftTriming)
            throws NumberFormatException {
        return fromBaseXToDecimalLeftTrimmed(base40Number, 40, characterToLeftTriming);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 40 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los ceros que existan del lado izquierdo de parametro de entrada, antes de
     * efectuar la conversión
     *
     * @param base40Number El número en base decimal al cual se le desea obtener su representación en base 40
     * @return Un double con la representación del número de entrada en base 40 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 40, o si la representación en base 40 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase40ToDecimalZeroLeftTrimmed(String base40Number)
            throws NumberFormatException {
        return fromBaseXToDecimalZeroLeftTrimmed(base40Number, 40);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 62 representado por el parámetro de entrada
     *
     * @param base62Number El número en base decimal al cual se le desea obtener su representación en base 62
     * @return Un double con la representación del número de entrada en base 62 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 62, o si la representación en base 62 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase62ToDecimal(String base62Number)
            throws NumberFormatException {
        return fromBaseXToDecimal(base62Number, 62);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 62 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los caracteres identificados por el parametro de entrada 'characterToLeftTriming'
     * que existan del lado izquierdo de parametro de entrada, antes de efectuar la conversión
     *
     * @param base62Number           El número en base decimal al cual se le desea obtener su representación en base 62
     * @param characterToLeftTriming El caracter que se quiere eliminar a la izquierda del numero en base 62
     * @return Un double con la representación del número de entrada en base 62 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 62, o si la representación en base 62 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase62ToDecimalLeftTrimmed(String base62Number, char characterToLeftTriming)
            throws NumberFormatException {
        return fromBaseXToDecimalLeftTrimmed(base62Number, 62, characterToLeftTriming);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 62 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los ceros que existan del lado izquierdo de parametro de entrada, antes de
     * efectuar la conversión
     *
     * @param base62Number El número en base decimal al cual se le desea obtener su representación en base 62
     * @return Un double con la representación del número de entrada en base 62 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 62, o si la representación en base 62 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase62ToDecimalZeroLeftTrimmed(String base62Number)
            throws NumberFormatException {
        return fromBaseXToDecimalZeroLeftTrimmed(base62Number, 62);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 8 representado por el parámetro de entrada
     *
     * @param base8Number El número en base decimal al cual se le desea obtener su representación en base 8
     * @return Un double con la representación del número de entrada en base 8 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 8, o si la representación en base 8 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase8ToDecimal(String base8Number)
            throws NumberFormatException {
        return fromBaseXToDecimal(base8Number, 8);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 8 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los caracteres identificados por el parametro de entrada 'characterToLeftTriming'
     * que existan del lado izquierdo de parametro de entrada, antes de efectuar la conversión
     *
     * @param base8Number            El número en base decimal al cual se le desea obtener su representación en base 8
     * @param characterToLeftTriming El caracter que se quiere eliminar a la izquierda del numero en base 8
     * @return Un double con la representación del número de entrada en base 8 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 8, o si la representación en base 8 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase8ToDecimalLeftTrimmed(String base8Number, char characterToLeftTriming)
            throws NumberFormatException {
        return fromBaseXToDecimalLeftTrimmed(base8Number, 8, characterToLeftTriming);
    }

    /**
     * Obtiene la representación en base decimal del numero en base 8 representado por el parámetro de entrada pero
     * suprimiendo previamente todos los ceros que existan del lado izquierdo de parametro de entrada, antes de
     * efectuar la conversión
     *
     * @param base8Number El número en base decimal al cual se le desea obtener su representación en base 8
     * @return Un double con la representación del número de entrada en base 8 convertido a base decimal
     * @throws NumberFormatException Si no es posible obtener un número en base decimal a partir del parametro de
     *                               entrada en base 8, o si la representación en base 8 del parametro de entrada
     *                               no es correcta, según la definición del conjunto ordenado de caracteres de
     *                               representación <code>digits</code>
     */

    public static long fromBase8ToDecimalZeroLeftTrimmed(String base8Number)
            throws NumberFormatException {
        return fromBaseXToDecimalZeroLeftTrimmed(base8Number, 8);
    }

    /**
     * Obtiene la representación en base 16 del numero en base decimal representado por el parámetro de entrada
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 16
     * @return Un String con la representación del número de entrada en base decimal convertido a base 16
     * @throws IllegalArgumentException Si no existen al menos 16 caracteres para representar el numero en base decimal
     *                                  de entrada en base 16
     */

    public static String fromDecimalToBase16(long number)
            throws IllegalArgumentException {
        return fromDecimalToBaseX(number, 16);
    }

    /**
     * Obtiene la representación en base X, descrita por el parametro de entrada 'radix', del numero en base decimal
     * representado por el parámetro de entrada 'decimalNumber'
     *
     * @param decimalNumber El número en base decimal al cual se le desea obtener su representación en base X,
     *                      descrita por el parametro de entrada 'radix'
     * @param radix         La base en la cual se quiere hacer la conversión
     * @return Un String con la representación del número de entrada en base decimal convertido a base X
     * @throws IllegalArgumentException Si no existen al menos 'radix' caracteres para representar el numero en base
     *                                  decimal de entrada en base X
     */

    public static String fromDecimalToBaseX(long decimalNumber, int radix)
            throws IllegalArgumentException {
        if (radix > digits.length) {
            throw new IllegalArgumentException(
                    "Is not possible to convert from decimal base to any other base greather than " + digits.length +
                            ", because of the defined set of characters to represent the returning value has fixed " +
                            "to" + " the following " + digits.length + " characteres: " + new String(
                            digits));
        }
        if (radix == 10) {
            return Long.toString(decimalNumber);
        }

        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (decimalNumber < 0);

        if (!negative) {
            decimalNumber = -decimalNumber;
        }

        while (decimalNumber <= -radix) {
            buf[charPos--] = digits[(int) (-(decimalNumber % radix))];
            decimalNumber = decimalNumber / radix;
        }
        buf[charPos] = digits[(int) (-decimalNumber)];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (65 - charPos));
    }

    /**
     * Obtiene la representación en base 16 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con el caracter descrito por el parámetro 'paddingCharacter' a la izquierda para completar un tamaño
     * identificado por el parámetro de entrada 'size'
     *
     * @param number           El número en base decimal al cual se le desea obtener su representación en base 16
     * @param size             El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que
     *                         representa el número convertido a base 16 dicho tamaño se ignora
     * @param paddingCharacter El caracter con el que se quiere rellenar a las izquierda del numero convertido a
     *                         base 16
     * @return Un String con la representación del número de entrada en base decimal convertido a base 16
     * @throws IllegalArgumentException Si no existen al menos 16 caracteres para representar el numero en base decimal
     *                                  de entrada en base 16
     */

    public static String fromDecimalToBase16LeftPaddedToXCharacters(long number, int size, char paddingCharacter)
            throws IllegalArgumentException {
        return fromDecimalToBaseXLeftPaddedToXCharacters(number, size, 16, paddingCharacter);
    }

    /**
     * Obtiene la representación en base X, descrita por el parametro de entrada 'radix', del numero en base decimal
     * representado por el parámetro de entrada 'decimalNumber', con ceros rellenando el String de salida hasta
     * completar el tamaño descrito por el parámetro de entrada 'size'
     *
     * @param decimalNumber    El número en base decimal al cual se le desea obtener su representación en base X,
     *                         descrita por el parametro de entrada 'radix'
     * @param size             El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que
     *                         representa el
     *                         número convertido a base X dicho tamaño se ignora
     * @param radix            La base en la cual se quiere hacer la conversión
     * @param paddingCharacter El caracter con el que se quiere rellenar a las izquierda del numero convertido a
     *                         base X
     * @return Un String con la representación del número de entrada en base decimal convertido a base X
     * @throws IllegalArgumentException Si no existen al menos 'radix' caracteres para representar el numero en base
     *                                  decimal de entrada en base X
     */

    public static String fromDecimalToBaseXLeftPaddedToXCharacters(long decimalNumber,
                                                                   int size,
                                                                   int radix,
                                                                   char paddingCharacter)
            throws IllegalArgumentException {
        String result = fromDecimalToBaseX(decimalNumber, radix);
        if (size > result.length()) {
            return StringUtils.leftPad(result, size, paddingCharacter);
        } else if (size < result.length()) {
            throw new IllegalArgumentException("La conversión en base '" + radix + "' del número en base decimal '" +
                    decimalNumber + "' no debe exceder el tamaño especificado '" + size + "'");
        } else {
            return result;
        }
    }

    /**
     * Obtiene la representación en base 16 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con ceros a la izquierda para completar un tamaño identificado por el parámetro de entrada 'size'
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 16
     * @param size   El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que representa el
     *               número convertido a base 16 dicho tamaño se ignora
     * @return Un String con la representación del número de entrada en base decimal convertido a base 16
     * @throws IllegalArgumentException Si no existen al menos 16 caracteres para representar el numero en base decimal
     *                                  de entrada en base 16
     */

    public static String fromDecimalToBase16ZeroLeftPaddedToXCharacters(long number, int size)
            throws IllegalArgumentException {
        return fromDecimalToBaseXZeroLeftPaddedToXCharacters(number, size, 16);
    }

    /**
     * Obtiene la representación en base X, descrita por el parametro de entrada 'radix', del numero en base decimal
     * representado por el parámetro de entrada 'decimalNumber', con ceros rellenando el String de salida hasta
     * completar el tamaño descrito por el parámetro de entrada 'size'
     *
     * @param decimalNumber El número en base decimal al cual se le desea obtener su representación en base X,
     *                      descrita por el parametro de entrada 'radix'
     * @param size          El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que
     *                      representa el
     *                      número convertido a base X dicho tamaño se ignora
     * @param radix         La base en la cual se quiere hacer la conversión
     * @return Un String con la representación del número de entrada en base decimal convertido a base X
     * @throws IllegalArgumentException Si no existen al menos 'radix' caracteres para representar el numero en base
     *                                  decimal de entrada en base X
     */

    public static String fromDecimalToBaseXZeroLeftPaddedToXCharacters(long decimalNumber, int size, int radix)
            throws IllegalArgumentException {
        return fromDecimalToBaseXLeftPaddedToXCharacters(decimalNumber, size, radix, '0');
    }

    /**
     * Obtiene la representación en base 2 del numero en base decimal representado por el parámetro de entrada
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 2
     * @return Un String con la representación del número de entrada en base decimal convertido a base 2
     * @throws IllegalArgumentException Si no existen al menos 2 caracteres para representar el numero en base decimal
     *                                  de entrada en base 2
     */

    public static String fromDecimalToBase2(long number)
            throws IllegalArgumentException {
        return fromDecimalToBaseX(number, 2);
    }

    /**
     * Obtiene la representación en base 2 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con el caracter descrito por el parámetro 'paddingCharacter' a la izquierda para completar un tamaño
     * identificado por el parámetro de entrada 'size'
     *
     * @param number           El número en base decimal al cual se le desea obtener su representación en base 2
     * @param size             El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que
     *                         representa el número convertido a base 2 dicho tamaño se ignora
     * @param paddingCharacter El caracter con el que se quiere rellenar a las izquierda del numero convertido a
     *                         base 2
     * @return Un String con la representación del número de entrada en base decimal convertido a base 2
     * @throws IllegalArgumentException Si no existen al menos 2 caracteres para representar el numero en base decimal
     *                                  de entrada en base 2
     */

    public static String fromDecimalToBase2LeftPaddedToXCharacters(long number, int size, char paddingCharacter)
            throws IllegalArgumentException {
        return fromDecimalToBaseXLeftPaddedToXCharacters(number, size, 2, paddingCharacter);
    }

    /**
     * Obtiene la representación en base 2 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con ceros a la izquierda para completar un tamaño identificado por el parámetro de entrada 'size'
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 2
     * @param size   El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que representa el
     *               número convertido a base 2 dicho tamaño se ignora
     * @return Un String con la representación del número de entrada en base decimal convertido a base 2
     * @throws IllegalArgumentException Si no existen al menos 2 caracteres para representar el numero en base decimal
     *                                  de entrada en base 2
     */

    public static String fromDecimalToBase2ZeroLeftPaddedToXCharacters(long number, int size)
            throws IllegalArgumentException {
        return fromDecimalToBaseXZeroLeftPaddedToXCharacters(number, size, 2);
    }

    /**
     * Obtiene la representación en base 36 del numero en base decimal representado por el parámetro de entrada
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 36
     * @return Un String con la representación del número de entrada en base decimal convertido a base 36
     * @throws IllegalArgumentException Si no existen al menos 36 caracteres para representar el numero en base decimal
     *                                  de entrada en base 36
     */

    public static String fromDecimalToBase36(long number)
            throws IllegalArgumentException {
        return fromDecimalToBaseX(number, 36);
    }

    /**
     * Obtiene la representación en base 36 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con el caracter descrito por el parámetro 'paddingCharacter' a la izquierda para completar un tamaño
     * identificado por el parámetro de entrada 'size'
     *
     * @param number           El número en base decimal al cual se le desea obtener su representación en base 36
     * @param size             El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que
     *                         representa el número convertido a base 36 dicho tamaño se ignora
     * @param paddingCharacter El caracter con el que se quiere rellenar a las izquierda del numero convertido a
     *                         base 36
     * @return Un String con la representación del número de entrada en base decimal convertido a base 36
     * @throws IllegalArgumentException Si no existen al menos 36 caracteres para representar el numero en base decimal
     *                                  de entrada en base 36
     */

    public static String fromDecimalToBase36LeftPaddedToXCharacters(long number, int size, char paddingCharacter)
            throws IllegalArgumentException {
        return fromDecimalToBaseXLeftPaddedToXCharacters(number, size, 36, paddingCharacter);
    }

    /**
     * Obtiene la representación en base 36 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con ceros a la izquierda para completar un tamaño identificado por el parámetro de entrada 'size'
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 36
     * @param size   El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que representa el
     *               número convertido a base 36 dicho tamaño se ignora
     * @return Un String con la representación del número de entrada en base decimal convertido a base 36
     * @throws IllegalArgumentException Si no existen al menos 36 caracteres para representar el numero en base decimal
     *                                  de entrada en base 36
     */

    public static String fromDecimalToBase36ZeroLeftPaddedToXCharacters(long number, int size)
            throws IllegalArgumentException {
        return fromDecimalToBaseXZeroLeftPaddedToXCharacters(number, size, 36);
    }

    /**
     * Obtiene la representación en base 40 del numero en base decimal representado por el parámetro de entrada
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 40
     * @return Un String con la representación del número de entrada en base decimal convertido a base 40
     * @throws IllegalArgumentException Si no existen al menos 40 caracteres para representar el numero en base decimal
     *                                  de entrada en base 40
     */

    public static String fromDecimalToBase40(long number)
            throws IllegalArgumentException {
        return fromDecimalToBaseX(number, 40);
    }

    public static String fromDecimalToBase40(double number)
            throws IllegalArgumentException {
        return fromDecimalToBaseX(Double.valueOf(number + "").longValue(), 40);
    }

    /**
     * Obtiene la representación en base 40 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con el caracter descrito por el parámetro 'paddingCharacter' a la izquierda para completar un tamaño
     * identificado por el parámetro de entrada 'size'
     *
     * @param number           El número en base decimal al cual se le desea obtener su representación en base 40
     * @param size             El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que
     *                         representa el número convertido a base 40 dicho tamaño se ignora
     * @param paddingCharacter El caracter con el que se quiere rellenar a las izquierda del numero convertido a
     *                         base 40
     * @return Un String con la representación del número de entrada en base decimal convertido a base 40
     * @throws IllegalArgumentException Si no existen al menos 40 caracteres para representar el numero en base decimal
     *                                  de entrada en base 40
     */

    public static String fromDecimalToBase40LeftPaddedToXCharacters(long number, int size, char paddingCharacter)
            throws IllegalArgumentException {
        return fromDecimalToBaseXLeftPaddedToXCharacters(number, size, 40, paddingCharacter);
    }

    /**
     * Obtiene la representación en base 40 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con ceros a la izquierda para completar un tamaño identificado por el parámetro de entrada 'size'
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 40
     * @param size   El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que representa el
     *               número convertido a base 40 dicho tamaño se ignora
     * @return Un String con la representación del número de entrada en base decimal convertido a base 40
     * @throws IllegalArgumentException Si no existen al menos 40 caracteres para representar el numero en base decimal
     *                                  de entrada en base 40
     */

    public static String fromDecimalToBase40ZeroLeftPaddedToXCharacters(long number, int size)
            throws IllegalArgumentException {
        return fromDecimalToBaseXZeroLeftPaddedToXCharacters(number, size, 40);
    }

    /**
     * Obtiene la representación en base 62 del numero en base decimal representado por el parámetro de entrada
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 62
     * @return Un String con la representación del número de entrada en base decimal convertido a base 62
     * @throws IllegalArgumentException Si no existen al menos 62 caracteres para representar el numero en base decimal
     *                                  de entrada en base 62
     */

    public static String fromDecimalToBase62(long number)
            throws IllegalArgumentException {
        return fromDecimalToBaseX(number, 62);
    }

    /**
     * Obtiene la representación en base 62 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con el caracter descrito por el parámetro 'paddingCharacter' a la izquierda para completar un tamaño
     * identificado por el parámetro de entrada 'size'
     *
     * @param number           El número en base decimal al cual se le desea obtener su representación en base 62
     * @param size             El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que
     *                         representa el número convertido a base 62 dicho tamaño se ignora
     * @param paddingCharacter El caracter con el que se quiere rellenar a las izquierda del numero convertido a
     *                         base 62
     * @return Un String con la representación del número de entrada en base decimal convertido a base 62
     * @throws IllegalArgumentException Si no existen al menos 62 caracteres para representar el numero en base decimal
     *                                  de entrada en base 62
     */

    public static String fromDecimalToBase62LeftPaddedToXCharacters(long number, int size, char paddingCharacter)
            throws IllegalArgumentException {
        return fromDecimalToBaseXLeftPaddedToXCharacters(number, size, 62, paddingCharacter);
    }

    /**
     * Obtiene la representación en base 62 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con ceros a la izquierda para completar un tamaño identificado por el parámetro de entrada 'size'
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 62
     * @param size   El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que representa el
     *               número
     *               convertido a base 62 dicho tamaño se ignora
     * @return Un String con la representación del número de entrada en base decimal convertido a base 62
     * @throws IllegalArgumentException Si no existen al menos 62 caracteres para representar el numero en base decimal
     *                                  de entrada en base 62
     */

    public static String fromDecimalToBase62ZeroLeftPaddedToXCharacters(long number, int size)
            throws IllegalArgumentException {
        return fromDecimalToBaseXZeroLeftPaddedToXCharacters(number, size, 62);
    }

    /**
     * Obtiene la representación en base 8 del numero en base decimal representado por el parámetro de entrada
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 8
     * @return Un String con la representación del número de entrada en base decimal convertido a base 8
     * @throws IllegalArgumentException Si no existen al menos 8 caracteres para representar el numero en base decimal
     *                                  de entrada en base 8
     */

    public static String fromDecimalToBase8(long number)
            throws IllegalArgumentException {
        return fromDecimalToBaseX(number, 8);
    }

    /**
     * Obtiene la representación en base 8 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con el caracter descrito por el parámetro 'paddingCharacter' a la izquierda para completar un tamaño
     * identificado por el parámetro de entrada 'size'
     *
     * @param number           El número en base decimal al cual se le desea obtener su representación en base 8
     * @param size             El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que
     *                         representa el número convertido a base 8 dicho tamaño se ignora
     * @param paddingCharacter El caracter con el que se quiere rellenar a las izquierda del numero convertido a
     *                         base 8
     * @return Un String con la representación del número de entrada en base decimal convertido a base 8
     * @throws IllegalArgumentException Si no existen al menos 8 caracteres para representar el numero en base decimal
     *                                  de entrada en base 8
     */

    public static String fromDecimalToBase8LeftPaddedToXCharacters(long number, int size, char paddingCharacter)
            throws IllegalArgumentException {
        return fromDecimalToBaseXLeftPaddedToXCharacters(number, size, 8, paddingCharacter);
    }

    /**
     * Obtiene la representación en base 8 del numero en base decimal representado por el parámetro de entrada,
     * rellenando con ceros a la izquierda para completar un tamaño identificado por el parámetro de entrada 'size'
     *
     * @param number El número en base decimal al cual se le desea obtener su representación en base 8
     * @param size   El tamaño de salida deseado. Si el tamaño es menor que el tamaño del String que representa el
     *               número convertido a base 8 dicho tamaño se ignora
     * @return Un String con la representación del número de entrada en base decimal convertido a base 8
     * @throws IllegalArgumentException Si no existen al menos 8 caracteres para representar el numero en base decimal
     *                                  de entrada en base 8
     */

    public static String fromDecimalToBase8ZeroLeftPaddedToXCharacters(long number, int size)
            throws IllegalArgumentException {
        return fromDecimalToBaseXZeroLeftPaddedToXCharacters(number, size, 8);
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Cannot clone instance of this class");
    }

}