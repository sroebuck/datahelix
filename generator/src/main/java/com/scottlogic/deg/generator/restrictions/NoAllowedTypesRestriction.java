package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.generation.TypeDefinition;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class NoAllowedTypesRestriction implements TypeRestrictions {
    public boolean isTypeAllowed(TypeDefinition type) {
        return false;
    }

    public String toString() {
        return "No types allowed";
    }

    public TypeRestrictions intersect(TypeRestrictions other) {
        return this;
    }

    public TypeRestrictions except(TypeDefinition... types) {
        return new DataTypeRestrictions(Arrays.asList(types));
    }

    public Set<TypeDefinition> getAllowedTypes() {
        return Collections.emptySet();
    }

    public int hashCode(){
        return this.getClass().hashCode();
    }

    public boolean equals(Object obj){
        return obj instanceof NoAllowedTypesRestriction;
    }
}
