package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.generation.NoStringsStringGenerator;
import com.scottlogic.deg.generator.generation.StringGenerator;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Represents a set of string restrictions that could never produce and values
 */
public class NoStringsPossibleStringRestrictions implements StringRestrictions {
    private final String reason;

    NoStringsPossibleStringRestrictions(String reason) {
        this.reason = reason;
    }

    @Override
    public MergeResult<StringRestrictions> intersect(StringRestrictions other) {
        return new MergeResult<>(this);
    }

    public String toString(){
        return String.format("No strings can be generated: %s", reason);
    }

    @Override
    public boolean match(String x) {
        return false;
    }

    @Override
    public StringGenerator createGenerator() {
        return new NoStringsStringGenerator(reason);
    }

    @Override
    public Integer getMinLength() {
        return 0;
    }

    @Override
    public Integer getMaxLength() {
        return 0;
    }

    @Override
    public Set<Integer> getExcludedLengths() {
        return Collections.emptySet();
    }

    @Override
    public Set<Pattern> getMatchingRegex() {
        return Collections.emptySet();
    }

    @Override
    public Set<Pattern> getContainingRegex() {
        return Collections.emptySet();
    }

    @Override
    public Set<Pattern> getNotMatchingRegex() {
        return Collections.emptySet();
    }

    @Override
    public Set<Pattern> getNotContainingRegex() {
        return Collections.emptySet();
    }
}
