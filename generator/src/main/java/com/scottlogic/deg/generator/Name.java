package com.scottlogic.deg.generator;

import com.google.common.collect.Sets;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.FieldValueSourceFactory;
import com.scottlogic.deg.generator.generation.fieldvaluesources.CannedValuesFieldValueSource;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.restrictions.StringRestrictions;

import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Name implements FieldValueSourceFactory {
    private final Set<String> names = Sets.newHashSet(
        "James",
        "Arthur",
        "Gertrude",
        "Cecil");

    @Override
    public FieldValueSource createValueSource(FieldSpec fieldSpec) {
        StringRestrictions stringRestrictions = fieldSpec.getStringRestrictions();
        Stream<String> filteredNames = stringRestrictions != null
            ? names.stream().filter(name -> isNamePermitted(name, stringRestrictions))
            : names.stream();

        return new CannedValuesFieldValueSource(filteredNames.map(name -> (Object)name).collect(Collectors.toList()));
    }

    private boolean isNamePermitted(String name, StringRestrictions stringRestrictions) {
        if (stringRestrictions.getMaxLength() != null && name.length() > stringRestrictions.getMaxLength()){
            return false;
        }

        if (stringRestrictions.getMinLength() != null && name.length() < stringRestrictions.getMinLength()){
            return false;
        }

        if (stringRestrictions.getExcludedLengths() != null && stringRestrictions.getExcludedLengths().contains(name.length())){
            return false;
        }

        if (preventValue(stringRestrictions.getContainingRegex(), p -> p.matcher(name).matches())){
            return false;
        }

        if (preventValue(stringRestrictions.getMatchingRegex(), p -> fullStringMatch(p).matcher(name).matches())){
            return false;
        }

        if (preventValue(stringRestrictions.getNotContainingRegex(), p -> !p.matcher(name).matches())){
            return false;
        }

        if (preventValue(stringRestrictions.getNotMatchingRegex(), p -> !fullStringMatch(p).matcher(name).matches())){
            return false;
        }

        return true;
    }

    private static boolean preventValue(Set<Pattern> patterns, Function<Pattern, Boolean> matcher) {
        if (patterns == null){
            return false;
        }

        return !patterns
            .stream()
            .allMatch(matcher::apply);
    }

    private static Pattern fullStringMatch(Pattern pattern){
        return Pattern.compile("^" + pattern.pattern() + "$");
    }

    @Override
    public DataGeneratorBaseTypes getUnderlyingDataType() {
        return DataGeneratorBaseTypes.STRING;
    }

    @Override
    public boolean isValid(Object value, FieldSpec fieldSpec) {
        return value instanceof String && names.contains(value);
    }
}

