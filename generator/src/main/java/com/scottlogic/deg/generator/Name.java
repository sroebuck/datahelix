package com.scottlogic.deg.generator;

import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.FieldValueSourceFactory;
import com.scottlogic.deg.generator.generation.fieldvaluesources.CannedValuesFieldValueSource;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;

import java.util.Arrays;
import java.util.List;

public class Name implements FieldValueSourceFactory {
    private final List<Object> names = Arrays.asList(
        "James",
        "Arthur",
        "Gertrude",
        "Cecil");

    @Override
    public FieldValueSource createValueSource(FieldSpec fieldSpec) {
        return new CannedValuesFieldValueSource(names);
    }

    @Override
    public Class getUnderlyingDataType() {
        return String.class;
    }

    @Override
    public boolean isValid(Object value, FieldSpec fieldSpec) {
        return names.contains(value);
    }
}
