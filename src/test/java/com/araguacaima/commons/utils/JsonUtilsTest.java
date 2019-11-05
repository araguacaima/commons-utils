package com.araguacaima.commons.utils;

import io.codearte.jfairy.producer.person.Person;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.araguacaima.commons.utils.JsonSchemaUtils.DEFINITIONS_ROOT;

public class JsonUtilsTest {

    JsonUtils jsonUtils = new JsonUtils();
    private File sourceClassesDir;

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
        jsonUtils.jsonToSourceClassFile(json, StringUtils.capitalize(className), packageName, sourceClassesDir, DEFINITIONS_ROOT, null);
    }

    @Test
    public void test2() throws IllegalAccessException, NoSuchFieldException, IOException {
        String json = "[\n" +
                "  {\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "      \"calculatedScore\": {\n" +
                "        \"type\": \"number\"\n" +
                "      },\n" +
                "      \"category\": {\n" +
                "        \"type\": \"string\",\n" +
                "        \"enum\": [\n" +
                "          \"CATEGORY_1\", \n" +
                "          \"CATEGORY_2\"\n" +
                "        ],\n" +
                "        \"$id\": \"com.bbva.gsa.forms.model.QuestionCategory\"\n" +
                "      },\n" +
                "      \"description\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"formId\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"id\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"maxScore\": {\n" +
                "        \"type\": \"integer\"\n" +
                "      },\n" +
                "      \"options\": {\n" +
                "        \"type\": \"array\",\n" +
                "        \"items\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"properties\": {\n" +
                "\t\t\t\"id\": {\n" +
                "\t\t\t  \"type\": \"string\"\n" +
                "\t\t\t},\n" +
                "            \"description\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"isText\": {\n" +
                "              \"type\": \"boolean\"\n" +
                "            },\n" +
                "            \"questionId\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"selected\": {\n" +
                "              \"type\": \"boolean\"\n" +
                "            },\n" +
                "            \"title\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"weighting\": {\n" +
                "              \"type\": \"number\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"$id\": \"com.bbva.gsa.forms.model.QuestionOption\"\n" +
                "        },\n" +
                "        \"$id\": \"java.util.Set<com.bbva.gsa.forms.model.QuestionOption>\"\n" +
                "      },\n" +
                "      \"title\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"type\": {\n" +
                "        \"type\": \"string\",\n" +
                "        \"enum\": [\n" +
                "          \"MULTIPLE\",\n" +
                "          \"SINGLE\"\n" +
                "        ],\n" +
                "        \"$id\": \"com.bbva.gsa.forms.model.QuestionType\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"$id\": \"com.bbva.gsa.forms.model.Question\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"type\": \"string\",\n" +
                "    \"enum\": [\n" +
                "      \"ES\",\n" +
                "      \"EN\"\n" +
                "    ],\n" +
                "    \"$id\": \"com.bbva.gsa.forms.model.FormLocale\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"type\": \"string\",\n" +
                "    \"enum\": [\n" +
                "      \"MULTIPLE\",\n" +
                "      \"SINGLE\"\n" +
                "    ],\n" +
                "    \"$id\": \"com.bbva.gsa.forms.model.QuestionType\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "\t  \"id\": {\n" +
                "\t\t\"type\": \"string\"\n" +
                "\t  },\n" +
                "      \"description\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"isText\": {\n" +
                "        \"type\": \"boolean\"\n" +
                "      },\n" +
                "      \"questionId\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"selected\": {\n" +
                "        \"type\": \"boolean\"\n" +
                "      },\n" +
                "      \"title\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"weighting\": {\n" +
                "        \"type\": \"number\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"$id\": \"com.bbva.gsa.forms.model.QuestionOption\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"type\": \"string\",\n" +
                "    \"enum\": [\n" +
                "      \"CATEGORY_1\",\n" +
                "      \"CATEGORY_2\"\n" +
                "    ],\n" +
                "    \"$id\": \"com.bbva.gsa.forms.model.QuestionCategory\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "      \"description\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"id\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"locale\": {\n" +
                "        \"type\": \"string\",\n" +
                "        \"enum\": [\n" +
                "          \"ES\",\n" +
                "          \"EN\"\n" +
                "        ],\n" +
                "        \"$id\": \"com.bbva.gsa.forms.model.FormLocale\"\n" +
                "      },\n" +
                "      \"questions\": {\n" +
                "        \"type\": \"array\",\n" +
                "        \"items\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"properties\": {\n" +
                "            \"calculatedScore\": {\n" +
                "              \"type\": \"number\"\n" +
                "            },\n" +
                "            \"category\": {\n" +
                "              \"type\": \"string\",\n" +
                "              \"enum\": [\n" +
                "                \"CATEGORY_1\",\n" +
                "                \"CATEGORY_2\"\n" +
                "              ],\n" +
                "              \"$id\": \"com.bbva.gsa.forms.model.QuestionCategory\"\n" +
                "            },\n" +
                "            \"description\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"formId\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"id\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"maxScore\": {\n" +
                "              \"type\": \"integer\"\n" +
                "            },\n" +
                "            \"options\": {\n" +
                "              \"type\": \"array\",\n" +
                "              \"items\": {\n" +
                "                \"type\": \"object\",\n" +
                "                \"properties\": {\n" +
                "\t\t\t\t  \"id\": {\n" +
                "\t\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t\t  },\n" +
                "                  \"description\": {\n" +
                "                    \"type\": \"string\"\n" +
                "                  },\n" +
                "                  \"isText\": {\n" +
                "                    \"type\": \"boolean\"\n" +
                "                  },\n" +
                "                  \"questionId\": {\n" +
                "                    \"type\": \"string\"\n" +
                "                  },\n" +
                "                  \"selected\": {\n" +
                "                    \"type\": \"boolean\"\n" +
                "                  },\n" +
                "                  \"title\": {\n" +
                "                    \"type\": \"string\"\n" +
                "                  },\n" +
                "                  \"weighting\": {\n" +
                "                    \"type\": \"number\"\n" +
                "                  }\n" +
                "                },\n" +
                "                \"$id\": \"com.bbva.gsa.forms.model.QuestionOption\"\n" +
                "              },\n" +
                "              \"$id\": \"java.util.Set<com.bbva.gsa.forms.model.QuestionOption>\"\n" +
                "            },\n" +
                "            \"title\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"type\": {\n" +
                "              \"type\": \"string\",\n" +
                "              \"enum\": [\n" +
                "                \"MULTIPLE\",\n" +
                "                \"SINGLE\"\n" +
                "              ],\n" +
                "              \"$id\": \"com.bbva.gsa.forms.model.QuestionType\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"$id\": \"com.bbva.gsa.forms.model.Question\"\n" +
                "        },\n" +
                "        \"$id\": \"java.util.Set<com.bbva.gsa.forms.model.Question>\"\n" +
                "      },\n" +
                "      \"title\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"url\": {\n" +
                "        \"type\": \"string\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"$id\": \"com.bbva.gsa.forms.model.Form\"\n" +
                "  }\n" +
                "]";

        String className = "Form";
        String packageName = "com.bbva.gsa.forms.model";
        jsonUtils.jsonToSourceClassFile(json, StringUtils.capitalize(className), packageName, sourceClassesDir, DEFINITIONS_ROOT, null);
    }
}
