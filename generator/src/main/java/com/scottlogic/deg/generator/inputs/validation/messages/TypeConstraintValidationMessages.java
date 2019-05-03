package com.scottlogic.deg.generator.inputs.validation.messages;

public class TypeConstraintValidationMessages implements StandardValidationMessages {


    private Class validType;
    private Class invalidType;

    public TypeConstraintValidationMessages(Class validType, Class invalidType) {

        this.validType = validType;
        this.invalidType = invalidType;
    }

    @Override
    public String getVerboseMessage() {
        return String.format(
            "Type %s is not valid. The valid type is: %s",
            invalidType.getSimpleName(),
            validType.getSimpleName());
    }
}
