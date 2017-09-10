package jreversepro.tester;

import java.io.File;
import java.io.IOException;

import jreversepro.tester.tests.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple test runner.
 */
public class TestMain {

    private static Logger log = LoggerFactory.getLogger(TestMain.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            log.info("ARGS: [jrevpro root dir]");
            return;
        }

        File rootdir = new File(args[0]);
        File classdir = new File(rootdir, "testclasses");
        File compiledir = new File(rootdir, "testrun" + System.currentTimeMillis());

        if (!rootdir.exists()) {
            log.info("root dir " + rootdir + " does not exist");
            return;
        }

        if (!classdir.exists()) {
            log.info("class dir " + classdir + " does not exist");
            return;
        }

        if (compiledir.exists()) {
            log.info("compile directory " + compiledir + " already exists - exiting");
            return;
        }

        compiledir.mkdir();

        TestContext context = null;

        try {
            String classdirname = classdir.getCanonicalPath();
            String compiledirname = compiledir.getCanonicalPath();

            context = new TestContext(classdirname, compiledirname);
        } catch (IOException e) {
            log.info("-- FAILED: " + e.getMessage());
        }

        runTest(context, TestSimpleReturns.class);
    }

    public static void runTest(TestContext context, Class tester) {
        log.info("TEST CASE " + tester.getName());

        TestBase testcase;

        try {
            testcase = (TestBase) tester.newInstance();
        } catch (Exception e) {
            log.info("-- FAILED: " + e.getMessage());
            return;
        }

        try {
            testcase.setContext(context);
            testcase.init();

            log.info("running against original class");
            testcase.test(testcase.getOriginalClass());
            log.info("running against decompiled class");
            testcase.test(testcase.getNewClass());
        } catch (Exception e) {
            log.info("-- FAILED: " + e.getMessage());
        } finally {
            testcase.teardown();
        }

        log.info("--DONE");

    }
}
