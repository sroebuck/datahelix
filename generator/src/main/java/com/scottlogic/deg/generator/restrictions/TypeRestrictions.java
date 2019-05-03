package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.constraints.atomic.IsOfTypeConstraint;

import java.util.Set;

public interface TypeRestrictions {
    TypeRestrictions except(Class... types);

    boolean isTypeAllowed(Class type);

    TypeRestrictions intersect(TypeRestrictions other);

    Set<Class> getAllowedTypes();
}
