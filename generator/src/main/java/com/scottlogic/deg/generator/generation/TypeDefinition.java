package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.DataGeneratorBaseTypes;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;

public class TypeDefinition {
    public static final TypeDefinition String = StringFieldValueSourceFactory.getTypeDefinition();
    public static final TypeDefinition Numeric = NumericFieldValueSourceFactory.getTypeDefinition();
    public static final TypeDefinition Temporal = TemporalFieldValueSourceFactory.getTypeDefinition();

    private final DataTypeFactory factory;

    public TypeDefinition(DataTypeFactory factory) {
        this.factory = factory;
    }

    public DataGeneratorBaseTypes getBaseType() {
        return factory.getUnderlyingDataType();
    }

    FieldValueSource getFieldValueSource(FieldSpec fieldSpec){
        return factory.createValueSource(fieldSpec);
    }

    public boolean canProduceAnyValues(FieldSpec fieldSpec){
        return factory.canProduceAnyValues(fieldSpec);
    }

    @Override
    public int hashCode(){
        return factory.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeDefinition) {
            return factory.getClass().equals(((TypeDefinition) obj).factory.getClass());
        }

        return false;
    }

    public boolean isValid(Object value, FieldSpec fieldSpec) {
        Class dataType = DataGeneratorBaseTypes.getValueClass(getBaseType());

        return dataType.isInstance(value) && factory.isValid(value, fieldSpec);
    }

    @Override
    public String toString(){
        return "Type: " + factory.getClass().getSimpleName();
    }

    public Class<?> getType() {
        return factory.getClass();
    }
}
