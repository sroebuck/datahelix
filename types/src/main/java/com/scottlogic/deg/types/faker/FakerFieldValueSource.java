package com.scottlogic.deg.types.faker;

import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.restrictions.StringRestrictions;
import com.scottlogic.deg.generator.utils.RandomNumberGenerator;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Must be public so it can be used by the generator when it is loaded via 'reflection'
 */
public class FakerFieldValueSource implements FieldValueSource {
    private final Supplier<Object> getNextValue;
    private final FieldSpec fieldSpec;

    public FakerFieldValueSource(Supplier<Object> getNextValue, FieldSpec fieldSpec) {
        this.getNextValue = getNextValue;
        this.fieldSpec = fieldSpec;
    }

    @Override
    public boolean isFinite() {
        return false;
    }

    @Override
    public long getValueCount() {
        return Long.MAX_VALUE;
    }

    @Override
    public Iterable<Object> generateInterestingValues() {
        return Collections.singletonList(getNextValue.get());
    }

    @Override
    public Iterable<Object> generateAllValues() {
        return () -> new FakerIterator(getNextValue, fieldSpec);
    }

    @Override
    public Iterable<Object> generateRandomValues(RandomNumberGenerator randomNumberGenerator) {
        return generateAllValues(); //presume that Faker is random
    }
}
