package com.scottlogic.deg.generator;

import java.time.OffsetDateTime;

public enum DataGeneratorBaseTypes{
    NUMERIC,
    STRING,
    TEMPORAL;

    public static Class getValueClass(DataGeneratorBaseTypes baseType){
        if (baseType.equals(DataGeneratorBaseTypes.STRING)){
            return String.class;
        }
        if (baseType.equals(DataGeneratorBaseTypes.NUMERIC)){
            return Number.class;
        }
        if (baseType.equals(DataGeneratorBaseTypes.TEMPORAL)){
            return OffsetDateTime.class;
        }

        throw new RuntimeException("Invalid base type for type definition");
    }
}
