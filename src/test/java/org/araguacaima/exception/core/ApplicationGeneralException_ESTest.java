/*
 * This file was automatically generated by EvoSuite
 * Tue Sep 05 19:28:25 GMT 2017
 */

package org.araguacaima.exception.core;

import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(EvoRunner.class)
@EvoRunnerParameters(mockJVMNonDeterminism = true,
                     useVFS = true,
                     useVNET = true,
                     resetStaticState = true,
                     separateClassLoader = true,
                     useJEE = true)
public class ApplicationGeneralException_ESTest extends ApplicationGeneralException_ESTest_scaffolding {

    @Test(timeout = 4000)
    public void test0()
            throws Throwable {
        ApplicationGeneralExceptionImpl applicationGeneralExceptionImpl0 = new ApplicationGeneralExceptionImpl("");
        applicationGeneralExceptionImpl0.environmentThrowableInfo = null;
        EnvironmentThrowableInfo environmentThrowableInfo0 =
                applicationGeneralExceptionImpl0.getEnvironmentThrowableInfo();
        assertNull(environmentThrowableInfo0);
    }

    @Test(timeout = 4000)
    public void test1()
            throws Throwable {
        ApplicationGeneralExceptionImpl applicationGeneralExceptionImpl0 = new ApplicationGeneralExceptionImpl(
                "getLineNumber");
        Date date0 = applicationGeneralExceptionImpl0.getOccurrenceDateAndTime();
        assertNull(date0);
    }

    @Test(timeout = 4000)
    public void test2()
            throws Throwable {
        ApplicationGeneralExceptionImpl applicationGeneralExceptionImpl0 = new ApplicationGeneralExceptionImpl(
                "getLineNumber");
        ApplicationGeneralExceptionImpl applicationGeneralExceptionImpl1 = new ApplicationGeneralExceptionImpl(
                "getClassName",
                applicationGeneralExceptionImpl0);
        assertNull(applicationGeneralExceptionImpl1.getLocalizedMessage());
    }

    @Test(timeout = 4000)
    public void test3()
            throws Throwable {
        ApplicationGeneralExceptionImpl applicationGeneralExceptionImpl0 = new ApplicationGeneralExceptionImpl("");
        EnvironmentThrowableInfo environmentThrowableInfo0 = applicationGeneralExceptionImpl0.getEnvironmentThrowableInfo();
        assertNotNull(environmentThrowableInfo0);
    }
}
