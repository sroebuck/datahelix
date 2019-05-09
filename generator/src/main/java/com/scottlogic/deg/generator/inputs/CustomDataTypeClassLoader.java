package com.scottlogic.deg.generator.inputs;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CustomDataTypeClassLoader {
    private static final ClassLoader classLoader = CustomDataTypeClassLoader.class.getClassLoader();

    public static final String customPackageImports = "datahelix.datatypes";
    private static final String moduleExtension = ".jar";
    private final Pattern classSpecPattern = Pattern.compile("^(.+?)(?:\\" + moduleExtension + ")?\\|(.+)$");

    public Class getDataTypeClass(String classSpec, Class requiredImplementation) throws ClassNotFoundException, InvalidProfileException {
        Matcher matcher = classSpecPattern.matcher(classSpec);
        if (!matcher.matches()){
            throw new InvalidProfileException("Custom data type is not in the correct format, it should be in the format: <module-name-or-path>|<className>");
        }

        String moduleName = matcher.group(1);
        String requestedClassName = matcher.group(2);

        Optional<Class> foundClass = getClassesFromJarFile(requestedClassName)
            .filter(requiredImplementation::isAssignableFrom)
            .findFirst();

        return foundClass.orElseThrow(() -> new ClassNotFoundException("Class " + requestedClassName + " could not be found in " + moduleName + moduleExtension));
    }

    private static Stream<Class> getClassesFromJarFile(String requestedClassName){
        return getConfiguredPackageImports()
            .map(packageImport -> {
                String fullClassName = packageImport + "." + requestedClassName;
                return getClass(fullClassName);
            })
            .filter(Objects::nonNull);
    }

    private static Class getClass(String fullClassName) {
        try {
            return classLoader.loadClass(fullClassName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static Stream<String> getConfiguredPackageImports() {
        String property = System.getenv(customPackageImports);
        if (property == null || property.equals("")){
            return Stream.empty();
        }

        return Arrays.stream(property
            .split(";"));
    }

    public String getModuleName(String classSpec) {
        Matcher matcher = classSpecPattern.matcher(classSpec);

        return matcher.matches()
            ? matcher.group(1)
            : null;
    }
}
