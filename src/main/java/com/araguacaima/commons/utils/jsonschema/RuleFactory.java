package com.araguacaima.commons.utils.jsonschema;

import com.araguacaima.commons.utils.ReflectionUtils;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.util.ParcelableHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class RuleFactory extends org.jsonschema2pojo.rules.RuleFactory {

    private static final ReflectionUtils reflectionUtils = ReflectionUtils.getInstance();
    private final String definitionsRoot;
    private final Map definitions;
    private final Map<String, JType> generatedTypes = new HashMap<>();
    private final Set<String> generatedClassNames = new HashSet<>();

    public RuleFactory(GenerationConfig generationConfig, Annotator annotator, SchemaStore schemaStore, String definitionsRoot, Map definitions) throws NoSuchFieldException, IllegalAccessException {
        super(generationConfig, annotator, schemaStore);
        this.definitions = definitions;
        this.definitionsRoot = definitionsRoot;
        Field field = org.jsonschema2pojo.rules.RuleFactory.class.getDeclaredField("nameHelper");
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        int modifiers = modifiersField.getInt(field);

        // blank out the final bit in the modifiers int
        modifiers &= ~Modifier.FINAL;
        modifiersField.setInt(field, modifiers);
        field.set(this, new NameHelper(generationConfig, true));
    }

    /**
     * Provides a rule instance that should be applied when a property
     * declaration (child of the "properties" declaration) is found in the
     * schema.
     *
     * @return a schema rule that can handle a property declaration.
     */
    @Override
    public Rule<JDefinedClass, JDefinedClass> getPropertyRule() {
        return new PropertyRule(this, definitionsRoot, definitions);
    }

    /**
     * Provides a rule instance that should be applied when an "array"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "array" declaration.
     */
    @Override
    public Rule<JPackage, JClass> getArrayRule() {
        return new ArrayRule(this, definitionsRoot, definitions);
    }

    /**
     * Provides a rule instance that should be applied when an "object"
     * declaration is found in the schema.
     *
     * @return a schema rule that can handle the "object" declaration.
     */
    @Override
    public Rule<JPackage, JType> getObjectRule() {
        return new ObjectRule(this, new ParcelableHelper(), getReflectionHelper());
    }

    public Map<String, JType> getGeneratedTypes() {
        return generatedTypes;
    }

    public Set<String> getGeneratedClassNames() {
        return generatedClassNames;
    }

    public void addGeneratedClassName(String generatedClassName) {
        generatedClassNames.add(generatedClassName);
    }

    public boolean classNameAlreadyGenerated(String generatedClassName) {
        return generatedClassNames.contains(generatedClassName);
    }
}
