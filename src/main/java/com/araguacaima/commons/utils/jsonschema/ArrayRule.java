package com.araguacaima.commons.utils.jsonschema;

import com.araguacaima.commons.utils.ReflectionUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.util.Inflector;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArrayRule extends org.jsonschema2pojo.rules.ArrayRule {

    private static final ReflectionUtils reflectionUtils = new ReflectionUtils(null);
    private String definitionsRoot;
    private Map definitions;
    private RuleFactory ruleFactory;

    ArrayRule(RuleFactory ruleFactory) {
        super(ruleFactory);
    }

    ArrayRule(RuleFactory ruleFactory, String definitionsRoot, Map definitions) {
        super(ruleFactory);
        this.ruleFactory = ruleFactory;
        this.definitionsRoot = definitionsRoot;
        this.definitions = definitions;
    }

    /**
     * <p>Applies this schema rule to take the required code generation steps.</p>
     *
     * <p>When constructs of type "array" appear in the schema, these are mapped to
     * Java collections in the generated POJO. If the array is marked as having
     * "uniqueItems" then the resulting Java type is {@link Set}, if not, then
     * the resulting Java type is {@link List}. The schema given by "items" will
     * decide the generic type of the collection.</p>
     *
     * <p>If the "items" property requires newly generated types, then the type
     * name will be the singular version of the nodeName (unless overridden by
     * the javaType property) e.g.
     * <pre>
     *  "fooBars" : {"type":"array", "uniqueItems":"true", "items":{type:"object"}}
     *  ==&gt;
     *  {@code Set<FooBar> getFooBars(); }
     * </pre>
     * </p>
     *
     * @param nodeName the name of the property which has type "array"
     * @param node     the schema "type" node
     * @param parent   the parent node
     * @param jpackage the package into which newly generated types should be added
     * @return the Java type associated with this array rule, either {@link Set}
     * or {@link List}, narrowed by the "items" type
     */
    @Override
    public JClass apply(String nodeName, JsonNode node, JsonNode parent, JPackage jpackage, Schema schema) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootDefinitions = mapper.valueToTree(definitions);
        ObjectNode schemaContent = (ObjectNode) schema.getContent();
        schemaContent.set(definitionsRoot, rootDefinitions);
        super.apply(nodeName, node, parent, jpackage, schema);
        JClass arrayType;

        boolean uniqueItems = node.has("uniqueItems") && node.get("uniqueItems").asBoolean();
        boolean rootSchemaIsArray = !schema.isGenerated();

        JType itemType;
        if (node.has("items")) {
            Rule<JClassContainer, JType> schemaRule = ruleFactory.getSchemaRule();

            JsonNode items = node.get("items");
            itemType = schemaRule.apply(nodeName, items, node, jpackage, schema);
        } else {
            itemType = jpackage.owner().ref(Object.class);
        }

        if (uniqueItems) {
            arrayType = jpackage.owner().ref(Set.class).narrow(itemType);
        } else {
            arrayType = jpackage.owner().ref(List.class).narrow(itemType);
        }

        if (rootSchemaIsArray) {
            schema.setJavaType(arrayType);
        }
        return arrayType;
    }

    private String makeSingular(String nodeName) {
        return Inflector.getInstance().singularize(nodeName);
    }
}
