package org.araguacaima.commons.exception.core;

import org.araguacaima.commons.utils.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * The internal representation of caller environment information.
 */
@SuppressWarnings("CanBeFinal")
public class EnvironmentThrowableInfo implements java.io.Serializable {

    /**
     * When location information is not available the constant
     * <code>NA</code> is returned. Current value of this string
     * constant is <b>?</b>.
     */
    public static final String NA = "?";
    private static final ThrowableStackTraceRenderer RENDERER = new ThrowableStackTraceRenderer();
    private static final Logger log = LoggerFactory.getLogger(EnvironmentThrowableInfo.class);
    private static final long serialVersionUID = -1325822038990805636L;
    private static final StringWriter sw = new StringWriter();
    private static final PrintWriter pw = new PrintWriter(sw);
    private static Method getClassNameMethod;
    private static Method getFileNameMethod;
    private static Method getLineNumberMethod;
    private static Method getMethodNameMethod;
    private static Method getStackTraceMethod;

    static {
        try {
            getStackTraceMethod = Throwable.class.getMethod("getStackTrace");
            Class<?> stackTraceElementClass = Class.forName("java.lang.StackTraceElement");
            getClassNameMethod = stackTraceElementClass.getMethod("getClassName");
            getMethodNameMethod = stackTraceElementClass.getMethod("getMethodName");
            getFileNameMethod = stackTraceElementClass.getMethod("getFileName");
            getLineNumberMethod = stackTraceElementClass.getMethod("getLineNumber");
        } catch (ClassNotFoundException | NoSuchMethodException ex) {
            log.debug("EnvironmentThrowableInfo will use pre-JDK 1.4 methods to determine location.");
        }
    }

    /**
     * All available caller information, in the format
     * <code>fully.qualified.classname.of.caller.methodName(Filename.java:line)</code>
     */
    public String fullInfo;
    /**
     * Caller's fully qualified class name.
     */
    transient String className;
    /**
     * Caller's file name.
     */
    transient String fileName;
    /**
     * Caller's line number.
     */
    transient String lineNumber;
    /**
     * Caller's method name.
     */
    transient String methodName;
    /**
     * Caller's stacktrace
     */
    transient Collection stackTrace;

    /**
     * Instantiate location information based on a Throwable. We
     * expect the Throwable
     *
     * @param t Throwable used to determine location, may be null.
     *          class.
     */
    public EnvironmentThrowableInfo(Throwable t) {
        this(t, t.getClass().getName());
    }

