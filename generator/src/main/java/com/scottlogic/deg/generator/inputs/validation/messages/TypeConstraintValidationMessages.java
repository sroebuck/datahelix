package com.scottlogic.deg.generator.inputs.validation.messages;

import com.scottlogic.deg.generator.generation.TypeDefinition;

public class TypeConstraintValidationMessages implements StandardValidationMessages {


    private TypeDefinition validType;
    private TypeDefinition invalidType;

    public TypeConstraintValidationMessages(TypeDefinition validType, TypeDefinition invalidType) {

        this.validType = validType;
        this.invalidType = invalidType;
    }

    @Override
    public String getVerboseMessage() {
        return String.format(
            "Type %s is not valid. The valid type is: %s",
            invalidType.getType().getSimpleName(),
            validType.getType().getSimpleName());
    }
}
