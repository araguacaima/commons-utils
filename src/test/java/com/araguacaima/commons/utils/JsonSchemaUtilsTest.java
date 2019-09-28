package com.araguacaima.commons.utils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class JsonSchemaUtilsTest {

    private JsonSchemaUtils<ClassLoader> jsonSchemaUtils = new JsonSchemaUtils<>(JsonSchemaUtilsTest.class.getClassLoader());
    private String schema;
    private File sourceClassesDir;
    private File compiledClassesDir;
    private String schemas;

    @Before
    public void init() {
        JsonSchemaUtils<ClassLoader> jsonSchemaUtils = new JsonSchemaUtils<>(JsonSchemaUtilsTest.class.getClassLoader());
        schema = "  {\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "      \"description\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"id\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"questions\": {\n" +
                "        \"type\": \"array\",\n" +
                "        \"items\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"properties\": {\n" +
                "            \"calculatedScore\": {\n" +
                "              \"type\": \"number\"\n" +
                "            },\n" +
                "            \"description\": {\n" +
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
                "                  \"description\": {\n" +
                "                    \"type\": \"string\"\n" +
                "                  },\n" +
                "                  \"selected\": {\n" +
                "                    \"type\": \"boolean\"\n" +
                "                  },\n" +
                "                  \"title\": {\n" +
                "                    \"type\": \"string\"\n" +
                "                  },\n" +
                "                  \"weighing\": {\n" +
                "                    \"type\": \"number\"\n" +
                "                  }\n" +
                "                },\n" +
                "                \"$id\": \"com.araguacaima.braas.core.drools.model.forms.QuestionOption\"\n" +
                "              },\n" +
                "              \"$id\": \"java.util.Set<com.araguacaima.braas.core.drools.model.forms.QuestionOption>\"\n" +
                "            },\n" +
                "            \"title\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"type\": {\n" +
                "              \"type\": \"string\",\n" +
                "              \"enum\": [\n" +
                "                \"BOOLEAN\",\n" +
                "                \"MULTIPLE\",\n" +
                "                \"SINGLE\",\n" +
                "                \"TRISTATE\"\n" +
                "              ],\n" +
                "              \"$id\": \"com.araguacaima.braas.core.drools.model.forms.QuestionType\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"$id\": \"com.araguacaima.braas.core.drools.model.forms.Question\"\n" +
                "        },\n" +
                "        \"$id\": \"java.util.Set<com.araguacaima.braas.core.drools.model.forms.Question>\"\n" +
                "      },\n" +
                "      \"title\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"url\": {\n" +
                "        \"type\": \"string\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"$id\": \"com.araguacaima.braas.core.drools.model.forms.Form\"\n" +
                "  }\n";
        schemas = "[\n" +
                "  {\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "      \"calculatedScore\": {\n" +
                "        \"type\": \"number\"\n" +
                "      },\n" +
                "      \"description\": {\n" +
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
                "            \"description\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"selected\": {\n" +
                "              \"type\": \"boolean\"\n" +
                "            },\n" +
                "            \"title\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"weighing\": {\n" +
                "              \"type\": \"number\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"$id\": \"com.araguacaima.braas.core.drools.model.forms.QuestionOption\"\n" +
                "        },\n" +
                "        \"$id\": \"java.util.Set<com.araguacaima.braas.core.drools.model.forms.QuestionOption>\"\n" +
                "      },\n" +
                "      \"title\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"type\": {\n" +
                "        \"type\": \"string\",\n" +
                "        \"enum\": [\n" +
                "          \"BOOLEAN\",\n" +
                "          \"MULTIPLE\",\n" +
                "          \"SINGLE\"\n" +
                "        ],\n" +
                "        \"$id\": \"com.araguacaima.braas.core.drools.model.forms.QuestionType\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"$id\": \"com.araguacaima.braas.core.drools.model.forms.Question\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"type\": \"string\",\n" +
                "    \"enum\": [\n" +
                "      \"BOOLEAN\",\n" +
                "      \"MULTIPLE\",\n" +
                "      \"SINGLE\"\n" +
                "    ],\n" +
                "    \"$id\": \"com.araguacaima.braas.core.drools.model.forms.QuestionType\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "      \"description\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"selected\": {\n" +
                "        \"type\": \"boolean\"\n" +
                "      },\n" +
                "      \"title\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"weighing\": {\n" +
                "        \"type\": \"number\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"$id\": \"com.araguacaima.braas.core.drools.model.forms.QuestionOption\"\n" +
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
                "      \"questions\": {\n" +
                "        \"type\": \"array\",\n" +
                "        \"items\": {\n" +
                "          \"type\": \"object\",\n" +
                "          \"properties\": {\n" +
                "            \"calculatedScore\": {\n" +
                "              \"type\": \"number\"\n" +
                "            },\n" +
                "            \"description\": {\n" +
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
                "                  \"description\": {\n" +
                "                    \"type\": \"string\"\n" +
                "                  },\n" +
                "                  \"selected\": {\n" +
                "                    \"type\": \"boolean\"\n" +
                "                  },\n" +
                "                  \"title\": {\n" +
                "                    \"type\": \"string\"\n" +
                "                  },\n" +
                "                  \"weighing\": {\n" +
                "                    \"type\": \"number\"\n" +
                "                  }\n" +
                "                },\n" +
                "                \"$id\": \"com.araguacaima.braas.core.drools.model.forms.QuestionOption\"\n" +
                "              },\n" +
                "              \"$id\": \"java.util.Set<com.araguacaima.braas.core.drools.model.forms.QuestionOption>\"\n" +
                "            },\n" +
                "            \"title\": {\n" +
                "              \"type\": \"string\"\n" +
                "            },\n" +
                "            \"type\": {\n" +
                "              \"type\": \"string\",\n" +
                "              \"enum\": [\n" +
                "                \"BOOLEAN\",\n" +
                "                \"MULTIPLE\",\n" +
                "                \"SINGLE\"\n" +
                "              ],\n" +
                "              \"$id\": \"com.araguacaima.braas.core.drools.model.forms.QuestionType\"\n" +
                "            }\n" +
                "          },\n" +
                "          \"$id\": \"com.araguacaima.braas.core.drools.model.forms.Question\"\n" +
                "        },\n" +
                "        \"$id\": \"java.util.Set<com.araguacaima.braas.core.drools.model.forms.Question>\"\n" +
                "      },\n" +
                "      \"title\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"url\": {\n" +
                "        \"type\": \"string\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"$id\": \"com.araguacaima.braas.core.drools.model.forms.Form\"\n" +
                "  }\n" +
                "]";
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
