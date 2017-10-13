/*
 * @(#)KeyWords.java
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
 **/
package jreversepro.common;

public interface KeyWords {
    /**
     * Exception Class of type 'any'.
     */
    String ANY = "<any>";
    /**
     * 'boolean' datatype.
     */
    String BOOLEAN = "Z";
    String BREAK = "break";
    /**
     * 'byte' datatype.
     */
    String BYTE = "B";
    String CASE = "case";
    /**
     * 'char' datatype.
     */
    String CHAR = "C";
    String CLASS = "class";
    /**
     * JVM Representation of java.lang.String
     */
    String CLASS_STRING = "java/lang/String";
    /**
     * JVM representation of the method static {.. }
     */
    String CLINIT = "<clinit>";
    String CLOSE_BRACKET = "]";
    String COND_AND = "&&";
    String COND_NOT = "!";
    String COND_OR = "||";
    String CONTINUE = "continue";
    String DEFAULT = "default";
    /**
     * Default Package that is included when the JVM is
     * launched in the beginning.
     */
    String DEFAULT_PACKAGE = "java.lang";
    /**
     * 'double' datatype.
     */
    String DOUBLE = "D";
    String EQUALTO = " = ";
    String FALSE = "false";
    /**
     * 'float' datatype.
     */
    String FLOAT = "F";
    //Constants containing KeyWords
    String FOREIGN_CLASS = "<ForeignClass>";
    String FOREIGN_OBJ = "<ForeignObject>";
    String GOTO = "goto";
    /**
     * JVM representation of the constructor method.
     */
    String INIT = "<init>";
    String INSTANCEOF = "instanceof";
    /**
     * 'int' datatype.
     */
    String INT = "I";
    String INTERFACE = "interface";
    String JVM_BOOLEAN = "Z";
    String JVM_CHAR = "C";
    String JVM_VOID = "V";
    /**
     * JVM representation of the class java.lang.Object
     */
    String LANG_OBJECT = "java/lang/Object";
    String LENGTH = "length";
    /**
     * 'long' datatype.
     */
    String LONG = "J";
    String NEW = "new";
    /**
     * 'null' datatype.
     */
    String NULL = "null";
    String OPEN_BRACKET = "[";
    String OPR_EQ = "==";
    String OPR_GE = ">=";
    // Operators
    String OPR_GT = ">";
    String OPR_LE = "<=";
    String OPR_LT = "<";
    String OPR_NE = "!=";
    String OPR_NOT = "!";
    /**
     * datatype is a reference to an object
     */
    String REFERENCE = "reference";
    String RETURN = "return";
    /**
     * datatype is of type returnaddress
     */
    String RET_ADDR = "returnaddress";
    /**
     * 'short' datatype.
     */
    String SHORT = "S";
    char SPACE = ' ';
    String STATIC = "static";
    String SUPER = "super";
    String SWITCH = "switch";
    /**
     * this pointer variable name
     */
    String THIS = "this";
    /**
     * Refers to the name of the current class type.
     */
    String THISCLASS = "**this_class**";
    String THROW = "throw";
    String TRUE = "true";
    /**
     * 'void' datatype.
     */
    String VOID = "void";
}
