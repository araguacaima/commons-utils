/*
 * @(#)OperandConstants.java
 *
 * JReversePro - Java Decompiler / Disassembler.
 * Copyright (C) 2000 2001 Karthik Kumar.
 * EMail: akkumar@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or modify
 * it , under the terms of the GNU General   License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General   License for more details.
 * You should have received a copy of the GNU General   License
 * along with this program.If not, write to
 *  The Free Software Foundation, Inc.,
 *  59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package jreversepro.runtime;

/**
 * This contains constants maintaining the precedence of operators.
 *
 * @author Jiri Malak.
 */
public interface OperandConstants {

    int L_ADD = 12;
    int L_BITAND = 8;
    int L_BITOR = 6;
    int L_BITXOR = 7;
    int L_CAST = 15;
    int L_COMMA = 1;
    int L_DIV = 13;
    int L_EVAL = 2;
    int L_INDEX = 15;
    int L_LOGAND = 5;
    int L_LOGEQ = 9;
    int L_LOGIOF = 14;
    int L_LOGNEQ = 9;
    int L_LOGOR = 4;
    int L_LOGREL = 10;
    int L_MOD = 13;
    int L_MUL = 13;
    int L_REF = 15;
    int L_SHIFT = 11;
    int L_SUB = 12;
    int L_TERN = 3;
    int L_UNARY = 14;
    int VALUE = 99;
}
