package com.araguacaima.commons.utils;

import com.araguacaima.commons.utils.jsonschema.RuleFactory;
import io.codearte.jfairy.producer.person.Person;
import org.jsonschema2pojo.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.araguacaima.commons.utils.JsonSchemaUtils.DEFINITIONS_ROOT;

public class JsonUtilsTest {

    JsonUtils jsonUtils = new JsonUtils();
    private File sourceClassesDir;
    private static final NoopAnnotator noopAnnotator = new NoopAnnotator();
    private static final SchemaStore schemaStore = new SchemaStore();
    private static final org.jsonschema2pojo.SchemaGenerator schemaGenerator = new org.jsonschema2pojo.SchemaGenerator();
    private static final GenerationConfig config = new DefaultGenerationConfig() {

        @Override
        public boolean isUsePrimitives() {
            return true;
        }

        @Override
        public boolean isUseLongIntegers() {
            return true;
        }

        @Override
        public AnnotationStyle getAnnotationStyle() {
            return AnnotationStyle.NONE;
        }

        @Override
        public InclusionLevel getInclusionLevel() {
            return InclusionLevel.ALWAYS;
        }

        @Override
        public boolean isUseOptionalForGetters() {
            return false;
        }

        @Override
        public boolean isRemoveOldOutput() {
            return true;
        }

        @Override
        public boolean isSerializable() {
            return true;
        }

        @Override
        public boolean isIncludeConstructors() {
            return true;
        }

        @Override
        public boolean isIncludeAdditionalProperties() {
            return false;
        }

        @Override
        public String getTargetVersion() {
            return "1.8";
        }

        @Override
        public Language getTargetLanguage() {
            return Language.JAVA;
        }

    };


    @Before
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
        RuleFactory ruleFactory = new RuleFactory(config, noopAnnotator, schemaStore, DEFINITIONS_ROOT, null);
        jsonUtils.jsonToSourceClassFile(json, StringUtils.capitalize(className), packageName, sourceClassesDir, ruleFactory, schemaGenerator);
    }
}
