package com.scottlogic.deg.types.faker;

import com.scottlogic.deg.generator.DataGeneratorBaseTypes;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.DataTypeFactory;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.inputs.InvalidProfileException;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Faker implements DataTypeFactory {
    private static final com.github.javafaker.Faker faker = new com.github.javafaker.Faker();
    private final String fakerGroupMethod;
    private final String fakerDataMethod;

    public Faker(String spec) throws InvalidProfileException {
        Matcher matcher = Pattern.compile("^(.+?)\\.(.+)$").matcher(spec);
        if (!matcher.matches()){
            throw new InvalidProfileException("Specification for faker is invalid, it should be in the <group>.<method> format, found `" + spec + "`");
        }

        this.fakerGroupMethod = matcher.group(1);
        this.fakerDataMethod = matcher.group(2);
    }

    public Faker(String fakerGroupMethod, String fakerDataMethod) {
        this.fakerGroupMethod = fakerGroupMethod;
        this.fakerDataMethod = fakerDataMethod;
    }

    @Override
    public FieldValueSource createValueSource(FieldSpec fieldSpec) {
        Object fakerGroup = invokeMethod(faker, fakerGroupMethod);

        return new FakerFieldValueSource(() -> invokeMethod(fakerGroup, fakerDataMethod), fieldSpec);
    }

    private static Object invokeMethod(Object instance, String methodName){
        try {
            return instance.getClass().getDeclaredMethod(methodName).invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
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
        //check to see if the fakerGroup and fakerDataMethod are valid?

        return true; //presume there is nothing that would prevent value generation (maybe check that shorterThan is >= 2?)
    }
}
