package com.scottlogic.deg.generator.decisiontree.serialisation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scottlogic.deg.generator.generation.TypeDefinition;
import com.scottlogic.deg.generator.restrictions.AnyTypeRestriction;

import java.util.Optional;
import java.util.Set;

public class IsOfTypeConstraintDto implements ConstraintDto {
    public FieldDto field;
    @JsonProperty("requiredType")
    public String requiredTypeString;

    public String rule;

    public TypeDefinition getTypesFromTypesDto() {
        try {
            Class clazz = Class.forName(requiredTypeString);
            Set<TypeDefinition> allKnownTypes = new AnyTypeRestriction().getAllowedTypes();

            Optional<TypeDefinition> typeDefinition = allKnownTypes.stream()
                .filter(td -> td.getType().equals(clazz))
                .findFirst();

            return typeDefinition.orElseThrow(() -> new RuntimeException("Unable to find type definition for class: " + clazz.getName()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}