package com.scottlogic.deg.generator.inputs;

import com.scottlogic.deg.generator.FlatMappingSpliterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CustomDataTypeClassLoader {
    public static final String customDataTypePathName = "datahelix.datatypes";
    private final Pattern classSpecPattern = Pattern.compile("^(.+?)(?:\\.jar)?\\|(.+)$");

    public Class getDataTypeClass(String name, Class requiredImplementation) throws ClassNotFoundException, InvalidProfileException {
        Matcher matcher = classSpecPattern.matcher(name);
        if (!matcher.matches()){
            throw new InvalidProfileException("Custom data type is not in the correct format, it should be in the format: <jar-file-path>|<className>");
        }

        String moduleName = matcher.group(1);
        String className = matcher.group(2);

        Optional<Class> foundClass = getClassesFromJarFile(moduleName, javaEntryClassName -> javaEntryClassName.endsWith(className))
            .filter(requiredImplementation::isAssignableFrom)
            .findFirst();

        return foundClass.orElseThrow(() -> new ClassNotFoundException("Class " + name + " could not be found in " + moduleName + ".jar"));
    }

    private static Stream<Class> getClassesFromJarFile(String moduleName, Function<String, Boolean> classNamePredicate){
        try {
            Stream<JarFile> jarFiles = getJarFiles(moduleName);
            return FlatMappingSpliterator.flatMap(
                jarFiles,
                jarFile -> {
                    try {
                        URL[] urls = {new URL("jar:file:" + jarFile.getName() + ".jar!/")};
                        URLClassLoader cl = URLClassLoader.newInstance(urls);

                        return new EntriesIterator(jarFile.entries()).asStream()
                            .filter(jarEntry -> !jarEntry.isDirectory() && jarEntry.getName().endsWith(".class"))
                            .map(jarEntry -> {
                                String className = jarEntry.getName().substring(0, jarEntry.getName().length() - ".class".length());
                                return className.replace('/', '.');
                            })
                            .filter(classNamePredicate::apply)
                            .map(className -> {
                                try {
                                    return cl.loadClass(className);
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    } catch (MalformedURLException exc){
                        throw new RuntimeException(exc);
                    }
                });
        }
        catch (IOException exc){
            throw new RuntimeException(exc);
        }
    }

    private static Stream<JarFile> getJarFiles(String moduleName) throws IOException {
        boolean absolutePath = moduleName.contains(":");
        if (absolutePath){
            if (!Files.exists(Paths.get(moduleName + ".jar"))){
                throw new FileNotFoundException(String.format("Requested module %s could not be found", moduleName));
            }

            return Stream.of(new JarFile(moduleName + ".jar"));
        }

        Stream<String> dirs = Stream.concat(
            Stream.of(System.getProperty("user.dir")),
            getConfiguredPaths());

        return dirs
            .filter(Objects::nonNull)
            .map(dir -> Paths.get(dir, moduleName + ".jar").toFile())
            .filter(File::exists)
            .map(file -> {
                try {
                    return new JarFile(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    private static Stream<String> getConfiguredPaths() {
        String property = System.getenv(customDataTypePathName);
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

    private static class EntriesIterator implements Iterator<JarEntry> {
        private final Enumeration<JarEntry> entries;

        EntriesIterator(Enumeration<JarEntry> entries) {
            this.entries = entries;
        }

        @Override
        public boolean hasNext() {
            return entries.hasMoreElements();
        }

        @Override
        public JarEntry next() {
            return entries.nextElement();
        }

        Stream<JarEntry> asStream(){
            return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED),
                false
            );
        }
    }
}
