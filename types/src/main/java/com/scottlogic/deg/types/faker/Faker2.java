package com.scottlogic.deg.types.faker;

import com.scottlogic.deg.generator.DataGeneratorBaseTypes;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.DataTypeFactory;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;

public class Faker2 implements DataTypeFactory {
    private static final com.github.javafaker.Faker faker = new com.github.javafaker.Faker();
    private final String spec;

    public Faker2(String spec) {
        this.spec = spec;
    }

    @Override
    public FieldValueSource createValueSource(FieldSpec fieldSpec) {
        return new FakerFieldValueSource(() -> faker.expression(spec), fieldSpec);
    }

    @Override
    public DataGeneratorBaseTypes getUnderlyingDataType() {
        return DataGeneratorBaseTypes.STRING;
    }

    @Override
    public boolean isValid(Object value, FieldSpec fieldSpec) {
        return value instanceof String; //TODO: Do something to check that the value matches the expected data, maybe some input regex?
    }

    @Override
    public boolean canProduceAnyValues(FieldSpec fieldSpec) {
        try {
            if (faker.expression(spec) == null){ //check to see if 'spec' is valid
                return false;
            }

            return true;  //presume there is nothing that would prevent value generation (maybe check that shorterThan is >= 2?)
        } catch (Exception e) {
            return false;
        }
    }
}
