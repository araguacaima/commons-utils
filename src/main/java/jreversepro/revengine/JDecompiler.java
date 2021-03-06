/*
  @(#)JDecompiler.java JReversePro - Java Decompiler / Disassembler.
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
 */

package jreversepro.revengine;

import jreversepro.common.Helper;
import jreversepro.common.JJvmOpcodes;
import jreversepro.common.KeyWords;
import jreversepro.parser.ClassParserException;
import jreversepro.reflect.JConstantPool;
import jreversepro.reflect.JImport;
import jreversepro.reflect.JInstruction;
import jreversepro.reflect.JMethod;
import jreversepro.runtime.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * This decompiles the source code.
 *
 * @author Karthik Kumar
 */
public class JDecompiler implements BranchConstants, KeyWords, JReverseEngineer, JJvmOpcodes, OperandConstants {

    /**
     * Reference to classes imported by this class.
     */
    static JImport importInfo;
    /**
     * List containing the bytecode instructions.
     * List of 'JInstruction'.
     */
    final List byteIns;
    /**
     * Vector of JBranchEntry of TYPE_CATCH
     * and TYPE_CATCH_ANY
     */
    final Vector<JBranchEntry> catchBranches;
    /**
     * Reference to ConsantPoolInformation.
     */
    final JConstantPool cpInfo;
    /**
     * Current Method for which code is to be decompiled.
     */
    final JMethod curMethod;
    /**
     * Map of exceptions in the given block.
     */
    final Map mapCatchJExceptions;
    /**
     * SymbolTable generated for the current method.
     */
    final JSymbolTable symTable;
    /**
     * Class containing the reference to branch table.
     */
    JBranchTable branches;
    /**
     * Last encountered instruction.
     */
    int lastIns = 0;

    /**
     * index offset into bytecode array of current stmt
     */
    int lastInsPos = 0;

    /**
     * JDecompiler constructor.
     *
     * @param rhsMethod Method to be decompiled.
     * @param rhsCpInfo ConstantPool of the class to be decompiled.
     */
    public JDecompiler(JMethod rhsMethod, JConstantPool rhsCpInfo) {

        importInfo = rhsCpInfo.getImportedClasses();
        curMethod = rhsMethod;
        cpInfo = rhsCpInfo;
        symTable = new JSymbolTable(rhsMethod, importInfo);
        branches = new JBranchTable(curMethod);
        catchBranches = new Vector<>();

        byteIns = rhsMethod.getInstructions();
        mapCatchJExceptions = rhsMethod.getAllCatchJExceptions();
    }

    /**
     * Finalizer of the class.
     */
    protected void finalize() {
        branches = null;
    }

    /**
     * Don't depend on LineNumberTable Attribute of a method,
     * as it is optional. Available only if compiled with
     * debugging options on. ( -g ).
     *
     * @throws RevEngineException   Encountered when the decompiler could not
     *                              decompile the code. Specific to decompiling engine.
     * @throws ClassParserException Thrown in case of constantpool reference.
     */
    public void genCode()
            throws RevEngineException, ClassParserException {

        loadSymbolTable();

        curMethod.setSymbolTable(this.getSymbolTable());

        if (byteIns.size() != 0) {
            try {
                //Start decompiling code.
                JCollatingTable collatter = loadBranchTable();
                Helper.log("Before collation " + collatter.toString());
                collatter.identifyWhileLoops(branches.getGotoTable());
                collatter.collate();

                Helper.log("After collation " + collatter.toString());
                branches.setTables(collatter.getEffectiveBranches());

                //              branches.setEndTryCatch(byteIns);
                branches.identifyMoreBranches();

                branches.sort();

                genSource();
                //End Decompile
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RevEngineException(curMethod.getName() + " decompilation failed. Please feel " + " free to " +
                        "" + "" + " report the problem to me at " + " akkumar@users.sourceforge.net. ");
            }
        }
    }

