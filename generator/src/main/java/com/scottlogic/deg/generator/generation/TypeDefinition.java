package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.inputs.InvalidProfileException;

public class TypeDefinition {
    public static final TypeDefinition String = StringFieldValueSourceFactory.getTypeDefinition();
    public static final TypeDefinition Numeric = NumericFieldValueSourceFactory.getTypeDefinition();
    public static final TypeDefinition Temporal = TemporalFieldValueSourceFactory.getTypeDefinition();

    private final FieldValueSourceFactory factory;

    public TypeDefinition(FieldValueSourceFactory factory) {
        this.factory = factory;
    }

    public static TypeDefinition parse(String typeString) throws InvalidProfileException {
        throw new InvalidProfileException("Unrecognised type in type constraint: " + typeString);
    }

    public Class getType() {
        return factory.getUnderlyingDataType();
    }

    public FieldValueSource getFieldValueSource(FieldSpec fieldSpec){
        return factory.createValueSource(fieldSpec);
    }

    @Override
    public int hashCode(){
        return factory.getUnderlyingDataType().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeDefinition) {
            return getType().equals(((TypeDefinition) obj).getType());
        }

        return false;
    }
}
