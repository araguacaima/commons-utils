package com.araguacaima.commons.utils;

import io.codearte.jfairy.producer.person.Person;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.araguacaima.commons.utils.JsonSchemaUtils.DEFINITIONS_ROOT;

public class JsonUtilsTest {

    JsonUtils jsonUtils = new JsonUtils();
    private File sourceClassesDir;

    public void init() {
        File file = new File(System.getProperty("java.io.tmpdir"), "jsonSchemaUtils");
        if (!file.exists()) {
            file = FileUtils.createTempDir("jsonSchemaUtils");
        }
        sourceClassesDir = new File(file, "sources");
        sourceClassesDir.mkdirs();
    }

    @Test
    public void testGetJsonSchema() {
        System.out.println(jsonUtils.getJsonSchema(Person.class, true));
    }

    @Test
    public void testJsonToSourceClassFile() throws IOException, NoSuchFieldException, IllegalAccessException {
        init();
        String json = "{" +
                "  \"$schema\": \"http://json-schema.org/draft-07/schema#\"," +
                "  \"id\": \"Example\"," +
                "  \"type\": \"object\"," +
                "  \"properties\": {" +
                "    \"objectType\": {" +
                "      \"$ref\": \"#/definitions/foo/bar/Type1\"" +
                "    }," +
                "    \"arrayType\": {" +
                "      \"type\": \"array\"," +
                "      \"items\": {" +
                "        \"$ref\": \"#/definitions/foo/bar/Type1\"" +
                "      }" +
                "    }" +
                "  }," +
                "  \"$id\": \"foo.bar.Example\"," +
                "  \"definitions\": {" +
                "    \"foo\": {" +
                "      \"bar\": {" +
                "        \"Type1\": {" +
                "          \"$schema\": \"http://json-schema.org/draft-07/schema#\"," +
                "          \"id\": \"Type1\"," +
                "          \"type\": \"object\"," +
                "          \"properties\": {" +
                "            \"typeRef\": {" +
                "              \"$ref\": \"#/definitions/foo/Type2\"" +
                "            }" +
                "          }" +
                "        }" +
                "      }," +
                "      \"Type2\": {" +
                "        \"$schema\": \"http://json-schema.org/draft-07/schema#\"," +
                "        \"id\": \"Type2\"," +
                "        \"type\": \"object\"," +
                "        \"properties\": {" +
                "          \"typeString\": {" +
                "            \"type\": \"string\"" +
                "          }" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}";
        String className = "Example";
        String packageName = "foo.bar";
        jsonUtils.jsonToSourceClassFile(json, StringUtils.capitalize(className), packageName, sourceClassesDir, DEFINITIONS_ROOT, null);
    }
}