    /**
     * Loads the Local Symbol Table information.
     *
     * @throws RevEngineException   Thrown in case of any error
     *                              loading the symbol table.
     * @throws ClassParserException Thrown in case of invalid
     *                              constantpool reference.
     */
    public void loadSymbolTable()
            throws RevEngineException, ClassParserException {
        if (byteIns != null) {
            //          Helper.log("Loading Symbol Table **********");

            JRunTimeFrame rtf = new JRunTimeFrame(cpInfo, symTable, curMethod.getReturnType());

            JOperandStack jos = new JOperandStack();

            for (Object byteIn : byteIns) {
                JInstruction ins = (JInstruction) byteIn;
                int varIndex = ins.isStoreInstruction();
                String excDataType = (String) mapCatchJExceptions.get(ins.index);
                if (excDataType != null) {
                    if (varIndex == JInstruction.INVALID_VAR_INDEX && excDataType.equals(ANY)) {
                        varIndex = ins.referredVariable();
                    }
                    symTable.assignDataType(varIndex, excDataType, ins.index, true);

                    jos.push(FOREIGN_OBJ, FOREIGN_CLASS, VALUE);

                } else {
                    if (branches.isJSRTarget(ins.index)) {
                        jos.push(FOREIGN_OBJ, FOREIGN_CLASS, VALUE);
                    }
                    if (varIndex != JInstruction.INVALID_VAR_INDEX) {
                        //Save the local variable index.
                        symTable.assignDataType(varIndex, jos.topDatatype(), ins.index, false);
                    }
                }
                rtf.operateStack(ins, jos);

                //Add a referenced count of line numbers
                int referredVar = ins.referredVariable();
                if (referredVar != JInstruction.INVALID_VAR_INDEX) {
                    Helper.log(ins.toString() + "  " + ins.referredVariable());
                    symTable.addReference(referredVar, jos.topDatatype(), ins.index);
                }

                if (ins.opcode == OPCODE_INVOKEINTERFACE) {
                    symTable.touchVariable(rtf.getInvokedObject().getValue(), rtf.getInvokedObject().getDatatype());
                } else if (ins.opcode == OPCODE_GOTO) {
                    branches.addGotoEntry(ins.index, ins.getTargetPc());
                } else if (ins.opcode == OPCODE_JSR) {
                    branches.addJSRPc(ins.getTargetPc());
                } else if (ins.opcode == OPCODE_RET) {
                    branches.addRetPc(ins.getNextIndex());
                } else if (ins.opcode == OPCODE_GOTOW) {
                    branches.addGotoEntry(ins.index, ins.getTargetPcW());
                } else if (ins.opcode == OPCODE_JSRW) {
                    branches.addJSRPc(ins.getTargetPcW());
                }
            }
            //          Helper.log("Loaded Symbol Table **********");
            //          Helper.log(symTable.toString());
        }
    }

    /**
     * @return Returns the SymbolTable.
     */
    public JSymbolTable getSymbolTable() {
        return symTable;
    }