    /**
     * Instantiate location information based on a Throwable. We
     * expect the Throwable
     *
     * @param t            Throwable used to determine location, may be null.
     * @param callingClass class name of caller.  EnvironmentThrowableInfo will be site that calls a method on this
     *                     class.
     */
    public EnvironmentThrowableInfo(Throwable t, String callingClass) {
        if (t == null || callingClass == null) {
            return;
        }
        stackTrace = ThrowableStackTraceRenderer.render(t);
        if (getLineNumberMethod != null) {
            try {
                Object[] elements = (Object[]) getStackTraceMethod.invoke(t);
                String prevClass = NA;
                for (int i = elements.length - 1; i >= 0; i--) {
                    String thisClass = (String) getClassNameMethod.invoke(elements[i]);
                    if (callingClass.equals(thisClass)) {
                        int caller = i + 1;
                        if (caller < elements.length) {
                            className = prevClass;
                            methodName = (String) getMethodNameMethod.invoke(elements[caller]);
                            fileName = (String) getFileNameMethod.invoke(elements[caller]);
                            if (fileName == null) {
                                fileName = NA;
                            }
                            int line = (Integer) getLineNumberMethod.invoke(elements[caller]);
                            if (line < 0) {
                                lineNumber = NA;
                            } else {
                                lineNumber = String.valueOf(line);
                            }
                            this.fullInfo = className + "." + methodName + "(" + fileName + ":" + lineNumber + ")";
                        }
                        return;
                    }
                    prevClass = thisClass;
                }
                return;
            } catch (IllegalAccessException | RuntimeException ex) {
                log.debug("EnvironmentThrowableInfo failed using JDK 1.4 methods", ex);
            } catch (InvocationTargetException ex) {
                if (ex.getTargetException() instanceof InterruptedException || ex.getTargetException() instanceof
                        InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }
                log.debug("EnvironmentThrowableInfo failed using JDK 1.4 methods", ex);
            }
        }

        String s;
        // Protect against multiple access to sw.
        synchronized (sw) {
            t.printStackTrace(pw);
            s = sw.toString();
            sw.getBuffer().setLength(0);
        }
        int ibegin, iend;

        // This method of searching may not be fastest but it's safer
        // than counting the stack depth which is not guaranteed to be
        // constant across JVM implementations.
        ibegin = s.lastIndexOf(callingClass);
        if (ibegin == -1) {
            return;
        }

        //
        //   if the next character after the class name exists
        //       but is not a period, see if the classname is
        //       followed by a period earlier in the trace.
        //       Minimizes mistakeningly matching on a class whose
        //       name is a substring of the desired class.

        if (ibegin + callingClass.length() < s.length() && s.charAt(ibegin + callingClass.length()) != '.') {
            int i = s.lastIndexOf(callingClass + ".");
            if (i != -1) {
                ibegin = i;
            }
        }

        ibegin = s.indexOf(SystemInfo.get("line.separator"), ibegin);
        if (ibegin == -1) {
            return;
        }
        ibegin += SystemInfo.get("line.separator").length();

        // determine end of line
        iend = s.indexOf(SystemInfo.get("line.separator"), ibegin);
        if (iend == -1) {
            return;
        }

        // VA has a different stack trace format which doesn't
        // need to skip the inital 'at'

        // back up to first blank character
        ibegin = s.lastIndexOf("at ", iend);
        if (ibegin == -1) {
            return;
        }
        // Add 3 to skip "at ";
        ibegin += 3;

        // everything between is the requested stack item
        this.fullInfo = s.substring(ibegin, iend);
    }

    /**
     * Return the fully qualified class name of the caller making the
     * logging request.
     *
     * @return The fully qualified class name of the caller making the
     * logging request.
     */
    public String getClassName() {
        if (fullInfo == null) {
            return NA;
        }
        if (className == null) {
            // Starting the search from '(' is safer because there is
            // potentially a dot between the parentheses.
            int iend = fullInfo.lastIndexOf('(');
            if (iend == -1) {
                className = NA;
            } else {
                iend = fullInfo.lastIndexOf('.', iend);

                int ibegin = 0;

                if (iend == -1) {
                    className = NA;
                } else {
                    className = this.fullInfo.substring(ibegin, iend);
                }
            }
        }
        return className;
    }

    /**
     * Return the file name of the caller.
     *
     * @return The file name of the caller.
     */
    public String getFileName() {
        if (fullInfo == null) {
            return NA;
        }

        if (fileName == null) {
            int iend = fullInfo.lastIndexOf(':');
            if (iend == -1) {
                fileName = NA;
            } else {
                int ibegin = fullInfo.lastIndexOf('(', iend - 1);
                fileName = this.fullInfo.substring(ibegin + 1, iend);
            }
        }
        return fileName;
    }

    /**
     * Returns the line number of the caller.
     *
     * @return The line number of the caller.
     */
    public String getLineNumber() {
        if (fullInfo == null) {
            return NA;
        }

        if (lineNumber == null) {
            int iend = fullInfo.lastIndexOf(')');
            int ibegin = fullInfo.lastIndexOf(':', iend - 1);
            if (ibegin == -1) {
                lineNumber = NA;
            } else {
                lineNumber = this.fullInfo.substring(ibegin + 1, iend);
            }
        }
        return lineNumber;
    }

    /**
     * Returns the method name of the caller.
     *
     * @return The method name of the caller.
     */
    public String getMethodName() {
        if (fullInfo == null) {
            return NA;
        }
        if (methodName == null) {
            int iend = fullInfo.lastIndexOf('(');
            int ibegin = fullInfo.lastIndexOf('.', iend);
            if (ibegin == -1) {
                methodName = NA;
            } else {
                methodName = this.fullInfo.substring(ibegin + 1, iend);
            }
        }
        return methodName;
    }

    public Collection getStackTrace() {
        return stackTrace;
    }

}
