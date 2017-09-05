package org.araguacaima.utils;

import org.springframework.stereotype.Component;

/**
 * Constantes UNICODE y utilidades de encoding varias
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */
@Component
public class UnicodeUtils {

    // ***** Letras acentuadas, con tildes, con dieresis, y similares... *****

    public static final String A_MAY_CON_ACENTO = "\u00c1";
    public static final String A_MIN_CON_ACENTO = "\u00e1";
    public static final char COMILLA_DOBLE_1 = '\u0021'; // u0034
    public static final char COMILLA_DOBLE_2 = '\u0093'; // u0147
    public static final char COMILLA_DOBLE_3 = '\u0094'; // u0148
    public static final char DOLAR = '\u0024';
    public static final String ENIE_MAY = "\u00d1";
    public static final String ENIE_MIN = "\u00f1";
    public static final String E_MAY_CON_ACENTO = "\u00c9";
    public static final String E_MIN_CON_ACENTO = "\u00e9";
    public static final char INTERROGACION_ABRE = '\u00bf';
    public static final char INTERROGACION_CIERRA = '\u003f';
    public static final String I_MAY_CON_ACENTO = "\u00cd";
    public static final String I_MIN_CON_ACENTO = "\u00ed";

    // ***** Caracteres especiales... *****
    public static final char ORDINAL = '\u00B0';

    // ***** Signos de puntuacion... *****
    public static final String O_MAY_CON_ACENTO = "\u00d3";
    public static final String O_MIN_CON_ACENTO = "\u00f3";
    public static final String U_MAY_CON_ACENTO = "\u00da";
    // public  final char COMILLA_DOBLE_4 = '\u1524';
    public static final String U_MAY_CON_DIERESIS = "\u00dc";
    public static final String U_MIN_CON_ACENTO = "\u00fa";
    public static final String U_MIN_CON_DIERESIS = "\u00fc";

}
