package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.generation.TypeDefinition;

import java.util.*;
import java.util.stream.Collectors;

public class AnyTypeRestriction implements TypeRestrictions {
    private final static Set<TypeDefinition> allTypes = new HashSet<>(getAllPossibleTypes());

    private static Collection<TypeDefinition> getAllPossibleTypes() {
        //look at the class path for 'types' of data, with some way of identifying them

        return Arrays.asList(
            TypeDefinition.String,
            TypeDefinition.Numeric,
            TypeDefinition.Temporal
        );
    }

    public boolean isTypeAllowed(TypeDefinition type) {
        return true;
    }

    public String toString() {
        return "Any type";
    }

    public TypeRestrictions intersect(TypeRestrictions other) {
        return other;
    }


    public TypeRestrictions except(TypeDefinition... types) {
        if (types.length == 0)
            return this;

        List<TypeDefinition> allowedTypes = allTypes
            .stream()
            .filter(allowedType -> Arrays.stream(types).noneMatch(t -> allowedType.getBaseType().equals(t.getBaseType())))
            .collect(Collectors.toList());

        return new DataTypeRestrictions(allowedTypes);
    }

    public Set<TypeDefinition> getAllowedTypes() {
        return allTypes;
    }

    public int hashCode(){
        return this.getClass().hashCode();
    }

    public boolean equals(Object obj){
        return obj instanceof AnyTypeRestriction;
    }
}
