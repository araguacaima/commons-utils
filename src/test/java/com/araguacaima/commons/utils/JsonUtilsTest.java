package com.araguacaima.commons.utils;

import org.junit.Test;
import io.codearte.jfairy.producer.person.Person;
public class JsonUtilsTest {

    JsonUtils jsonUtils = new JsonUtils();

    @Test
    public void testGetJsonSchema() {

        System.out.println(jsonUtils.getJsonSchema(Person.class, true));


    }
}