    /**
     * Loads the Branch Table for a given method.
     * After loading the branch table this method collates the same and
     * returns a reference to JCollatingTable.
     *
     * @return Reference of type JCollatingTable.
     * @throws IOException        Thrown in case of any i/o error.
     * @throws RevEngineException Thrown in case any error with the
     *                            decompiling engine.
     * @throws IOException        Thrown in case an IO error.
     */
    private JCollatingTable loadBranchTable()
            throws RevEngineException, IOException {
        JRunTimeFrame rtf = new JRunTimeFrame(cpInfo, symTable, curMethod.getReturnType());

        JOperandStack jos = new JOperandStack();
        JCollatingTable collatter = new JCollatingTable(curMethod);

        int prevCode = 0;
        boolean eolFlag = false;
        for (Object byteIn : byteIns) {
            JInstruction thisIns = (JInstruction) byteIn;
            if (thisIns.isEndOfCatch()) {
                closeCatchBranch(thisIns.getNextIndex());
            }
            if (eolFlag) {
                prevCode = thisIns.index;
                eolFlag = false;
            }
            int insIndex = thisIns.index;
            String exc = (String) mapCatchJExceptions.get(insIndex);
            if (exc != null) {
                // push exception on the operand stack for catch
                jos.push(FOREIGN_OBJ, FOREIGN_CLASS, VALUE);
                // add catch or catch (any) branch to branch table
                int storeIndex = thisIns.isStoreInstruction();
                if (storeIndex == JInstruction.INVALID_VAR_INDEX && exc.equals(ANY)) {
                    storeIndex = thisIns.referredVariable();
                }
                String varName = symTable.getName(storeIndex, insIndex);
                String className = exc;
                if (exc.equals(ANY)) {
                    addCatchBranch(insIndex,
                            new JBranchEntry(curMethod,
                                    insIndex,
                                    insIndex,
                                    -1,
                                    TYPE_CATCH_ANY,
                                    className,
                                    varName,
                                    ""));
                } else {
                    className = JImport.getClassName(symTable.getDataType(storeIndex, insIndex));
                    addCatchBranch(insIndex,
                            new JBranchEntry(curMethod, insIndex, insIndex, -1, TYPE_CATCH, className, varName, ""));
                }
                eolFlag = true;
            } else if (branches.isJSRTarget(thisIns.index)) {
                // push exception on the operand stack for JSR
                jos.push(FOREIGN_OBJ, FOREIGN_CLASS, VALUE);
                eolFlag = true;
            }

            if (thisIns.opcode == OPCODE_TABLESWITCH || thisIns.opcode == OPCODE_LOOKUPSWITCH) {
                branches.addSwitch(new JSwitchTable(curMethod, thisIns, jos.peek(), branches.getGotoTable()));
                eolFlag = true;
            }
            try {
                rtf.operateStack(thisIns, jos);
            } catch (Exception ex) {
                throw new RevEngineException("Error processing " + " instruction " + thisIns.toString(), ex);
            }
            if (thisIns.isAnIfIns()) {
                if (thisIns.index > thisIns.getTargetPc()) {
                    collatter.addConditionalBranch(thisIns, prevCode, TYPE_DO_WHILE, rtf.getOpr1(), rtf.getOpr2());
                } else {
                    collatter.addConditionalBranch(thisIns, prevCode, TYPE_IF, rtf.getOpr1(), rtf.getOpr2());
                }
                eolFlag = true;
            } else if (thisIns.opcode == OPCODE_MONITORENTER) {
                branches.addMonitorPc(thisIns.index, rtf.getStatement());
                eolFlag = true;
            } else {
                eolFlag |= thisIns.isEndOfLine() || (thisIns.isInvokeIns() && jos.empty()) || thisIns.opcode ==
                        OPCODE_GOTO || thisIns.opcode == OPCODE_GOTOW || thisIns.opcode == OPCODE_JSR || thisIns
                        .opcode == OPCODE_JSRW || thisIns.opcode == OPCODE_RET;
            }
        }

        // add try or try (any) branch to branch table
        branches.addTryBlocks(curMethod.getexceptionBlocks());

        return collatter;
    }

    /**
     * Generates the source code for the given method.
     *
     * @throws RevEngineException   Thrown in case of any error
     *                              while generating the source code.
     * @throws ClassParserException Thrown in case of an invalid
     *                              constantpool reference.
     */
    private void genSource()
            throws RevEngineException, ClassParserException {

        JRunTimeContext context = createRuntimeContext();

        for (Object byteIn : byteIns) {
            JInstruction ins = (JInstruction) byteIn;

            //Begin Control Blocks.
            List brEnt = branches.startsWith(ins.index);
            if (brEnt.size() != 0) {
                context.getBeginStmt(brEnt, ins.index, symTable);
            }

            processJVMInstruction(ins, context);

            //End Control Blocks
            context.getEndStmt(ins.getNextIndex());
        }

        context.getFinalBlockStmt();
    }

    /**
     * Closes the Catch Branch if the given instruction
     * is an end-of-catch instruction
     *
     * @param closeIndex Current Instruction to examine if the
     *                   catch block can be closed.
     */
    private void closeCatchBranch(int closeIndex) {
        if (catchBranches.size() != 0) {
            JBranchEntry lastEnt = catchBranches.lastElement();
            if (lastEnt.getTargetPc() == -1) {
                lastEnt.setTargetPc(closeIndex);
                catchBranches.remove(lastEnt);
            }
            // Else the target of catch is already set.
            // We dont need to do anything here.
        }
    }

