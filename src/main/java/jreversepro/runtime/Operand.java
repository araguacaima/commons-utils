/*
  @(#)Operand.java JReversePro - Java Decompiler / Disassembler.
 * Copyright (C) 2000 Karthik Kumar.
 * EMail: akkumar@users.sourceforge.net
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it , under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.If not, write to
 * The Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package jreversepro.runtime;

import jreversepro.common.KeyWords;

/**
 * Abstraction of an element on the JVM Operand stack.
 *
 * @author Karthik Kumar
 */
public class Operand implements OperandConstants {

    /**
     * datatype of the operand. ( stack element ) .
     */
    final String datatype;
    /**
     * precedence of the operand.
     */
    final int precedence;
    /**
     * value of the operand
     */
    final String value;

    /**
     * @param value      Value int.
     * @param datatype   Datatype of the operand.
     * @param precedence precedence of the operand.
     */
    public Operand(int value, String datatype, int precedence) {
        this(String.valueOf(value), datatype, precedence);
    }

    /**
     * @param value      Value in String.
     * @param datatype   Datatype of the operand.
     * @param precedence precedence of the operand.
     */
    public Operand(String value, String datatype, int precedence) {
        this.value = value;
        this.datatype = datatype;
        this.precedence = precedence;
    }

    /**
     * @return Returns datatype.
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * @return Returns precedence.
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * @return Returns the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param precedence precedence of the operand.
     * @return Returns value taking into account precendence too,
     */
    public String getValueEx(int precedence) {
        if (this.precedence >= precedence) {
            return value;
        } else {
            return "(" + value + ")";
        }
    }

    /**
     * @return Returns true, if this type is one of the following.
     * integer,
     * boolean,
     * byte,
     * character,
     * short,
     * float,
     * reference,
     * return address.
     * In case it is either Long / Double the datatype belongs to
     * cat2
     */
    public boolean isCategory1() {
        switch (datatype) {
            case KeyWords.INT:
            case KeyWords.BOOLEAN:
            case KeyWords.BYTE:
            case KeyWords.CHAR:
            case KeyWords.SHORT:
            case KeyWords.FLOAT:
            case KeyWords.REFERENCE:
            case KeyWords.RET_ADDR:

                return true;

            default:
                return !datatype.equals(KeyWords.LONG) && !datatype.equals(KeyWords.DOUBLE);
        }
    }// End of Category 1 testing routine

    /**
     * @return Stringified format of this.
     */
    public String toString() {
        return "(" + datatype + ", " + value + ")";
    }
}
