package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.generation.fieldvaluesources.RealNumberFieldValueSource;
import com.scottlogic.deg.generator.restrictions.NumericRestrictions;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NumericFieldValueSourceFactory implements FieldValueSourceFactory {
    public static TypeDefinition getTypeDefinition() {
        return new TypeDefinition(new NumericFieldValueSourceFactory());
    }

    @Override
    public FieldValueSource createValueSource(FieldSpec fieldSpec) {
        NumericRestrictions restrictions = fieldSpec.getNumericRestrictions() == null
            ? new NumericRestrictions()
            : fieldSpec.getNumericRestrictions();

        return new RealNumberFieldValueSource(
            restrictions,
            getBlacklist(fieldSpec));
    }

    private static Set<Object> getBlacklist(FieldSpec fieldSpec) {
        if (fieldSpec.getSetRestrictions() == null)
            return Collections.emptySet();

        return new HashSet<>(fieldSpec.getSetRestrictions().getBlacklist());
    }

    @Override
    public Class getUnderlyingDataType() {
        return BigDecimal.class;
    }
}
