package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.generation.fieldvaluesources.datetime.DateTimeFieldValueSource;
import com.scottlogic.deg.generator.restrictions.DateTimeRestrictions;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TemporalFieldValueSourceFactory implements FieldValueSourceFactory {
    public static TypeDefinition getTypeDefinition() {
        return new TypeDefinition(new TemporalFieldValueSourceFactory());
    }

    @Override
    public FieldValueSource createValueSource(FieldSpec fieldSpec) {
        DateTimeRestrictions restrictions = fieldSpec.getDateTimeRestrictions();

        return new DateTimeFieldValueSource(
            restrictions != null ? restrictions : new DateTimeRestrictions(),
            getBlacklist(fieldSpec));
    }

    private static Set<Object> getBlacklist(FieldSpec fieldSpec) {
        if (fieldSpec.getSetRestrictions() == null)
            return Collections.emptySet();

        return new HashSet<>(fieldSpec.getSetRestrictions().getBlacklist());
    }

    @Override
    public Class getUnderlyingDataType() {
        return OffsetDateTime.class;
    }
}
