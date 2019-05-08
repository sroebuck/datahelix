package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.DataGeneratorBaseTypes;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.inputs.CustomDataTypeClassLoader;
import com.scottlogic.deg.generator.inputs.InvalidProfileException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeDefinition {
    private static final CustomDataTypeClassLoader classLoader = new CustomDataTypeClassLoader(); //TODO: Find some way to DI this.

    public static final TypeDefinition String = StringFieldValueSourceFactory.getTypeDefinition();
    public static final TypeDefinition Numeric = NumericFieldValueSourceFactory.getTypeDefinition();
    public static final TypeDefinition Temporal = TemporalFieldValueSourceFactory.getTypeDefinition();

    private final FieldValueSourceFactory factory;

    public TypeDefinition(FieldValueSourceFactory factory) {
        this.factory = factory;
    }

    public static TypeDefinition parse(String typeDef) throws InvalidProfileException {
        Matcher typeDefParsed = Pattern.compile("^(.+?)(?:\\((.+)\\))?$").matcher(typeDef);
        if (!typeDefParsed.matches()) {
            throw new InvalidProfileException("Unrecognised type in type constraint: " + typeDef + "; definition is invalid");
        }

        String typeString = typeDefParsed.group(1);
        String constructorArgs = typeDefParsed.group(2);
        boolean requiresStringConstructor = constructorArgs != null;

        Class<?> factoryClass;
        try {
            factoryClass = classLoader.getDataTypeClass(typeString, FieldValueSourceFactory.class);
        } catch (ClassNotFoundException e) {
            throw new InvalidProfileException(
                java.lang.String.format(
                    "Unrecognised type in type constraint: %s; class cannot be found or does not implement %s\n" +
                    "Check the %s.jar is in the working directory or the directory identified by '%s' (system property)",
                    typeString,
                    FieldValueSourceFactory.class.getName(),
                    classLoader.getModuleName(typeDefParsed.group(1)),
                    CustomDataTypeClassLoader.customDataTypePathName));
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
            throw new InvalidProfileException(
                java.lang.String.format(
                    "Invalid type in type constraint: %s; class does not have %s", typeString, constructorRequirement));
        }

        try {
            FieldValueSourceFactory factory = (FieldValueSourceFactory) (requiresStringConstructor
                ? constructor.newInstance(constructorArgs)
                : constructor.newInstance());
            return new TypeDefinition(factory);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new InvalidProfileException(
                java.lang.String.format(
                    "Unable to create type provider %s; %s", typeString, e.getMessage()));
        }
    }

    public DataGeneratorBaseTypes getBaseType() {
        return factory.getUnderlyingDataType();
    }

    FieldValueSource getFieldValueSource(FieldSpec fieldSpec){
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
        Class dataType = DataGeneratorBaseTypes.getValueClass(getBaseType());

        return dataType.isInstance(value) && factory.isValid(value, fieldSpec);
    }

    @Override
    public String toString(){
        return "Type: " + factory.getClass().getSimpleName();
    }

    public Class<?> getType() {
        return factory.getClass();
    }
}
