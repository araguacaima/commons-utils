/*
 * JSwitchBlock.java
 *
 * Created on September 5, 2002, 4:56 PM
 */

package jreversepro.reflect.method;

import jreversepro.revengine.JBranchEntry;

/**
 * @author pazandak@objs.com,  Copyright 2002.
 */
public class JSwitchBlock extends JBlockObject {

    /**
     * Contains stringified switch expression
     */
    private final String expr;

    /**
     * Creates a new instance of JSwitchBlock
     *
     * @param _jbe  Branch
     * @param _expr The expression
     */
    public JSwitchBlock(JBranchEntry _jbe, String _expr) {
        /*
      Associated Branch Entry
     */
        expr = _expr;
    }

    /**
     * Outputs any starting code to open the block
     *
     * @return The starting code to open the block
     */
    protected String getEntryCode() {
        return "switch (" + expr + ") {\n";
    }

    /**
     * Outputs any terminating code to close the block
     */
    protected String getExitCode() {
        return "\n" + indent + "}\n";
    }

}
