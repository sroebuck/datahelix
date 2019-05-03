package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.constraints.atomic.IsOfTypeConstraint;

import java.util.*;

public class NoAllowedTypesRestriction implements TypeRestrictions {
    public boolean isTypeAllowed(Class type) {
        return false;
    }

    public String toString() {
        return "No types allowed";
    }

    public TypeRestrictions intersect(TypeRestrictions other) {
        return this;
    }

    public TypeRestrictions except(Class... types) {
        return new DataTypeRestrictions(Arrays.asList(types));
    }

    public Set<Class> getAllowedTypes() {
        return Collections.emptySet();
    }

    public int hashCode(){
        return this.getClass().hashCode();
    }

    public boolean equals(Object obj){
        return obj instanceof NoAllowedTypesRestriction;
    }
}
