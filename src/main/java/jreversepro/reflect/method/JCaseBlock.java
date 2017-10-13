/*
 * JCaseBlock.java
 *
 * Created on September 5, 2002, 4:55 PM
 */

package jreversepro.reflect.method;

import jreversepro.common.KeyWords;
import jreversepro.revengine.JBranchEntry;

import java.util.StringTokenizer;

/**
 * @author akkumar
 */
public class JCaseBlock extends JBlockObject implements KeyWords {

    /**
     * Contains stringified switch expression
     */
    private final String expr;

    /**
     * Creates a new instance of JCaseBlock
     *
     * @param _jbe  Branch
     * @param _expr Expression
     */
    public JCaseBlock(JBranchEntry _jbe, String _expr) {
        /*
      Associated Branch Entry
     */
        JBranchEntry branch = _jbe;
        expr = _expr;
    }

    /**
     * Outputs any starting code to open the block
     */
    protected String getEntryCode() {
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(expr, ",");
        while (st.hasMoreTokens()) {
            String caseTarget = st.nextToken();
            sb.append("\n").append(indent);
            if (!caseTarget.equals(DEFAULT)) {
                sb.append("case ");
            }
            sb.append(caseTarget).append(": ");
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Outputs any terminating code to close the block
     *
     * @return The terminating code to close the block
     */
    protected String getExitCode() {
        return "";
    }

}
