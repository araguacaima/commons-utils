package com.araguacaima.commons.utils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JsonSchemaUtilsTest {

    private JsonSchemaUtils<ClassLoader> jsonSchemaUtils = new JsonSchemaUtils<>(JsonSchemaUtilsTest.class.getClassLoader());
    private String schema;
    private File sourceClassesDir;
    private File compiledClassesDir;
    private String schemas;

    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    @Before
    public void init() throws IOException {
        JsonSchemaUtils<ClassLoader> jsonSchemaUtils = new JsonSchemaUtils<>(JsonSchemaUtilsTest.class.getClassLoader());
        URL resourceSchema = JsonSchemaUtilsTest.class.getClassLoader().getResource("forms-json-schemas.json");
        schema = FileUtils.readFileToString(new File(resourceSchema.getFile()), StandardCharsets.UTF_8);
        URL resourceSchemas = JsonSchemaUtilsTest.class.getClassLoader().getResource("forms-json-schemas.json");
        schemas = FileUtils.readFileToString(new File(resourceSchemas.getFile()), StandardCharsets.UTF_8);
        File file = new File(System.getProperty("java.io.tmpdir"), "jsonSchemaUtils");
        if (!file.exists()) {
            file = FileUtils.createTempDir("jsonSchemaUtils");
        }
        sourceClassesDir = new File(file, "sources");
        sourceClassesDir.mkdirs();
        compiledClassesDir = new File(file, "compiled");
        compiledClassesDir.mkdirs();
    }

    @Test
    public void testProcessSingleSchema() throws URISyntaxException, NoSuchFieldException, IllegalAccessException, IOException, InstantiationException {
        jsonSchemaUtils.processFile_(schema, "test", sourceClassesDir, compiledClassesDir);
    }

    @Test
    public void testProcessMultipleSchemas() throws URISyntaxException, NoSuchFieldException, IllegalAccessException, IOException, InstantiationException {
        jsonSchemaUtils.processFile_(schemas, "test", sourceClassesDir, compiledClassesDir);
    }
}
