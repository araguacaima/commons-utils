/*
 * @(#)JClassInfo.java
 *
 * JReversePro - Java Decompiler / Disassembler.
 * Copyright (C) 2000 Karthik Kumar.
 * EMail: akkumar@users.sourceforge.net
 *
 * This program is free software; you can redistribute it and/or modify
 * it , under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.If not, write to
 *  The Free Software Foundation, Inc.,
 *  59 Temple Place - Suite 330,
 *  Boston, MA 02111-1307, USA.
 */
package jreversepro.reflect;

import jreversepro.common.AppConstants;
import jreversepro.common.Helper;
import jreversepro.common.KeyWords;
import jreversepro.revengine.JDecompiler;
import jreversepro.revengine.JDisAssembler;
import jreversepro.revengine.JReverseEngineer;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>JClassInfo</b> is the abstract representation of the Class File.
 * The names of the methods are self explanatory.
 *
 * @author Karthik Kumar
 */
public class JClassInfo implements KeyWords {

    /**
     * ACC_INTERFACE bit required to be set if it is an
     * interface and not a class.
     */
    public static final int ACC_INTERFACE = 0x0200;
    /**
     * ACC_SUPER bit required to be set on all
     * modern classes.
     */
    public static final int ACC_SUPER = 0x0020;

    //Generic Info about a class File.
    /**
     * List of interfaces present in the class.
     * All the members in the list are String.
     * For example if the class implements
     * java.awt.event.ActionListener then the list would
     * contain java/awt/event/ActionListener as its member.
     * The class file name would be in the JVM format as mentioned
     * above.
     */
    private final List interfaces;
    /**
     * List of fields present in the class.
     * All the members in the list are JField.
     */
    private final List memFields;
    /**
     * List of methods present in the class.
     * All the members in the list are JMethod.
     */
    private final List memMethods;
    /**
     * Absolute path where the class' source file was located.
     */
    private String absPath;
    /**
     * An integer referring to the access permission of the
     * class.
     * Like if a class is public static void main ()
     * then the accessflag would have appropriate bits
     * set to say if it public static.
     */
    private int accessFlag;
    /**
     * ConstantPool information contained in the class.
     */
    private JConstantPool cpInfo;
    /**
     * Major number of the JVM version that this class file
     * is compiled for.
     */
    private short majorNumber;
    /**
     * Minor number of the JVM version that this class file
     * is compiled for.
     */
    private short minorNumber;
    /**
     * Name of the source file in which this class files' code
     * is present.
     */
    private String srcFile;
    /**
     * Name of the current class' superclass in the JVM format.
     * That is, if the class is String then the name would be
     * java/lang/String.
     */
    private String superClass;
    /**
     * Name of the current class in the JVM format.
     * That is, if the class is String then the name would be
     * java/lang/String.
     */
    private String thisClass;

    /**
     * Empty constructor
     */
    public JClassInfo() {
        memFields = new ArrayList();
        memMethods = new ArrayList();
        interfaces = new ArrayList();
        cpInfo = new JConstantPool(2);

        /*
      TRUE if the class was decompiled.
      False if disasembled.
     */
        boolean decompiled = false;
    }

    /**
     * Adds a new field present in the class.
     *
     * @param rhsField contains the field-related information.
     */
    public void addField(JField rhsField) {
        memFields.add(rhsField);
    }

    /**
     * Adds a new interface that is implemented by this class.
     *
     * @param interfaceName Name of the interface.
     */
    public void addInterface(String interfaceName) {
        interfaces.add(interfaceName);
    }

    /**
     * Adds a new method present in the class.
     *
     * @param rhsMethod contains the method-related information.
     */
    public void addMethod(JMethod rhsMethod) {
        memMethods.add(rhsMethod);
    }

    /**
     * Returns the fields present in the class.
     *
     * @return Returns a List of JField
     */
    public List getFields() {
        return memFields;
    }

    /**
     * Returns the List of interfaces of the current class.
     *
     * @return interfaces of the current class.
     */
    public List getInterfaces() {
        return interfaces;
    }

    /**
     * Returns the major number of the JVM.
     *
     * @return JVM
     */
    public int getMajor() {
        return majorNumber;
    }

    /**
     * Returns the minor number of the JVM.
     *
     * @return JVM minor version
     */
    public int getMinor() {
        return minorNumber;
    }

    /**
     * Returns the path name of this class.
     *
     * @return Absolute path of this class.
     */
    public String getPathName() {
        return absPath;
    }

