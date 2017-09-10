package com.araguacaima.commons.utils;

import com.araguacaima.commons.utils.json.parser.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;

@Component
public class JsonUtils {

    private ClassLoaderUtils classLoaderUtils;
    private Map<String, Class> classesFound = new HashMap<>();
    private MapUtils mapUtils;
    private ObjectMapper mapper;
    private SimpleModule module = new SimpleModule("serializers", Version.unknownVersion());
    private Reflections reflections;

    @Autowired
    public JsonUtils(ClassLoaderUtils classLoaderUtils,
                     MapUtils mapUtils,
                     @Qualifier("reflections") Reflections reflections) {

        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.ANY);
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);

        module.addSerializer(DateTime.class, new DateTimeSerializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addSerializer(Enum.class, new EnumSerializer());
        module.addDeserializer(DateTime.class, new DateTimeDeserializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        mapper.registerModule(module);
        this.mapUtils = mapUtils;
    }

    public void addDeserializer(Class clazz, JsonDeserializer deserializer) {
        this.module.addDeserializer(clazz, deserializer);
    }

    public void addSerializer(Class clazz, JsonSerializer serializer) {
        this.module.addSerializer(clazz, serializer);
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
        type = type.replaceFirst("\\[.*?\\]", StringUtils.EMPTY);
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

        Collection<String> subTypes = new ArrayList<>();
        try {
            subTypes = (Collection<String>) org.apache.commons.collections4.CollectionUtils.select(reflections
                            .getAllTypes(),
                    new Predicate() {
                        @Override
                        public boolean evaluate(Object object) {
                            String type = (String) object;
                            return type.endsWith("." + capitalizedSubType);
                        }
                    });
        } catch (ReflectionsException re) {
            Set<String> classPathList = new TreeSet<String>();
            classPathList.addAll(fullArtifactsPath);
            classLoaderUtils.loadResourcesIntoClassLoader(classPathList);
            subTypes = (Collection<String>) org.apache.commons.collections4.CollectionUtils.select(reflections
                            .getAllTypes(),
                    new Predicate() {
                        @Override
                        public boolean evaluate(Object object) {
                            String type = (String) object;
                            return type.endsWith("." + capitalizedSubType);
                        }
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
            throws IllegalArgumentException, IllegalAccessException, InstantiationException {

        JsonPathParser<T> parser = new JsonPathParser<T>(dtoExtClass, classLoader);
        Set<DataTypeInfo> result = new LinkedHashSet<>();
        try {
            if (StringUtils.isNotBlank(jsonPath)) {
                String[] tokens = jsonPath.split("\\.");
                DataTypeInfo dataType = new DataTypeInfo();
                String firstToken = StringUtils.uncapitalize(tokens[0].replaceFirst("\\[.*?\\]",
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
                            consumedTokens.append(".").append(token.replaceFirst("\\[.*?\\]", StringUtils.EMPTY));
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
                                    if (ReflectionUtils.isCollectionImplementation(clazz)) {

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
                                            generic = (Class) org.apache.commons.collections4.CollectionUtils.find(
                                                    classes,
                                                    new Predicate() {
                                                        @Override
                                                        public boolean evaluate(Object object) {
                                                            return ((Class) object).getSimpleName().equals
                                                                    (StringUtils.capitalize(
                                                                    finalGeneric1));
                                                        }
                                                    });
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
        Class clazz = ReflectionUtils.extractGenerics(field);
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
        return !field.getName().startsWith("this") && getters != null && getters.size() >= 1 && setters != null &&
                setters.size() >= 1;
    }

    public Object fromJSON(ObjectMapper mapper, final String jsonObject, final Class beanType)
            throws IOException {
        return mapper.readValue(jsonObject, beanType);
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
                        Field field = (Field) mapUtils.findObject(map, new Predicate() {
                            @Override
                            public boolean evaluate(Object object) {
                                return object.getClass().getName().equals(priorityClass.getName());
                            }
                        }, null, MapUtils.EVALUATE_JUST_KEY);
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
            throws IllegalArgumentException, IllegalAccessException, InstantiationException {

        JsonPathParser<T> parser = new JsonPathParser<T>(dtoExtClass, classLoader);
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

    public String toJSON(final Object object)
            throws IOException {
        return toJSON(object, false);
    }

    public String toJSON(final Object object, boolean flatten)
            throws IOException {
        if (flatten) {
            return mapper.writer().writeValueAsString(object);
        } else {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        }
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

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
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

    public void setObjectMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String toJSON(ObjectMapper mapper, final Object object)
            throws IOException {
        return toJSON(mapper, object, false);
    }

    public String toJSON(ObjectMapper mapper, final Object object, boolean flatten)
            throws IOException {
        if (flatten) {
            return mapper.writer().writeValueAsString(object);
        } else {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        }
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
