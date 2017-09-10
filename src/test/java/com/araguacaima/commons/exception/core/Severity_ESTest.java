/*
 * This file was automatically generated by EvoSuite
 * Fri Sep 08 16:24:26 GMT 2017
 */

package com.araguacaima.commons.exception.core;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static org.evosuite.runtime.EvoAssertions.verifyException;
import static org.junit.Assert.*;

@RunWith(EvoRunner.class)
@EvoRunnerParameters(mockJVMNonDeterminism = true,
                     useVFS = true,
                     useVNET = true,
                     resetStaticState = true,
                     separateClassLoader = true,
                     useJEE = true)
public class Severity_ESTest extends Severity_ESTest_scaffolding {

    @Test
    public void test00()
            throws Throwable {
        Severity severity0 = Severity.DEBUG;
        Severity severity1 = Severity.WARNING;
        boolean boolean0 = severity0.greaterThan(severity1);
        assertEquals(5, severity0.getIntLevel());
        assertFalse(boolean0);
        assertEquals(3, severity1.getIntLevel());
    }

    @Test
    public void test01()
            throws Throwable {
        Severity severity0 = Severity.WARNING;
        Severity severity1 = Severity.INFO;
        boolean boolean0 = severity0.equals(severity1);
        assertFalse(severity1.equals((Object) severity0));
        assertFalse(boolean0);
    }

    @Test
    public void test02()
            throws Throwable {
        Severity severity0 = Severity.FATAL;
        Severity severity1 = Severity.DEBUG;
        int int0 = severity0.compareTo(severity1);
        assertEquals(4, int0);
        assertEquals(5, severity1.getIntLevel());
    }

    @Test
    public void test03()
            throws Throwable {
        Severity severity0 = Severity.DEBUG;
        Severity severity1 = Severity.INFO;
        int int0 = severity0.compareTo(severity1);
        assertEquals((-1), int0);
        assertEquals(4, severity1.getIntLevel());
    }

    @Test
    public void test04()
            throws Throwable {
        Severity severity0 = Severity.FATAL;
        // Undeclared exception!
        try {
            severity0.lessThan((Severity) null);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("com.araguacaima.commons.exception.core.Severity", e);
        }
    }

    @Test
    public void test05()
            throws Throwable {
        Severity severity0 = Severity.FATAL;
        // Undeclared exception!
        try {
            severity0.greaterThan((Severity) null);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("com.araguacaima.commons.exception.core.Severity", e);
        }
    }

    @Test
    public void test06()
            throws Throwable {
        // Undeclared exception!
        try {
            Severity.getSeverity((String) null);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("com.araguacaima.commons.exception.core.Severity", e);
        }
    }

    @Test
    public void test07()
            throws Throwable {
        Severity severity0 = Severity.WARNING;
        // Undeclared exception!
        try {
            severity0.compareTo((Object) null);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
            verifyException("com.araguacaima.commons.exception.core.Severity", e);
        }
    }

    @Test
    public void test08()
            throws Throwable {
        Severity severity0 = Severity.INFO;
        Collection collection0 = Severity.getSeverities();
        // Undeclared exception!
        try {
            severity0.compareTo(collection0);
            fail("Expecting exception: ClassCastException");

        } catch (ClassCastException e) {
            //
            // java.util.HashMap$EntrySet cannot be cast to com.araguacaima.commons.exception.core.Severity
            //
            verifyException("com.araguacaima.commons.exception.core.Severity", e);
        }
    }

    @Test
    public void test09()
            throws Throwable {
        Severity severity0 = Severity.INFO;
        severity0.hashCode();
        assertEquals(4, severity0.getIntLevel());
    }

    @Test
    public void test10()
            throws Throwable {
        Severity severity0 = Severity.DEBUG;
        int int0 = severity0.getIntLevel();
        assertEquals(5, int0);
    }

    @Test
    public void test11()
            throws Throwable {
        Severity severity0 = Severity.DEBUG;
        String string0 = severity0.getName();
        assertEquals("DEBUG", string0);
    }

    @Test
    public void test12()
            throws Throwable {
        Severity severity0 = Severity.FATAL;
        Severity severity1 = Severity.DEBUG;
        boolean boolean0 = severity1.lessThan(severity0);
        assertEquals(5, severity1.getIntLevel());
        assertTrue(boolean0);
    }

    @Test
    public void test13()
            throws Throwable {
        Severity severity0 = Severity.INFO;
        boolean boolean0 = severity0.lessThan(severity0);
        assertFalse(boolean0);
        assertEquals(4, severity0.getIntLevel());
    }

    @Test
    public void test14()
            throws Throwable {
        Severity severity0 = Severity.FATAL;
        Severity severity1 = Severity.DEBUG;
        boolean boolean0 = severity0.greaterThan(severity1);
        assertTrue(boolean0);
        assertEquals(5, severity1.getIntLevel());
    }

    @Test
    public void test15()
            throws Throwable {
        Severity severity0 = Severity.DEBUG;
        boolean boolean0 = severity0.greaterThan(severity0);
        assertFalse(boolean0);
        assertEquals(5, severity0.getIntLevel());
    }

    @Test
    public void test16()
            throws Throwable {
        Severity severity0 = Severity.WARNING;
        Collection collection0 = Severity.getSeverities();
        boolean boolean0 = severity0.equals(collection0);
        assertFalse(boolean0);
    }

    @Test
    public void test17()
            throws Throwable {
        Severity severity0 = Severity.FATAL;
        Severity severity1 = Severity.DEBUG;
        boolean boolean0 = severity0.equals(severity1);
        assertFalse(severity1.equals((Object) severity0));
        assertFalse(boolean0);
    }

    @Test
    public void test18()
            throws Throwable {
        Severity severity0 = Severity.INFO;
        boolean boolean0 = severity0.equals(severity0);
        assertTrue(boolean0);
    }

    @Test
    public void test19()
            throws Throwable {
        Severity severity0 = Severity.FATAL;
        boolean boolean0 = severity0.equals((Object) null);
        assertFalse(boolean0);
    }

    @Test
    public void test20()
            throws Throwable {
        Severity.getSeverities();
        Severity severity0 = Severity.getSeverity("ERROR");
        assertEquals("ERROR", severity0.getName());
    }

    @Test
    public void test21()
            throws Throwable {
        Severity severity0 = Severity.INFO;
        String string0 = severity0.toString();
        assertEquals("INFO", string0);
    }

    @Test
    public void test22()
            throws Throwable {
        Severity severity0 = Severity.FATAL;
        int int0 = severity0.compareTo(severity0);
        assertEquals(0, int0);
        assertEquals(1, severity0.getIntLevel());
    }

    @Test
    public void test23()
            throws Throwable {
        Severity.getSeverity("!~E%4");
        Collection collection0 = Severity.getSeverities();
        assertNotNull(collection0);
    }
}