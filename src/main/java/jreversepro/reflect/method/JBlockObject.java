/*
 * JBlockObject.java
 *
 * Created on September 4, 2002, 2:50 PM
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

import jreversepro.reflect.JLineOfCode;

import java.util.Vector;

/**
 * @author pazandak@objs.com -- Copyright 2002.
 */
public abstract class JBlockObject {

    /**
     * Contains list of all blocks and stmts contained within this block
     */
    private final Vector<Object> blocksNstmts;

    /**
     * Default indentation
     */
    private final String defaultIndent = "     ";

    /**
     * Directed indentation
     */
    protected String indent = "";

    /**
     * Creates a new instance of BranchObject
     */
    public JBlockObject() {
        blocksNstmts = new Vector<>();
    }

    /**
     * Called to add a sub block
     *
     * @param _jbo BlockObject to be added.
     */
    public void addBlock(JBlockObject _jbo) {
        blocksNstmts.add(_jbo);
    }

    /**
     * Called to add a line of code.
     *
     * @param _loc LineofCode to be added.
     */
    public void addStatement(JLineOfCode _loc) {
        blocksNstmts.add(_loc);
    }

    /**
     * @return Returns any starting code to open the block as a String.
     */
    protected String getEntryCode() {
        return "";
    }

    /**
     * @return Returns any starting code to open the block as a JLineOfCode.
     */
    protected JLineOfCode getEntryLineOfCode() {
        return new JLineOfCode(indent + getEntryCode(), this, JLineOfCode.ENTRY);
    }

    /**
     * @return Returns any terminating code to close the block as a String.
     */
    protected String getExitCode() {
        return "";
    }

    /**
     * @return Returns any terminating code to close the block as a
     * JLineOfCode.
     */
    protected JLineOfCode getExitLineOfCode() {
        return new JLineOfCode(indent + getExitCode(), this, JLineOfCode.EXIT);
    }

    /**
     * @param _indent Indentation String to be appended.
     * @return Outputs the method code contained in this block
     * (and sub-blocks) as a vector of JLineOfCode objects
     */
    public Vector<Object> getFlattenedCode(String _indent) {

        indent = _indent;
        Vector<Object> locs = new Vector<>();

        //Adds block entry code as a JLineOfCode
        locs.add(getEntryLineOfCode());

        //Print code inside block
        for (Object o : blocksNstmts) {
            if (o instanceof JBlockObject) {
                locs.add(((JBlockObject) o).
                        getFlattenedCode(_indent + defaultIndent));
            } else if (o instanceof JLineOfCode) {
                locs.add(((JLineOfCode) o).toString(_indent + defaultIndent));
            }
        }

        //Adds block exit code as a JLineOfCode
        locs.add(getExitLineOfCode());

        return locs;
    }

    /**
     * @return Returns TRUE if block has only one stmt or block and does not
     * need bracketing, otherwise returns FALSE.
     */
    protected boolean isSimpleBlock() {
        return blocksNstmts.size() == 1;
    }

    /**
     * Called to remove last block.
     */
    public void removeLastBlock() {
        if (blocksNstmts.size() > 0) {
            blocksNstmts.removeElementAt(blocksNstmts.size() - 1);
        }
    }

    /**
     * Called to remove the last line of code added
     *
     * @return Last Statement if the block has got at least one
     * statement.
     * NULL, if the block has no statement.
     */
    public JLineOfCode removeLastStatement() {
        if (blocksNstmts.size() > 0) {
            return (JLineOfCode) blocksNstmts.remove(blocksNstmts.size() - 1);
        } else {
            return null;
        }
    }

    /**
     * @param _indent Indentation.
     * @return Outputs the method code contained in this block
     * (and sub-blocks) as a string
     */
    public String toString(String _indent) {

        indent = _indent;

        StringBuilder sb = new StringBuilder();

        //Print block entry code
        sb.append(indent).append(getEntryCode());

        //Print code inside block
        //log.info("[numBlocks="+blocksNstmts.size());
        for (Object o : blocksNstmts) {
            if (o instanceof JBlockObject) {
                sb.append(((JBlockObject) o).toString(indent + defaultIndent));
            } else if (o instanceof JLineOfCode) {
                sb.append(((JLineOfCode) o).toString(indent + defaultIndent));
            }
        }

        //Print block exit code
        sb.append(indent).append(getExitCode());

        return sb.toString();
    }
}
