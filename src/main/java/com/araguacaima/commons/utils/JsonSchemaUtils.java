package com.araguacaima.commons.utils;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;

public class JsonSchemaUtils {

    public static final String DEFINITIONS_ROOT = "definitions";
    private JsonUtils jsonUtils = new JsonUtils();
    private MapUtils mapUtils = MapUtils.getInstance();
    private CompilerUtils.FilesCompiler filesCompiler;

    public JsonSchemaUtils() throws IOException {
        filesCompiler = new CompilerUtils.FilesCompiler();
    }

    public Set<Class<?>> processFile(File file, String packageName, File sourceCodeDirectory, File compiledClassesDirectory) throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException, URISyntaxException {
        String json = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
        try {
            Map<String, String> jsonSchema = jsonUtils.fromJSON(json, Map.class);
            String id = jsonSchema.get("$id");
            jsonUtils.jsonToSourceClassFile(json, id, packageName, sourceCodeDirectory, DEFINITIONS_ROOT);
        } catch (MismatchedInputException ignored) {
            Collection<Map<String, Object>> jsonSchemas = jsonUtils.fromJSON(json, Collection.class);
            Set<String> ids = new LinkedHashSet<>();
            LinkedHashMap<String, LinkedHashMap> definitionMap = new LinkedHashMap<>();
            jsonSchemas.forEach(jsonSchema -> {
                try {
                    String id = jsonSchema.get("$id").toString();
                    String className_;
                    String packageName_;
                    if (id.contains(".")) {
                        className_ = id.substring(id.lastIndexOf('.') + 1);
                        packageName_ = id.substring(0, id.lastIndexOf('.'));
                    } else {
                        className_ = id;
                        packageName_ = packageName;
                    }
                    LinkedHashMap<String, LinkedHashMap> map_ = mapUtils.createKeysFromPackageName(packageName_, definitionMap);
                    Map innerMap = mapUtils.getLastValueFromPackageName(packageName_, map_);
                    innerMap.put(className_, jsonSchema);
                    jsonSchema.put("$id", id);
                    jsonSchema.put("$schema", "http://json-schema.org/draft-07/schema#");
                    ids.add(id);
                    definitionMap.putAll(map_);

                    //fixing $ref properties
                    LinkedHashMap properties = (LinkedHashMap) jsonSchema.get("properties");
                    if (MapUtils.isNotEmpty(properties)) {
                        for (Object key : properties.keySet()) {
                            LinkedHashMap value = (LinkedHashMap) properties.get(key);
                            String innerId = (String) value.get("$id");
                            if (StringUtils.isNotBlank(innerId) && innerId.contains(".")) {
                                value.clear();
                                value.put("$ref", "#/definitions/" + innerId.replaceAll("\\.", "/"));
                                LinkedHashMap definitions = (LinkedHashMap) jsonSchema.get(JsonSchemaUtils.DEFINITIONS_ROOT);
                                if (definitions == null) {
                                    definitions = new LinkedHashMap();
                                    jsonSchema.put(JsonSchemaUtils.DEFINITIONS_ROOT, definitions);
                                }
                                definitions.put(innerId, "");
                            }
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            });
            definitionsToClasses(definitionMap, ids, sourceCodeDirectory);
        }
        return filesCompiler.compile(sourceCodeDirectory, compiledClassesDirectory, org.apache.commons.io.FileUtils.listFiles(sourceCodeDirectory, new String[]{"java"}, true));
    }

    private void definitionsToClasses(LinkedHashMap<String, LinkedHashMap> definitions, Set<String> ids, File rootDirectory) throws IOException, NoSuchFieldException, IllegalAccessException {
        FileUtils.cleanDirectory(rootDirectory);
        for (String id : ids) {
            Map map = mapUtils.getLastValueFromPackageName(id, definitions);
            if (map != null) {
                PackageClassUtils packageClassUtils = new PackageClassUtils(id).invoke();
                String className = packageClassUtils.getClassName();
                String packageName = packageClassUtils.getPackageName();
                LinkedHashMap result = new LinkedHashMap();
                LinkedHashMap map1 = (LinkedHashMap) map.get(DEFINITIONS_ROOT);
                if (map1 != null) {
                    for (Object keyObj : map1.keySet()) {
                        String key = keyObj.toString();
                        Map value = mapUtils.getLastValueFromPackageName(key, definitions);
                        if (value != null) {
                            packageClassUtils = new PackageClassUtils(key).invoke();
                            String className_ = packageClassUtils.getClassName();
                            String packageName_ = packageClassUtils.getPackageName();
                            LinkedHashMap<String, LinkedHashMap> map_ = mapUtils.createKeysFromPackageName(packageName_, result);
                            value.remove("$id");
                            value.remove("$schema");
                            value.remove(DEFINITIONS_ROOT);
                            Map value_ = mapUtils.getLastValueFromPackageName(key, map_);
                            if (value_ == null) {
                                value_ = mapUtils.getLastValueFromPackageName(packageName_, map_);
                                value_.put(className_, value);
                            } else {
                                value_.putAll(value);
                            }
                            result.putAll(map_);
                        }
                    }
                }
                map.put(DEFINITIONS_ROOT, result);
                jsonUtils.jsonToSourceClassFile(jsonUtils.toJSON(map), StringUtils.capitalize(className), packageName, rootDirectory, DEFINITIONS_ROOT);
            }
        }
    }


}
