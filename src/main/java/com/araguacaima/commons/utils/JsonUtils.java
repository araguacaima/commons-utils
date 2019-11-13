package com.araguacaima.commons.utils;

import com.araguacaima.commons.utils.json.parser.*;
import com.araguacaima.commons.utils.jsonschema.RuleFactory;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.bohnman.squiggly.Squiggly;
import com.github.victools.jsonschema.generator.*;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JType;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Loader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.jsonschema2pojo.SchemaMapper;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;


public class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);
    private final Map<String, Class> classesFound = new HashMap<>();
    private final SimpleModule module = new SimpleModule("serializers", Version.unknownVersion());
    private final FileUtils fileUtils = new FileUtils();
    private final EnumsUtils<?> enumsUtils = EnumsUtils.getInstance();
    private final ReflectionUtils reflectionUtils = ReflectionUtils.getInstance();
    private ClassLoaderUtils classLoaderUtils;
    private MapUtils mapUtils;
    private ObjectMapper mapper;
    private Reflections reflections;

    public JsonUtils() {
        init();
    }

    private void init() {
        mapper = buildObjectMapper();
    }

    public ObjectMapper buildObjectMapper() {
        return buildObjectMapper(null);
    }

    public ObjectMapper buildObjectMapper(String filter) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.ANY);
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        module.addSerializer(DateTime.class, new DateTimeSerializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addSerializer(Enum.class, new EnumSerializer());
        module.addDeserializer(DateTime.class, new DateTimeDeserializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        mapper.registerModule(module);

        if (StringUtils.isNotBlank(filter)) {
            mapper = Squiggly.init(mapper, filter);
        }

        return mapper;
    }

    public void addDeserializer(Class clazz, JsonDeserializer<Object> deserializer) {
        this.module.addDeserializer(clazz, deserializer);
    }

    public void addSerializer(Class clazz, JsonSerializer serializer) {
        this.module.addSerializer(clazz, serializer);
    }


    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void setObjectMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Set<DataTypeInfo> buildObjectHierarchyFromJsonPath(String jsonPath,
                                                              ClassLoader classLoader,
                                                              Collection<String> fullArtifactsPath,
                                                              String canonicalPackagePrefix,
                                                              String nonCanonicalPackagePrefix,
                                                              String entitiesPackagePrefix)
            throws Exception {
        Set<DataTypeInfo> result = new LinkedHashSet<>();

        if (StringUtils.isNotBlank(jsonPath)) {
            if (classLoader == null) {
                classLoader = this.getClass().getClassLoader();
            }
            final Set<PriorityClass> priorityClasses = findClasses(jsonPath,
                    fullArtifactsPath,
                    canonicalPackagePrefix,
                    nonCanonicalPackagePrefix,
                    entitiesPackagePrefix);
            try {
                for (final PriorityClass priorityClass : priorityClasses) {
                    Class extClass = priorityClass.getClazz();
                    Set<DataTypeInfo> dataTypes = createNewBeansHierarchyFromJsonPath(jsonPath, extClass, classLoader);
                    if (dataTypes != null && dataTypes.size() > 0) {
                        result.addAll(dataTypes);
                        break;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return result;
    }

    private Set<PriorityClass> findClasses(String jsonPath,
                                           Collection<String> fullArtifactsPath,
                                           String canonicalPackagePrefix,
                                           String nonCanonicalPackagePrefix,
                                           String entitiesPackagePrefix)
            throws Exception {
        Set<PriorityClass> result = new LinkedHashSet<>();
        String type = jsonPath.split("\\.")[0];
        type = type.replaceFirst("\\[.*?]", StringUtils.EMPTY);
        final String capitalizedSubType = StringUtils.capitalize(type);
        String classname = StringUtils.EMPTY;

        PriorityClass priorityClass = new PriorityClass(canonicalPackagePrefix,
                nonCanonicalPackagePrefix,
                entitiesPackagePrefix);
        for (Map.Entry<String, Class> clazzEntry : classesFound.entrySet()) {
            String classname_ = clazzEntry.getKey();
            if (classname_.endsWith(capitalizedSubType)) {
                Class value = clazzEntry.getValue();
                priorityClass.setClazz(value);
                result.add(priorityClass);
            }
        }

        // Si no se encuentra en el mapa interno, buscar en el modelo no canónico

        Collection<String> subTypes;
        try {
            subTypes = (Collection<String>) org.apache.commons.collections4.CollectionUtils.select(reflections
                            .getAllTypes(),
                    (Predicate<String>) object -> {
                        String type12 = (String) object;
                        return type12.endsWith("." + capitalizedSubType);
                    });
        } catch (ReflectionsException re) {
            Set<String> classPathList = new TreeSet<>(fullArtifactsPath);
            classLoaderUtils.loadResourcesIntoClassLoader(classPathList);
            subTypes = (Collection<String>) org.apache.commons.collections4.CollectionUtils.select(reflections
                            .getAllTypes(),
                    (Predicate<String>) object -> {
                        String type1 = (String) object;
                        return type1.endsWith("." + capitalizedSubType);
                    });
        }

        for (String classname_ : subTypes) {
            if (classname_.endsWith(capitalizedSubType)) {
                priorityClass.setName(classname_);
                result.add(priorityClass);
                Class clazz = classLoaderUtils.loadClass(classname_);
                classesFound.put(classname, clazz);
            }
        }
        return result;
    }

    public <T> Set<DataTypeInfo> createNewBeansHierarchyFromJsonPath(String jsonPath,
                                                                     Class<T> dtoExtClass,
                                                                     ClassLoader classLoader)
            throws IllegalArgumentException {

        JsonPathParser<T> parser = new JsonPathParser<>(dtoExtClass, classLoader);
        Set<DataTypeInfo> result = new LinkedHashSet<>();
        try {
            if (StringUtils.isNotBlank(jsonPath)) {
                String[] tokens = jsonPath.split("\\.");
                DataTypeInfo dataType = new DataTypeInfo();
                String firstToken = StringUtils.uncapitalize(tokens[0].replaceFirst("\\[.*?]",
                        StringUtils.EMPTY).replaceFirst("DTO", StringUtils.EMPTY));
                dataType.setPath(firstToken);
                dataType.setType(dtoExtClass);
                dataType.setField(null);
                result.add(dataType);
                StringBuilder consumedTokens = new StringBuilder();
                consumedTokens.append(firstToken);
                if (tokens.length > 1) {
                    for (int i = 1; i < tokens.length; i++) {
                        try {
                            String token = tokens[i];
                            consumedTokens.append(".").append(token.replaceFirst("\\[.*?]", StringUtils.EMPTY));
                            String path = consumedTokens.toString();
                            Map<T, Field> map = null;
                            DataTypeInfo dataTypeInfo = new DataTypeInfo();
                            try {
                                map = parser.parse(path);
                            } catch (Throwable ignored) {
                                dataTypeInfo.setPath(jsonPath);
                                dataTypeInfo.setType(null);
                                dataTypeInfo.setType(null);
                                result.add(dataTypeInfo);
                            }
                            if (map != null && map.size() > 0) {
                                T type = map.keySet().iterator().next();
                                Field field = map.get(type);
                                if (field != null) {
                                    Class clazz = field.getType();
                                    if (reflectionUtils.isCollectionImplementation(clazz)) {

                                        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                                        Class generic = (Class) genericType.getActualTypeArguments()[0];
                                        if (generic.isInterface()) {
                                            Set<? extends Class<?>> classes = reflections.getSubTypesOf(generic);
                                            String finalGeneric = token;
                                            try {
                                                finalGeneric = token.substring(token.indexOf("<") + 1,
                                                        token.indexOf(">"));
                                            } catch (StringIndexOutOfBoundsException ignored) {
                                            }
                                            final String finalGeneric1 = finalGeneric;
                                            generic = org.apache.commons.collections4.IterableUtils.find(classes,
                                                    (Predicate) object -> ((Class) object).getSimpleName().equals(
                                                            StringUtils.capitalize(finalGeneric1)));
                                        }
                                        dataTypeInfo.setType(generic);
                                        dataTypeInfo.setCollection(true);
                                    } else {
                                        dataTypeInfo.setType(clazz);
                                    }
                                    dataTypeInfo.setPath(path);
                                    dataTypeInfo.setField(field);
                                    result.add(dataTypeInfo);
                                }
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            throw new IllegalArgumentException(e);
        }
        return result;
    }

    private boolean fieldFilter(Field field, Class finalClazz) {
        Set<Method> getters;
        String capitalizedFieldName = StringUtils.capitalize(field.getName());
        Class clazz = reflectionUtils.extractGenerics(field);
        if (!Boolean.class.isAssignableFrom(clazz) && !Boolean.TYPE.equals(clazz)) {
            getters = org.reflections.ReflectionUtils.getAllMethods(finalClazz,
                    org.reflections.ReflectionUtils.withModifier(Modifier.PUBLIC),
                    org.reflections.ReflectionUtils.withName("get" + capitalizedFieldName));
        } else {
            getters = org.reflections.ReflectionUtils.getAllMethods(finalClazz,
                    org.reflections.ReflectionUtils.withModifier(Modifier.PUBLIC),
                    org.reflections.ReflectionUtils.withName("is" + capitalizedFieldName));
        }
        Set<Method> setters = org.reflections.ReflectionUtils.getAllMethods(finalClazz,
                org.reflections.ReflectionUtils.withModifier(Modifier.PUBLIC),
                org.reflections.ReflectionUtils.withName("set" + capitalizedFieldName));
        return !field.getName().startsWith("this") && getters.size() >= 1 && setters != null && setters.size() >= 1;
    }

    public <T> T fromJSON(String jsonObject, Class<T> beanType) throws IOException {
        return this.mapper.readValue(jsonObject, beanType);
    }

    public <T> T fromJSON(ObjectMapper mapper, String jsonObject, Class<T> beanType)
            throws IOException {
        if (mapper != null) {
            return mapper.readValue(jsonObject, beanType);
        } else {
            return this.mapper.readValue(jsonObject, beanType);
        }

    }

    public Map<Class, Field> getFieldFromJsonPath(String jsonPath,
                                                  ClassLoader classLoader,
                                                  Collection<String> fullArtifactsPath,
                                                  String canonicalPackagePrefix,
                                                  String nonCanonicalPackagePrefix,
                                                  String entitiesPackagePrefix)
            throws Exception {
        Map<Class, Field> result = new HashMap<>();

        if (StringUtils.isNotBlank(jsonPath)) {
            if (classLoader == null) {
                classLoader = this.getClass().getClassLoader();
            }
            final Set<PriorityClass> dtoExtClass = findClasses(jsonPath,
                    fullArtifactsPath,
                    canonicalPackagePrefix,
                    nonCanonicalPackagePrefix,
                    entitiesPackagePrefix);
            Map<Object, Field> map;
            for (final PriorityClass priorityClass : dtoExtClass) {
                try {
                    Class extClass = priorityClass.getClazz();
                    map = createNewBeanFromJsonPath(jsonPath, extClass, classLoader);
                    if (map != null && map.size() > 0) {
                        Field field = (Field) mapUtils.findObject(map,
                                (Predicate) object -> object.getClass().getName().equals(priorityClass.getName()),
                                null,
                                MapUtils.EVALUATE_JUST_KEY);
                        result.put(extClass, field);
                        break;
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        return result;
    }

    public <T> Map<T, Field> createNewBeanFromJsonPath(String jsonPath, Class<T> dtoExtClass, ClassLoader classLoader)
            throws IllegalArgumentException {

        JsonPathParser<T> parser = new JsonPathParser<>(dtoExtClass, classLoader);
        try {
            if (StringUtils.isNotBlank(jsonPath)) {
                return parser.parse(jsonPath);
            }
        } catch (Throwable e) {
            System.out.println(e.getMessage());
            throw new IllegalArgumentException(e);
        }
        return null;
    }

    public JsonNode getJsonNode()
            throws IOException {
        return getJsonNode(toJSON("{}"));
    }

    public JsonNode getJsonNode(String jsonStr)
            throws IOException {
        return mapper.readTree(jsonStr);
    }

    public JsonNode getJsonNode(Object object)
            throws IOException {
        return getJsonNode(toJSON(object));
    }

    public JsonNode getJsonNodeFromJsonPath(JsonNode jsonNode, String jsonPath) {
        if (jsonNode == null) {
            return null;
        }
        jsonPath = jsonPath.replaceAll("\\.", "/");
        if (!jsonPath.startsWith("/")) {
            jsonPath = "/" + jsonPath;
        }
        return jsonNode.at(jsonPath);
    }

    public ObjectNode getObjectNode()
            throws IOException {
        return getObjectNode(toJSON("{}"));
    }

    public ObjectNode getObjectNode(String jsonStr)
            throws IOException {
        JsonNode jsonNode = mapper.readTree(jsonStr);
        return (ObjectNode) jsonNode;
    }

    public ObjectNode getObjectNode(Object object)
            throws IOException {
        return getObjectNode(toJSON(object));
    }

    public String mergeJson(String baseJson, String jsonForMerging)
            throws IOException {
        Object defaults = mapper.readValue(baseJson, Object.class);
        ObjectReader updater = mapper.readerForUpdating(defaults);
        Object merged = updater.readValue(jsonForMerging);
        return toJSON(merged);
    }

    public String toJSON(final Object object)
            throws IOException {
        return toJSON(object, false, null);
    }

    public String toJSON(final Object object, String filter)
            throws IOException {
        return toJSON(object, false, filter);
    }

    public String toJSON(final Object object, boolean flatten)
            throws IOException {
        return toJSON(object, flatten, null);
    }

    public String toJSON(final Object object, boolean flatten, String filter)
            throws IOException {
        return toJSON(mapper, object, flatten, filter);
    }

    public String toJSON(ObjectMapper mapper, final Object object)
            throws IOException {
        return toJSON(mapper, object, false, null);
    }

    public String toJSON(ObjectMapper mapper, final Object object, boolean flatten)
            throws IOException {
        return toJSON(mapper, object, flatten, null);
    }

    public String toJSON(ObjectMapper mapper, final Object object, boolean flatten, String filter)
            throws IOException {
        ObjectMapper mapper_;
        if (StringUtils.isNotBlank(filter)) {
            mapper_ = buildObjectMapper(filter);
        } else {
            mapper_ = mapper;
        }
        ObjectWriter writer;
        if (flatten) {
            writer = mapper_.writer();
        } else {
            writer = mapper_.writerWithDefaultPrettyPrinter();
        }
        return writer.writeValueAsString(object);
    }

    public LinkedHashSet<String> buildJsonPath(Class clazz) {
        if (clazz != null) {
            return buildJsonPath(clazz, null, true);
        } else {
            return new LinkedHashSet<>();
        }
    }

    public LinkedHashSet<String> buildJsonPath(Class clazz, String rootName) {
        if (clazz != null) {
            return buildJsonPath(clazz, rootName, true);
        } else {
            return new LinkedHashSet<>();
        }
    }

    public LinkedHashSet<String> buildJsonPath(Class clazz,
                                               String rootName,
                                               boolean includeDatatype) {
        StringBuilder sb = new StringBuilder();
        if (clazz != null) {
            String innerRootName = StringUtils.uncapitalize(clazz.getSimpleName());
            if (!StringUtils.isBlank(rootName)) {
                innerRootName = rootName + "." + innerRootName;
            }
            sb.append(innerRootName);
            List<Class> visited = new ArrayList<>();
            return buildJsonPath(sb,
                    visited,
                    clazz,
                    includeDatatype,
                    reflectionUtils.isCollectionImplementation(clazz));
        }
        return new LinkedHashSet<>();
    }

    private LinkedHashSet<String> buildJsonPath(StringBuilder sb,
                                                List<Class> visited,
                                                Class clazz,
                                                boolean includeDatatype,
                                                boolean isCollection) {
        final LinkedHashSet<String> mappings = new LinkedHashSet<>();
        if (clazz != null) {
            if (!visited.contains(clazz)) {
                StringBuilder classRow = new StringBuilder();
                boolean isEnum = clazz.isEnum();
                if (isCollection) {
                    Class generics = reflectionUtils.extractGenerics(clazz);
                    sb.append("[]");
                    if (generics != null && !Object.class.equals(generics)) {
                        clazz = generics;
                    }
                    if (includeDatatype) {
                        classRow.append(sb).append("\t").append("Array");
                    }
                } else {
                    if (includeDatatype) {
                        classRow.append(sb).append("\t").append(isEnum ? "String" : "Object");
                    }
                }
                visited.add(clazz);
                mappings.add(classRow.toString());
                final Class finalClazz = clazz;

                reflectionUtils.doWithFields(clazz,
                        field -> {
                            Class<?> type = reflectionUtils.extractGenerics(field);
                            boolean fieldIsCollection = reflectionUtils.isCollectionImplementation(field.getType());
                            StringBuilder fieldRow = new StringBuilder(sb).append(".").append(field.getName());

                            if (reflectionUtils.getSimpleJavaTypeOrNull(type) == null || fieldIsCollection) {
                                LinkedHashSet<String> c;
                                c = buildJsonPath(fieldRow,
                                        visited,
                                        type,
                                        includeDatatype,
                                        fieldIsCollection);
                                visited.remove(type);
                                mappings.addAll(c);

                            } else {
                                if (includeDatatype) {
                                    fieldRow.append("\t").append(type.getSimpleName());
                                }
                                mappings.add(fieldRow.toString());
                            }
                        },
                        field -> fieldFilter(field, finalClazz));
            }
        }
        return mappings;
    }

    public void buildJsonSchemaMapFromClassFile(File rootDirectory, File file, Map<String, String> jsonSchemas) {
        buildJsonSchemaMapFromClassFile(rootDirectory, file, jsonSchemas, null, null);
    }

    public void buildJsonSchemaMapFromClassFile(File rootDirectory, File file, Map<String, String> jsonSchemas, Collection<Option> with, Collection<Option> without) {
        String currentClass = null;
        String currentFile = fileUtils.getRelativePathFrom(rootDirectory, file);
        String CLASS_SUFFIX = "class";
        if (com.araguacaima.commons.utils.StringUtils.isNotBlank(currentFile)) {
            currentFile = currentFile.substring(1) + File.separator + file.getName();
            currentClass = currentFile.replace("." + CLASS_SUFFIX, StringUtils.EMPTY).replaceAll("\\\\", ".");
        }
        CtClass ctClass;
        ClassPool pool = ClassPool.getDefault();
        try {
            if (!file.exists()) {
                throw new RuntimeException("Class '" + file.getCanonicalPath() + "' does not belongs to provided classes' path. It's not possible to load classes depending to other libraries");
            }
            if (file.isFile()) {
                Loader cl = new Loader(pool);
                try {
                    ctClass = pool.get(currentClass);
                } catch (Throwable ignored) {
                    ctClass = pool.makeClassIfNew(new FileInputStream(file));
                }
                ctClass.defrost();
                Class<?> class_ = cl.loadClass(currentClass);
                String jsonSchema = getJsonSchema(class_, true, with, without);
                String className = ctClass.getName();
                log.debug("Class '" + className + "' loaded successfully!. Schema: \n" + jsonSchema);
                jsonSchemas.put(className, jsonSchema);
            }
        } catch (java.lang.NoClassDefFoundError ncdfe) {
            String dependencyName = ncdfe.getCause().getMessage().replaceAll("\\.", "/");
            File dependency = new File(rootDirectory, dependencyName + "." + CLASS_SUFFIX);
            buildJsonSchemaMapFromClassFile(rootDirectory, dependency, jsonSchemas);
            buildJsonSchemaMapFromClassFile(rootDirectory, file, jsonSchemas);
        } catch (TypeNotPresentException tnpe) {
            String dependencyName = tnpe.typeName().replaceAll("\\.", "/");
            File dependency = new File(rootDirectory, dependencyName + "." + CLASS_SUFFIX);
            buildJsonSchemaMapFromClassFile(rootDirectory, dependency, jsonSchemas);
            buildJsonSchemaMapFromClassFile(rootDirectory, file, jsonSchemas);
        } catch (Throwable e) {
            String msg = "It was not possible to load file '" + currentFile + "' due to the following exception: " + e.getMessage();
            log.error(msg);
            throw new RuntimeException(msg);
        }

    }

    public String getJsonSchema(Class<?> clazz, boolean showVersion) {
        return getJsonSchema(clazz, showVersion, Collections.singletonList(Option.DEFINITIONS_FOR_ALL_OBJECTS), null);
    }

    public String getJsonSchema(Class<?> clazz, boolean showVersion, Collection<Option> with, Collection<Option> without) {
        ObjectMapper objectMapper = new ObjectMapper();
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(objectMapper, OptionPreset.PLAIN_JSON);
        if (showVersion) {
            configBuilder.with(Option.SCHEMA_VERSION_INDICATOR);
        } else {
            configBuilder.without(Option.SCHEMA_VERSION_INDICATOR);
        }
        if (CollectionUtils.isNotEmpty(with)) {
            IterableUtils.forEach(with, option -> configBuilder.with(option));
        }
        if (CollectionUtils.isNotEmpty(without)) {
            IterableUtils.forEach(without, option -> configBuilder.without(option));
        }
        SchemaGeneratorConfigPart<FieldScope> scopeSchemaGeneratorConfigPart = new SchemaGeneratorConfigPart<>();
        configBuilder.with((jsonSchemaTypeNode, javaType, config) -> jsonSchemaTypeNode.put("$id", javaType.getTypeName()));
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(clazz);
        return jsonSchema.toString();
    }

    public String getDefaultJsonSchema() {
        ObjectMapper objectMapper = new ObjectMapper();
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(objectMapper, OptionPreset.PLAIN_JSON);
        SchemaGeneratorConfig config = configBuilder.build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode jsonSchema = generator.generateSchema(Object.class);
        return jsonSchema.toString();
    }

    public void jsonToSourceClassFile(String json, String className, String packageName, File rootDirectory, RuleFactory ruleFactory, org.jsonschema2pojo.SchemaGenerator schemaGenerator) throws IOException {
        String fullClassName = packageName + "." + className;
        //if (!ruleFactory.classNameAlreadyGenerated(fullClassName)) {
            JCodeModel codeModel = new JCodeModel();
            SchemaMapper mapper = new SchemaMapper(ruleFactory, schemaGenerator);
            //log.trace("#### evaluating: " + json);
            JType type = mapper.generate(codeModel, className, packageName, json);
            //JType generatedType = ruleFactory.getGeneratedClassName(fullClassName);
            //if (generatedType == null) {
                codeModel.build(rootDirectory);
                /*Collection<File> files = FileUtils.listFiles(rootDirectory, new String[]{"java"}, true);
                ruleFactory.getGeneratedTypes().forEach((key, value) -> {
                    JDefinedClass clazz = (JDefinedClass) value.boxify();
                    ruleFactory.addGeneratedClassName(clazz.getPackage().name() + "." + key, value);
                });
                ruleFactory.addGeneratedClassName(((JDefinedClass) type.boxify()).getPackage().name() + "." + className, type);*/
            //}
        //}
    }

    private class PriorityClass implements Comparable<PriorityClass> {
        private String canonicalPackagePattern = StringUtils.EMPTY;
        private Class clazz;
        private String entitiesPackagePattern = StringUtils.EMPTY;
        private String name;
        private String nonCanonicalPackagePattern = StringUtils.EMPTY;

        public PriorityClass(String canonicalPackage, String nonCanonicalPackage, String entitiesPackage) {
            this();
            this.canonicalPackagePattern = canonicalPackage + "\\..*?\\.";
            this.nonCanonicalPackagePattern = nonCanonicalPackage + "\\..*?\\.";
            this.entitiesPackagePattern = entitiesPackage + "\\..*?\\.";
        }

        private PriorityClass() {
        }

        @Override
        public int compareTo(PriorityClass o) {
            if (o == null) {
                return 0;
            } else {
                if (o.clazz == null) {
                    return 0;
                } else {
                    if (o.clazz.getName().matches(this.entitiesPackagePattern + o.clazz.getSimpleName())) {
                        return 3;
                    } else if (o.clazz.getName().matches(this.nonCanonicalPackagePattern + o.clazz.getSimpleName())) {
                        return 2;
                    } else if (o.clazz.getName().matches(this.canonicalPackagePattern + o.clazz.getSimpleName())) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        }

        public String getCanonicalPackagePattern() {
            return canonicalPackagePattern;
        }

        public Class getClazz() {
            return clazz;
        }

        public void setClazz(Class clazz) {
            this.clazz = clazz;
            if (this.clazz != null) {
                this.name = clazz.getName();
            }
        }

        public String getEntitiesPackagePattern() {
            return entitiesPackagePattern;
        }

        public String getName() {
            return name;
        }

        public void setName(String name)
                throws ClassNotFoundException {
            this.name = name;
            this.clazz = classLoaderUtils.loadClass(name);
        }

        public String getNonCanonicalPackagePattern() {
            return nonCanonicalPackagePattern;
        }
    }
}
