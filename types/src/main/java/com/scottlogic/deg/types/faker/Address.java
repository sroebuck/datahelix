package com.scottlogic.deg.types.faker;

import com.github.javafaker.Faker;
import com.scottlogic.deg.generator.DataGeneratorBaseTypes;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.DataTypeFactory;
import com.scottlogic.deg.generator.generation.fieldvaluesources.CannedValuesFieldValueSource;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;

public class Address implements DataTypeFactory {
    private static final Faker faker = new Faker();

    @Override
    public FieldValueSource createValueSource(FieldSpec fieldSpec) {
        if (fieldSpec.getStringRestrictions().getMaxLength() <= 10){
            return CannedValuesFieldValueSource.of(); //TODO: Yield some information about the inability to produce data
        }

        return new FakerFieldValueSource(() -> faker.address().streetAddress(), fieldSpec); //TODO: ensure the value meets the other FieldSpec requirements
    }

    @Override
    public DataGeneratorBaseTypes getUnderlyingDataType() {
        return DataGeneratorBaseTypes.STRING;
    }

    @Override
    public boolean isValid(Object value, FieldSpec fieldSpec) {
        return value instanceof String && valueIsAnAddress((String) value);
    }

    private boolean valueIsAnAddress(String value) {
        return value.matches("^\\d+\\s+[A-Z][a-z]+\\s+[A-Z][a-z]+$");
    }
}
