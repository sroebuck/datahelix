package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.CannedValuesFieldValueSource;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.generation.fieldvaluesources.RealNumberFieldValueSource;
import com.scottlogic.deg.generator.generation.fieldvaluesources.datetime.DateTimeFieldValueSource;
import com.scottlogic.deg.generator.restrictions.DateTimeRestrictions;
import com.scottlogic.deg.generator.restrictions.NumericRestrictions;
import com.scottlogic.deg.generator.restrictions.StringRestrictions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class TypeDefinition {
    public static TypeDefinition String = new TypeDefinition(String.class, TypeDefinition::createStrings);
    public static TypeDefinition Numeric = new TypeDefinition(BigDecimal.class, TypeDefinition::createNumerics);
    public static TypeDefinition Temporal = new TypeDefinition(OffsetDateTime.class, TypeDefinition::createTemporals);

    private final Class type;
    private final Function<FieldSpec, FieldValueSource> factory;

    public TypeDefinition(Class type, Function<FieldSpec, FieldValueSource> factory) {
        this.type = type;
        this.factory = factory;
    }

    public Class getType() {
        return type;
    }

    public FieldValueSource getFieldValueSource(FieldSpec fieldSpec){
        return factory.apply(fieldSpec);
    }

    @Override
    public int hashCode(){
        return type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeDefinition) {
            return type.equals(((TypeDefinition) obj).type);
        }

        return false;
    }

    /*TODO: Move these into factories*/
    private static FieldValueSource createStrings(FieldSpec fieldSpec){
        StringRestrictions stringRestrictions = fieldSpec.getStringRestrictions();

        if (stringRestrictions == null) {
            return new CannedValuesFieldValueSource(Collections.emptyList());
        }

        Set<Object> blacklist = getBlacklist(fieldSpec);

        StringGenerator generator = stringRestrictions.createGenerator();
        if (blacklist.size() > 0) {
            RegexStringGenerator blacklistGenerator = RegexStringGenerator.createFromBlacklist(blacklist);

            generator = generator.intersect(blacklistGenerator);
        }

        return generator.asFieldValueSource();
    }

    private static FieldValueSource createNumerics(FieldSpec fieldSpec) {
        NumericRestrictions restrictions = fieldSpec.getNumericRestrictions() == null
            ? new NumericRestrictions()
            : fieldSpec.getNumericRestrictions();

        return new RealNumberFieldValueSource(
            restrictions,
            getBlacklist(fieldSpec));
    }

    private static FieldValueSource createTemporals(FieldSpec fieldSpec) {
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
}
