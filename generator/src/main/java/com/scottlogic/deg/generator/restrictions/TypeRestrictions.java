package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.generation.TypeDefinition;

import java.util.Set;

public interface TypeRestrictions {
    TypeRestrictions except(TypeDefinition... types);

    boolean isTypeAllowed(TypeDefinition type);

    TypeRestrictions intersect(TypeRestrictions other);

    Set<TypeDefinition> getAllowedTypes();
}