    /**
     * Adds a Catch Branch entry.
     *
     * @param insIndex Instruction Index.
     * @param brent    BranchEntry for the catch branch to be added.
     */
    private void addCatchBranch(int insIndex, JBranchEntry brent) {
        closeCatchBranch(insIndex);
        branches.add(brent);
        catchBranches.add(brent);
    }

    /**
     * Creates a runtime context afresh for use for decompilation.
     *
     * @return A Runtime context created afresh.
     */
    private JRunTimeContext createRuntimeContext() {
        JRunTimeFrame rtf = new JRunTimeFrame(cpInfo, symTable, curMethod.getReturnType());
        JOperandStack jos = new JOperandStack();
        return new JRunTimeContext(this, curMethod, rtf, jos, branches);
    }

    /**
     * Process a single JVM instruction.
     *
     * @param ins     Instruction to be processed.
     * @param context Runtime context for this method.
     * @throws RevEngineException   Thrown in case of any error
     *                              that may occur while processing this particular instruction.
     * @throws ClassParserException Thrown in case of an invalid
     *                              constantpool reference.
     */
    private void processJVMInstruction(JInstruction ins, JRunTimeContext context)
            throws RevEngineException, ClassParserException {

        JOperandStack jos = context.getOperandStack();

        if (mapCatchJExceptions.get(ins.index) != null) {
            jos.push(FOREIGN_OBJ, FOREIGN_CLASS, VALUE);
        } else if (branches.isJSRTarget(ins.index)) {
            jos.push(FOREIGN_OBJ, FOREIGN_CLASS, VALUE);
        }

        boolean foreign = jos.isTopDatatypeForeign();
        int varIndex = ins.isStoreInstruction();
        String datatype = symTable.declare(varIndex, ins.index);

        context.executeInstruction(ins);

        //        Helper.log(ins  + " " + jos.toString());
        if (Helper.isDebug()) {
            context.addTextCode("// " + ins);
        }
        if (ins.isEndOfLine() && jos.empty()) {
            String stmt = "";
            if (varIndex != JInstruction.INVALID_VAR_INDEX && datatype != null) {
                // A Valid Store instruction.
                stmt = datatype + " ";
            }
            if (!foreign) {
                context.addCode(lastIns, ins.index + ins.length - 1, lastInsPos, ins.position, stmt); //new
                lastIns = ins.index + ins.length; //new
                lastInsPos = ins.position + 1; //new
            }
        } else if (ins.isInvokeIns() && jos.empty()) {
            context.addCode(lastIns, ins.index + (ins.length - 1), lastInsPos, ins.position); //new

            lastIns = ins.index + ins.length; //new
            lastInsPos = ins.position + 1; //new
        } else if (ins.opcode == OPCODE_GOTO) {
            context.processBreakContinue(ins.index, ins.getTargetPc());
            //lastIns = ins.index+ins.length; //new
            //lastInsPos = ins.position+1; //new
        } else if (ins.opcode == OPCODE_GOTOW) {
            context.processBreakContinue(ins.index, ins.getTargetPcW());
            lastIns = ins.index + ins.length; //new
            lastInsPos = ins.position + 1; //new
        }

    }

    /**
     * @return Returns get bytecode offset of current stmt
     */
    public int getLastIns() {
        return lastIns;
    }

    /**
     * set bytecode offset of current stmt
     *
     * @param li Last Instruction Offset
     */
    public void setLastIns(int li) {
        lastIns = li;
    }

    /**
     * @return Returns index offset into bytecode array of current stmt.
     */
    public int getLastInsPos() {
        return lastInsPos;
    }

    /**
     * set index offset into bytecode array of current stmt.
     *
     * @param lip Last Instruction Position.
     */
    public void setLastInsPos(int lip) {
        lastInsPos = lip;
    }
}
