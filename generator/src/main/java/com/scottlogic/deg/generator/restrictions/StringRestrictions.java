package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.common.profile.constraints.atomic.IsOfTypeConstraint;
import com.scottlogic.deg.generator.generation.StringGenerator;

public interface StringRestrictions extends TypedRestrictions {
    MergeResult<StringRestrictions> intersect(StringRestrictions other);

    @Override
    default boolean isCorrectType(Object o) {
        return IsOfTypeConstraint.Types.STRING.isInstanceOf(o);
    }

    boolean match(String x);

    @Override
    default boolean match(Object x) {
        return isCorrectType(x) && match((String) x);
    }

    StringGenerator createGenerator();
}


