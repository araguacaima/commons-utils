/*
  Copyright © 2010-2017 Nokia
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package com.araguacaima.commons.utils.jsonschema;

import com.araguacaima.commons.utils.PackageClassUtils;
import com.araguacaima.commons.utils.ReflectionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.*;
import org.apache.commons.lang3.StringUtils;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;


/**
 * Applies the schema rules that represent a property definition.
 *
 * @see <a href=
 * "http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.2">http:/
 * /tools.ietf.org/html/draft-zyp-json-schema-03#section-5.2</a>
 */
public class PropertyRule extends org.jsonschema2pojo.rules.PropertyRule {

    private static final Logger log = LoggerFactory.getLogger(PropertyRule.class);
    private static final ReflectionUtils reflectionUtils = ReflectionUtils.getInstance();
    private final String definitionsRoot;
    private final Map definitions;
    private final RuleFactory ruleFactory;

    PropertyRule(RuleFactory ruleFactory, String definitionsRoot, Map definitions) {
        super(ruleFactory);
        this.ruleFactory = ruleFactory;
        this.definitionsRoot = definitionsRoot;
        this.definitions = definitions;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * This rule adds a property to a given Java class according to the Java
     * Bean spec. A private field is added to the class, along with accompanying
     * accessor methods.
     * <p>
     * If this rule's schema mapper is configured to include builder methods
     * (see {@link GenerationConfig#isGenerateBuilders()} ),
     * then a builder method of the form <code>withFoo(Foo foo);</code> is also
     * added.
     *
     * @param nodeName the name of the property to be applied
     * @param node     the node describing the characteristics of this property
     * @param parent   the parent node
     * @param jclass   the Java class which should have this property added
     * @return the given jclass
     */
    @Override
    public JDefinedClass apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass jclass, Schema schema) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootDefinitions = mapper.valueToTree(definitions);
        ObjectNode schemaContent = (ObjectNode) schema.getContent();
        schemaContent.set(definitionsRoot, rootDefinitions);
        super.apply(nodeName, node, parent, jclass, schema);
        JsonNode $ref = node.get("$ref");
        if ($ref != null) {
            String ref = $ref.asText();
            if (StringUtils.isNotBlank(ref)) {
                int indexOfDefinitionsRoot = ref.indexOf(definitionsRoot);
                if (indexOfDefinitionsRoot != -1) {
                    if (ref.startsWith("#")) {
                        ref = ref.substring(1);
                    }
                    if (ref.startsWith("/")) {
                        ref = ref.substring(1);
                    }
                    ref = ref.substring(definitionsRoot.length());
                    if (ref.startsWith("/")) {
                        ref = ref.substring(1);
                    }
                }
                log.info("#### nodeName: " + nodeName + " | ref: " + ref);
                PackageClassUtils packageClassUtils = PackageClassUtils.instance(ref);
                ref = packageClassUtils.getPackageName();
                String className = packageClassUtils.getClassName();
                JFieldVar fieldVar = jclass.fields().get(nodeName);
                if (fieldVar != null) {
                    JType type = fieldVar.type();
                    String fullyQualifiedClassName = packageClassUtils.getFullyQualifiedClassName();
                    JType generatedType = ruleFactory.getGeneratedClassName(fullyQualifiedClassName);
                    if (generatedType == null) {
                        ruleFactory.addGeneratedClassName(fullyQualifiedClassName, type);
                        Class<? extends JType> aClass = type.getClass();
                        log.info("#### type: " + aClass.getName());
                        if (aClass.isAssignableFrom(JDefinedClass.class)) {
                            JDefinedClass jDefinedClass = (JDefinedClass) type;
                            JPackage jPackage = jDefinedClass._package();
                            Field fieldOuter = reflectionUtils.getField(JDefinedClass.class, "outer");
                            try {
                                fieldOuter.setAccessible(true);
                                JClassContainer outer = (JClassContainer) fieldOuter.get(type);
                                JCodeModel owner = jclass.owner();
                                JPackage newPackage = owner._package(ref);
                                fieldOuter.set(type, newPackage);
                                try {
                                    Field fieldClasses = reflectionUtils.getField(JPackage.class, "classes");
                                    fieldClasses.setAccessible(true);
                                    Map<String, JDefinedClass> classesNew = (Map<String, JDefinedClass>) fieldClasses.get(newPackage);
                                    classesNew.put(className, jDefinedClass);
                                    if (outer.isClass()) {
                                        fieldClasses = reflectionUtils.getField(JDefinedClass.class, "classes");
                                        fieldClasses.setAccessible(true);
                                        Map<String, JDefinedClass> classesOld = (Map<String, JDefinedClass>) fieldClasses.get(outer);
                                        classesOld.remove(className);
                                        JDefinedClass outer1 = (JDefinedClass) outer;
                                        log.info("#### outer: " + outer1.name() + " | classes: " + StringUtils.join(outer1.classes()));
                                    } else {
                                        log.info("#### outer '" + ((JPackage) outer).name() + "' is not a class");
                                    }

                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                                log.info("#### package: " + newPackage.name() + " | classes: " + StringUtils.join(newPackage.classes()));

                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return jclass;
    }

}
