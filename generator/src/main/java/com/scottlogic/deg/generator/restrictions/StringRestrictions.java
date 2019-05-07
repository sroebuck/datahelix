package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.generation.StringGenerator;

import java.util.Set;
import java.util.regex.Pattern;

public interface StringRestrictions {
    MergeResult<StringRestrictions> intersect(StringRestrictions other);

    static boolean isString(Object o) {
        return o instanceof String;
    }

    boolean match(String x);

    default boolean match(Object x){
        return isString(x) && match((String)x);
    }

    StringGenerator createGenerator();

    Integer getMinLength();
    Integer getMaxLength();
    Set<Integer> getExcludedLengths();
    Set<Pattern> getMatchingRegex();
    Set<Pattern> getContainingRegex();
    Set<Pattern> getNotMatchingRegex();
    Set<Pattern> getNotContainingRegex();
}


