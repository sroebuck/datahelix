package com.scottlogic.deg.generator.inputs;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.DataTypeImports;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class CustomDataTypeClassLoader {
    private final ClassLoader classLoader;

    @Inject
    public CustomDataTypeClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Class getDataTypeClass(String className, Class requiredImplementation, DataTypeImports imports) throws ClassNotFoundException {
        Optional<Class> foundClass = getClassesFromImportedPackages(className, imports)
            .filter(requiredImplementation::isAssignableFrom)
            .findFirst();

        return foundClass.orElseThrow(() -> new ClassNotFoundException("Class " + className + " could not be found"));
    }

    private Stream<Class> getClassesFromImportedPackages(String requestedClassName, DataTypeImports imports){
        return imports.getImports().stream()
            .map(packageImport -> {
                String fullClassName = packageImport + "." + requestedClassName;
                return getClass(fullClassName);
            })
            .filter(Objects::nonNull);
    }

    private Class getClass(String fullClassName) {
        try {
            return classLoader.loadClass(fullClassName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
