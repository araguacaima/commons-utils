/**
 * @(#)AppConstants.java JReversePro - Java Decompiler / Disassembler.
 * Copyright (C) 2000 2001 Karthik Kumar.
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
 **/
package jreversepro.common;

import java.util.Date;

/**
 * @author Karthik Kumar
 */
public interface AppConstants {
    /**
     * DecompileFlag Property.
     */
    String DECOMPILE_FLAG = "Decompile";
    /**
     * Font of GUI window.
     */
    String FONT = "Font";
    /**
     * Look And Feel of Window.
     */
    String L_AND_F = "LookAndFeel";
    /**
     * MAGIC corresponds to the Magic number appearing in
     * the beginning of class files.
     */
    int MAGIC = 0xCAFEBABE;
    /**
     * Name of property file.
     */
    String PROP_FILE = "jrev.ini";
    /**
     * Heading of property file.
     */
    String PROP_HEADING = "JReversePro - Java Decompiler / Disassembler";
    /**
     * Title of GUI window.
     */
    String TITLE = "JReversePro - Java Decompiler / Disassembler";
    /**
     * Version of the software.
     */
    String VERSION = "1.4.1";
    /**
     * GPL Information.
     */
    String GPL_INFO = "// JReversePro v " + VERSION + " " + (new Date(System.currentTimeMillis())) + "\n// " +
            "http://jrevpro.sourceforge.net" + "\n// Copyright (C)2000 2001 2002 Karthik Kumar." + "\n// JReversePro " +
            "comes with ABSOLUTELY NO WARRANTY;" + "\n// This is free software, and you are welcome to redistribute"
            + "\n// it under certain conditions;See the File 'COPYING' for " + "more details.\n";
    /**
     * XPosition of GUI window.
     */
    String XPOS = "XPos";
    /**
     * Width of GUI window.
     */
    String XSIZE = "Width";
    /**
     * YPosition of GUI window.
     */
    String YPOS = "YPos";
    /**
     * Height of GUI window.
     */
    String YSIZE = "Height";
}
