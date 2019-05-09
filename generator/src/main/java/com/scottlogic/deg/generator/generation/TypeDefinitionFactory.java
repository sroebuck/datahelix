package com.scottlogic.deg.generator.generation;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.inputs.CustomDataTypeClassLoader;
import com.scottlogic.deg.generator.inputs.InvalidProfileException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeDefinitionFactory {
    private final CustomDataTypeClassLoader classLoader;

    //TODO: Remove this constructor, use IoC
    public TypeDefinitionFactory() {
        this(new CustomDataTypeClassLoader());
    }

    @Inject
    public TypeDefinitionFactory(CustomDataTypeClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public TypeDefinition createFromDefinition(String typeDef) throws InvalidProfileException {
        Matcher typeDefParsed = Pattern.compile("^(.+?)(?:\\((.+)\\))?$").matcher(typeDef);
        if (!typeDefParsed.matches()) {
            throw new InvalidProfileException("Unrecognised type in type constraint: " + typeDef + "; definition is invalid");
        }

        String typeString = typeDefParsed.group(1);
        String constructorArgs = typeDefParsed.group(2);
        boolean requiresStringConstructor = constructorArgs != null;

        Class<?> factoryClass;
        try {
            factoryClass = classLoader.getDataTypeClass(typeString, DataTypeFactory.class);
        } catch (ClassNotFoundException e) {
            throw new InvalidProfileException(
                java.lang.String.format(
                    "Unrecognised type in type constraint: %s; class cannot be found or does not implement %s\n" +
                        "Check the %s.jar is in the working directory or the directory identified by '%s' (system property)",
                    typeString,
                    DataTypeFactory.class.getName(),
                    classLoader.getModuleName(typeDefParsed.group(1)),
                    CustomDataTypeClassLoader.customPackageImports));
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
            DataTypeFactory factory = (DataTypeFactory) (requiresStringConstructor
                ? constructor.newInstance(constructorArgs)
                : constructor.newInstance());
            return new TypeDefinition(factory);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new InvalidProfileException(
                java.lang.String.format(
                    "Unable to create type provider %s; %s", typeString, e.getMessage()));
        }
    }
}
