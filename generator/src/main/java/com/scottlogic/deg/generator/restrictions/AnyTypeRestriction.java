package com.scottlogic.deg.generator.restrictions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

public class AnyTypeRestriction implements TypeRestrictions {
    private final static Set<Class> allTypes = new HashSet<>(getAllPossibleTypes());

    private static Collection<Class> getAllPossibleTypes() {
        //look at the class path for 'types' of data, with some way of identifying them

        return Arrays.asList(
            String.class,
            BigDecimal.class,
            OffsetDateTime.class
        );
    }

    public boolean isTypeAllowed(Class type) {
        return true;
    }

    public String toString() {
        return "Any type";
    }

    public TypeRestrictions intersect(TypeRestrictions other) {
        return other;
    }


    public TypeRestrictions except(Class... types) {
        if (types.length == 0)
            return this;

        ArrayList<Class> allowedTypes = new ArrayList<>(allTypes);
        allowedTypes.removeAll(Arrays.asList(types));

        return new DataTypeRestrictions(allowedTypes);
    }

    public Set<Class> getAllowedTypes() {
        return allTypes;
    }

    public int hashCode(){
        return this.getClass().hashCode();
    }

    public boolean equals(Object obj){
        return obj instanceof AnyTypeRestriction;
    }
}
