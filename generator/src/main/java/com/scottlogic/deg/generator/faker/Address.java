package com.scottlogic.deg.generator.faker;

import com.github.javafaker.Faker;
import com.scottlogic.deg.generator.DataGeneratorBaseTypes;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.FieldValueSourceFactory;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;

public class Address implements FieldValueSourceFactory {
    private static final Faker faker = new Faker();

    @Override
    public FieldValueSource createValueSource(FieldSpec fieldSpec) {
        return new FakerFieldValueSource(() -> faker.address().streetAddress()); //TODO: ensure the value meets the other FieldSpec requirements
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
