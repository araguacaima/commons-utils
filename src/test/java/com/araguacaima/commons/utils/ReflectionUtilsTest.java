package com.araguacaima.commons.utils;

import org.junit.Assert;
import org.junit.Test;

public class ReflectionUtilsTest {

    private A object;
    private ReflectionUtils reflectionUtils = ReflectionUtils.getInstance();

    public ReflectionUtilsTest() {
        object = new A();
        object.setField1(0);
        B object2 = new B();
        object2.setType("Test");
        object.setField2(object2);
    }

    @Test
    public void testExtractByType() {
        Assert.assertEquals(reflectionUtils.extractByType(object, B.class).size(), 1);
    }

    public class A {
        private Integer field1;
        private B field2;

        public A() {
        }

        public Integer getField1() {
            return field1;
        }

        public void setField1(Integer field1) {
            this.field1 = field1;
        }

        public B getField2() {
            return field2;
        }

        public void setField2(B field2) {
            this.field2 = field2;
        }
    }

    public class B {
        private String type;

        public B() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
