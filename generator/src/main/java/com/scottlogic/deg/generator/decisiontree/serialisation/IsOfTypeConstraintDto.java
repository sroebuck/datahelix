package com.scottlogic.deg.generator.decisiontree.serialisation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IsOfTypeConstraintDto implements ConstraintDto {
    public FieldDto field;
    @JsonProperty("requiredType")
    public String requiredTypeString;

    public String rule;

    public Class getTypesFromTypesDto() {
        try {
            return Class.forName(requiredTypeString);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}