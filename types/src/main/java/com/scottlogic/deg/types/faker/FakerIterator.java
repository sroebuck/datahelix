package com.scottlogic.deg.types.faker;

import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.restrictions.StringRestrictions;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

class FakerIterator implements Iterator<Object> {
    private final Supplier<Object> getNextValue;
    private final FieldSpec fieldSpec;

    FakerIterator(Supplier<Object> getNextValue, FieldSpec fieldSpec) {
        this.getNextValue = getNextValue;
        this.fieldSpec = fieldSpec;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Object next() {
        String value;

        do {
            value = (String)getNextValue.get();
        } while (!isValuePermitted(value, fieldSpec.getStringRestrictions()));

        return value;
    }

    private boolean isValuePermitted(String value, StringRestrictions stringRestrictions) {
        if (stringRestrictions == null){
            return true;
        }

        if (stringRestrictions.getMaxLength() != null && value.length() > stringRestrictions.getMaxLength()){
            return false;
        }

        if (stringRestrictions.getMinLength() != null && value.length() < stringRestrictions.getMinLength()){
            return false;
        }

        if (stringRestrictions.getExcludedLengths() != null && stringRestrictions.getExcludedLengths().contains(value.length())){
            return false;
        }

        if (preventValue(stringRestrictions.getContainingRegex(), p -> p.matcher(value).matches())){
            return false;
        }

        if (preventValue(stringRestrictions.getMatchingRegex(), p -> fullStringMatch(p).matcher(value).matches())){
            return false;
        }

        if (preventValue(stringRestrictions.getNotContainingRegex(), p -> !p.matcher(value).matches())){
            return false;
        }

        if (preventValue(stringRestrictions.getNotMatchingRegex(), p -> !fullStringMatch(p).matcher(value).matches())){
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
}
