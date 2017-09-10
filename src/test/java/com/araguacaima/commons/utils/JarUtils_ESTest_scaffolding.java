/**
 * Scaffolding file used to store all the setups needed to run
 * tests automatically generated by EvoSuite
 * Fri Sep 08 16:22:58 GMT 2017
 */

package com.araguacaima.commons.utils;

import org.evosuite.runtime.annotation.EvoSuiteClassExclude;
import org.evosuite.runtime.sandbox.Sandbox;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

@EvoSuiteClassExclude
public class JarUtils_ESTest_scaffolding {

    private static final java.util.Properties defaultProperties = (java.util.Properties) java.lang.System
            .getProperties().clone();
    @org.junit.Rule
    public org.evosuite.runtime.vnet.NonFunctionalRequirementRule nfr = new org.evosuite.runtime.vnet
            .NonFunctionalRequirementRule();
    private org.evosuite.runtime.thread.ThreadStopper threadStopper = new org.evosuite.runtime.thread.ThreadStopper
            (org.evosuite.runtime.thread.KillSwitchHandler.getInstance(),
            3000);

    @AfterClass
    public static void clearEvoSuiteFramework() {
        Sandbox.resetDefaultSecurityManager();
        java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
    }

    @BeforeClass
    public static void initEvoSuiteFramework() {
        org.evosuite.runtime.RuntimeSettings.className = "com.araguacaima.commons.utils.JarUtils";
        org.evosuite.runtime.GuiSupport.initialize();
        org.evosuite.runtime.RuntimeSettings.maxNumberOfThreads = 100;
        org.evosuite.runtime.RuntimeSettings.maxNumberOfIterationsPerLoop = 10000;
        org.evosuite.runtime.RuntimeSettings.mockSystemIn = true;
        org.evosuite.runtime.RuntimeSettings.sandboxMode = org.evosuite.runtime.sandbox.Sandbox.SandboxMode.RECOMMENDED;
        org.evosuite.runtime.sandbox.Sandbox.initializeSecurityManagerForSUT();
        org.evosuite.runtime.classhandling.JDKClassResetter.init();
        setSystemProperties();
        initializeClasses();
        org.evosuite.runtime.Runtime.getInstance().resetRuntime();
    }

    public static void setSystemProperties() {

        java.lang.System.setProperties((java.util.Properties) defaultProperties.clone());
        java.lang.System.setProperty("file.encoding", "Cp1252");
        java.lang.System.setProperty("java.awt.headless", "true");
        java.lang.System.setProperty("user.country", "MX");
        java.lang.System.setProperty("user.language", "es");
        java.lang.System.setProperty("user.timezone", "America/Mexico_City");
    }

    private static void initializeClasses() {
        org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(JarUtils_ESTest_scaffolding.class
                        .getClassLoader(),
                "com.araguacaima.commons.utils.JarUtils");
    }

    @After
    public void doneWithTestCase() {
        threadStopper.killAndJoinClientThreads();
        org.evosuite.runtime.jvm.ShutdownHookHandler.getInstance().safeExecuteAddedHooks();
        org.evosuite.runtime.classhandling.JDKClassResetter.reset();
        resetClasses();
        org.evosuite.runtime.sandbox.Sandbox.doneWithExecutingSUTCode();
        org.evosuite.runtime.agent.InstrumentingAgent.deactivate();
        org.evosuite.runtime.GuiSupport.restoreHeadlessMode();
    }

    private static void resetClasses() {
        org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(JarUtils_ESTest_scaffolding.class.getClassLoader());

        org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses("com.araguacaima.commons.utils.JarUtils");
    }

    @Before
    public void initTestCase() {
        threadStopper.storeCurrentThreads();
        threadStopper.startRecordingTime();
        org.evosuite.runtime.jvm.ShutdownHookHandler.getInstance().initHandler();
        org.evosuite.runtime.sandbox.Sandbox.goingToExecuteSUTCode();
        setSystemProperties();
        org.evosuite.runtime.GuiSupport.setHeadless();
        org.evosuite.runtime.Runtime.getInstance().resetRuntime();
        org.evosuite.runtime.agent.InstrumentingAgent.activate();
    }
}
