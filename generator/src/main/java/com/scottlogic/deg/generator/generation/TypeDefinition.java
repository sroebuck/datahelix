package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.DataGeneratorBaseTypes;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.inputs.InvalidProfileException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeDefinition {
    public static final TypeDefinition String = StringFieldValueSourceFactory.getTypeDefinition();
    public static final TypeDefinition Numeric = NumericFieldValueSourceFactory.getTypeDefinition();
    public static final TypeDefinition Temporal = TemporalFieldValueSourceFactory.getTypeDefinition();

    private final FieldValueSourceFactory factory;

    public TypeDefinition(FieldValueSourceFactory factory) {
        this.factory = factory;
    }

    public static TypeDefinition parse(String typeDef) throws InvalidProfileException {
        Matcher typeDefParsed = Pattern.compile("^(.+?)(?:\\:(.+))?$").matcher(typeDef);
        if (!typeDefParsed.matches()) {
            throw new InvalidProfileException("Unrecognised type in type constraint: " + typeDef + "; definition is invalid");
        }

        String typeString = typeDefParsed.group(1);
        String constructorArgs = typeDefParsed.group(2);
        boolean requiresStringConstructor = constructorArgs != null;

        Class factoryClass;
        try {
            factoryClass = Class.forName(typeString);
        } catch (ClassNotFoundException e) {
            throw new InvalidProfileException("Unrecognised type in type constraint: " + typeString + "; class cannot be found");
        }

        if (!FieldValueSourceFactory.class.isAssignableFrom(factoryClass)){
            throw new InvalidProfileException("Invalid type in type constraint: " + typeString + "; class does not implement " + FieldValueSourceFactory.class.getSimpleName());
        }

        Constructor constructor;
        try {
            constructor = requiresStringConstructor
                ? factoryClass.getConstructor(String.class)
                : factoryClass.getConstructor();
        } catch (NoSuchMethodException e) {
            String constructorRequirement = requiresStringConstructor
                ? "a constructor taking a single String argument"
                : "an empty constructor";
            throw new InvalidProfileException("Invalid type in type constraint: " + typeString + "; class does not have " + constructorRequirement);
        }

        try {
            FieldValueSourceFactory factory = (FieldValueSourceFactory) (requiresStringConstructor
                ? constructor.newInstance(constructorArgs)
                : constructor.newInstance());
            return new TypeDefinition(factory);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new InvalidProfileException("Unable to create type provider " + typeString + "; " + e.getMessage());
        }

        throw new InvalidProfileException("Unable to create type provider " + typeString);
    }

    public DataGeneratorBaseTypes getBaseType() {
        return factory.getUnderlyingDataType();
    }

    public FieldValueSource getFieldValueSource(FieldSpec fieldSpec){
        return factory.createValueSource(fieldSpec);
    }

    @Override
    public int hashCode(){
        return factory.getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeDefinition) {
            return factory.getClass().equals(((TypeDefinition) obj).factory.getClass());
        }

        return false;
    }

    public boolean isValid(Object value, FieldSpec fieldSpec) {
        return factory.isValid(value, fieldSpec);
    }

    @Override
    public String toString(){
        return "Type: " + factory.getClass().getSimpleName();
    }

    public Class<?> getType() {
        return factory.getClass();
    }
}