    /**
     * Sets the pathname of this class.
     *
     * @param classPath Path to this class.
     */
    public void setPathName(String classPath) {
        absPath = classPath;
    }

    /**
     * Returns the source file of the current class.
     *
     * @return source file of the current class.
     */
    public String getSourceFile() {
        return srcFile;
    }

    /**
     * Sets the name of the source file to which this
     * was contained in.
     *
     * @param rhsSrcFile Name of the source file.
     */
    public void setSourceFile(String rhsSrcFile) {
        srcFile = rhsSrcFile;
    }

    /**
     * Returns the stringified disassembled/decompiled class.
     *
     * @param getBytecode If TRUE, returns the disassembled code
     *                    IF the class has already been disassembled. If FALSE,
     *                    returns the decompiled code IF the class has been
     *                    decompiled. Otherwise, returns null;
     * @return Stringified class
     */
    public String getStringifiedClass(boolean getBytecode) {
        return getStringifiedClass(getBytecode, false);
    }

    /**
     * Returns the stringified disassembled/decompiled class, optionally with
     * metadata.
     *
     * @param getBytecode     If TRUE, returns the disassembled code
     *                        IF the class has already been disassembled. If FALSE,
     *                        returns the decompiled code IF the class has been
     *                        decompiled. Otherwise, returns null;
     * @param includeMetadata TRUE if method stack and exception data should be output.
     * @return Stringified class
     */
    public String getStringifiedClass(boolean getBytecode, boolean includeMetadata) {

        return String.valueOf(getHeaders()) + getPackageImports() + getThisSuperClasses() + getStringifiedInterfaces
                () + "{" + getStringifiedFields() + getStringifiedMethods(
                getBytecode,
                includeMetadata) + "\n}";
    }

    /**
     * @return Returns a StringBuffer containing the headers for the reverse
     * engineered code.
     */
    private StringBuffer getHeaders() {
        StringBuffer init = new StringBuffer();
        init.append("// Decompiled by JReversePro " + AppConstants.VERSION);
        init.append("\n// Home : http://jrevpro.sourceforge.net ");
        init.append("\n// JVM VERSiON: ").append(majorNumber).append(".").append(minorNumber);
        init.append("\n// SOURCEFILE: ").append(srcFile);
        return init;
    }

    /**
     * @return Returns a StringBuffer containing the package and import
     * information of the .class file.
     */
    private StringBuffer getPackageImports() {
        StringBuffer result = new StringBuffer();
        String packageName = Helper.getPackageName(thisClass);

        if (packageName.length() != 0) {
            result.append("\npackage ").append(packageName).append(";");
        }

        result.append("\n\n").append(cpInfo.getImportedClasses().getImportClasses(packageName));
        return result;
    }

    /**
     * @return Returns a StringBuffer containing the current class name
     * and the super class name.
     */
    private StringBuffer getThisSuperClasses() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n\n").append(getAccessString()).append(" ");

        sb.append(JImport.
                getClassName(thisClass));

