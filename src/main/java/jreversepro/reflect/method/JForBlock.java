/*
 * JForBlock.java
 *
 * Created on September 4, 2002, 2:52 PM
 * JReversePro - Java Decompiler / Disassembler.
 * Copyright (C) 2002 pazandak@objs.com
 * EMail: pazandak@objs.com
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

package jreversepro.reflect.method;

import jreversepro.revengine.JBranchEntry;

/**
 * @author pazandak@objs.com -- Copyright 2002.
 */
public class JForBlock extends JBlockObject {

    /**
     * Contains stringified init FOR expression
     */
    private String init;
    /**
     * Contains stringified loop FOR expression
     */
    private String loop;
    /**
     * Contains stringified conditional test FOR expression
     */
    private String test;

    /**
     * Creates a new instance of JForBlock
     *
     * @param _jbe  Branch
     * @param _test The test expression
     */
    public JForBlock(JBranchEntry _jbe, String _test) {
        this(_jbe, "", _test, "");
    }

    /**
     * Creates a new instance of JForBlock
     *
     * @param _jbe  Branch
     * @param _init The init expression
     * @param _test The test expression
     * @param _loop The loop expression
     */
    public JForBlock(JBranchEntry _jbe, String _init, String _test, String _loop) {
        /*
      Associated Branch Entry
     */
        init = _init;
        test = _test;
        loop = _loop;
    }

    /**
     * Outputs any starting code to open the block
     *
     * @return The starting code to open the block
     */
    protected String getEntryCode() {
        if (isSimpleBlock()) {
            return "for (" + init + ";" + test + ";" + loop + ") \n";
        } else {
            return "for (" + init + ";" + test + ";" + loop + ") {\n";
        }
    }

    /**
     * Outputs any terminating code to close the block
     *
     * @return The terminating code to close the block
     */
    protected String getExitCode() {
        if (isSimpleBlock()) {
            return "\n";
        } else {
            return "}\n";
        }
    }

    /**
     * Set the init expression
     *
     * @param _init The init expression
     */
    public void setInitExpr(String _init) {
        init = _init;
    }

    /**
     * Set the loop expression
     *
     * @param _loop The loop expression
     */
    public void setLoopExpr(String _loop) {
        loop = _loop;
    }

    /**
     * Set the test expression
     *
     * @param _test The test expression
     */
    public void setTestExpr(String _test) {
        test = _test;
    }

}