        if (!superClass.equals(LANG_OBJECT)) {
            sb.append(" extends ");
            sb.append(JImport.
                    getClassName(superClass)).append("   ");
        }
        return sb;
    }

    /**
     * @return Returns a StringBuffer containing the information
     * of the interfaces implemented by the class.
     */
    private StringBuffer getStringifiedInterfaces() {
        StringBuffer sb = new StringBuffer();
        if (interfaces.size() != 0) {
            sb.append("\n\t\timplements ");
            for (int i = 0; i < interfaces.size(); i++) {
                if (i != 0) {
                    sb.append(" ,");
                }
                sb.append(JImport.
                        getClassName((String) interfaces.get(i)));
            }
        }
        return sb;
    }

    /**
     * @return Returns a StringBuffer containing the information
     * of fields present in this class.
     */
    private StringBuffer getStringifiedFields() {
        StringBuffer sb = new StringBuffer("\n");
        for (Object memField : memFields) {
            JField field = (JField) memField;
            String datatype = JImport.
                    getClassName(Helper.getJavaDataType(field.getDatatype(), false));

            String access = field.getQualifierName();

            sb.append("\n\t").append(access);
            sb.append(datatype);
            sb.append(" ").append(field.getName());
            String val = field.getValue();
            if (field.isFinal() && val.length() != 0) {
                sb.append(" = ").append(val);
            }
            sb.append(";");
        }
        return sb;
    }

    /**
     * Returns the stringified disassembled/decompiled method.
     *
     * @param getBytecode     If TRUE, returns the disassembled code
     *                        IF the method has already been disassembled. If FALSE,
     *                        returns the decompiled code IF the method has been
     *                        decompiled. Otherwise, returns null;
     * @param includeMetadata TRUE if method stack and exception data should be output
     * @return Stringified methods in this class
     */
    public String getStringifiedMethods(boolean getBytecode, boolean includeMetadata) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.getMethods().size(); i++) {
            JMethod method = (JMethod) this.getMethods().get(i);
            sb.append(method.getMethodAsString(getBytecode, includeMetadata));
        }
        return sb.toString();
    }

    /**
     * Returns the access string of this class.
     *
     * @return Returns the access string of this class.
     */
    public String getAccessString() {
        StringBuilder accString = new StringBuilder();
        accString.append(JMember.getStringRep(accessFlag, false));

        if (isClass()) {
            accString.append(CLASS);
        } else {
            accString.append(INTERFACE);
        }
        return accString.toString();
    }

    /**
     * Returns the methods of this class.
     *
     * @return Returns a list of JMethods
     */
    public List getMethods() {
        return memMethods;
    }

    /**
     * Returns if this is a class or an interface
     *
     * @return Returns true if this is a class,
     * false, if this is an interface.
     */
    public boolean isClass() {
        return ((accessFlag & ACC_INTERFACE) == 0);
    }

    /**
     * Returns the class name of this class' super class.
     *
     * @return name of the current class' super-class.
     */
    public String getSuperClass() {
        return superClass;
    }

    /**
     * Sets the name of the current class' superclass.
     *
     * @param rhsName Name of this class; superclass.
     */
    public void setSuperClass(String rhsName) {
        superClass = rhsName;
    }

    /**
     * @param fullyQualified Parameter to indicate if to return
     *                       the fully qualified name.
     *                       Yes - Fully qualified name along with the package name.
     *                       No - Just the class name only.
     * @return Returns Thisclass name only.
     */
    public String getThisClass(boolean fullyQualified) {
        if (fullyQualified) {
            return thisClass;
        } else {
            int lastIndex = thisClass.lastIndexOf('/');
            if (lastIndex != -1) {
                return thisClass.substring(lastIndex + 1);
            } else {
                return thisClass;
            }
        }
    }

    /**
     * Returns the class name of this class.
     *
     * @return name of the current class.
     */
    public String getThisClass() {
        return thisClass;
    }

    /**
     * Sets the name of the current class.
     *
     * @param rhsName Name of this class.
     */
    public void setThisClass(String rhsName) {
        thisClass = rhsName;
    }

    /**
     * Reverse Engineer the Class file.
     *
     * @param getBytecode True disassembler, false - decompile.
     */
    public void reverseEngineer(boolean getBytecode) {
        //Reverse Engineer here
        processMethods(getBytecode);
    }

    /**
     * Process the methods.
     *
     * @param getBytecode TRUE - disassemble.
     *                    FALSE - disassemble.
     */
    public void processMethods(boolean getBytecode) {

        for (int i = 0; i < this.getMethods().size(); i++) {

            JMethod method = (JMethod) this.getMethods().get(i);
            JReverseEngineer jre;

            jre = getBytecode ? new JDisAssembler(method, this.getConstantPool()) : new JDecompiler(method,
                    this.getConstantPool());
            try {
                jre.genCode();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Returns the constantpool reference
     *
     * @return Returns the ConstantPool reference.
     */
    public JConstantPool getConstantPool() {
        return this.cpInfo;
    }

    /**
     * Sets the ConstantPool information of this class.
     *
     * @param cpInfo contains the constant pool information
     *               of this class.
     */
    public void setConstantPool(JConstantPool cpInfo) {
        this.cpInfo = cpInfo;
    }

    /**
     * Sets the access flag of the class.
     *
     * @param rhsAccess Access flag of the class.
     */
    public void setAccess(int rhsAccess) {
        accessFlag = rhsAccess;
    }

    /**
     * Sets the major and minor number of the JVM
     * for which this class file is compiled for.
     *
     * @param rhsMajor Major number
     * @param rhsMinor Minor number
     */
    public void setMajorMinor(short rhsMajor, short rhsMinor) {
        majorNumber = rhsMajor;
        minorNumber = rhsMinor;
    }

    /**
     * Sets the package to which this class belongs to.
     *
     * @param packageName name of the package to be set.
     */
    public void setPackageName(String packageName) {
        /*
      Name of the package of the current class in the JVM format.
      That is the fully qualified name of the class is
      java.lang.String. then the package name would contain
      java/lang.
     */
        String packageName1 = packageName;
    }
}
