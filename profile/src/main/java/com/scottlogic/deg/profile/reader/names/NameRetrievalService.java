package com.scottlogic.deg.profile.reader.names;

import com.scottlogic.deg.common.profile.constraints.atomic.NameConstraintTypes;
import com.scottlogic.deg.profile.reader.CatalogService;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.scottlogic.deg.common.profile.constraints.atomic.NameConstraintTypes.FIRST;
import static com.scottlogic.deg.common.profile.constraints.atomic.NameConstraintTypes.LAST;

public class NameRetrievalService implements CatalogService<NameConstraintTypes, NameHolder> {

    private static final String SEP = "/";

    private static final String NAMES_ROOT = "names" + SEP;

    private final String FIRST_MALE_NAMES = NAMES_ROOT + "firstname_male.csv";

    private final String FIRST_FEMALE_NAMES = NAMES_ROOT + "firstname_female.csv";

    private final String LAST_NAMES = NAMES_ROOT + "surname.csv";

    private final Map<NameConstraintTypes, Set<String>> nameTypeMappings;

    private final NamePopulator<InputStream> populator;

    public NameRetrievalService(final NamePopulator<InputStream> populator) {
        this.populator = populator;
        nameTypeMappings = setupNameTypeMappings();
    }

    private Map<NameConstraintTypes, Set<String>> setupNameTypeMappings() {
        Map<NameConstraintTypes, Set<String>> mappings = new EnumMap<>(NameConstraintTypes.class);
        mappings.put(LAST, Stream.of(LAST_NAMES).collect(Collectors.toSet()));
        mappings.put(FIRST, Stream.of(FIRST_MALE_NAMES, FIRST_FEMALE_NAMES).collect(Collectors.toSet()));
        return mappings;
    }

    @Override
    public Set<NameHolder> retrieveValues(NameConstraintTypes configuration) {
        switch (configuration) {
            case FIRST:
            case LAST:
                return generateSingles(nameTypeMappings.get(configuration));
            case FULL:
                return generateCombinations(generateSingles(nameTypeMappings.get(FIRST)),
                    generateSingles(nameTypeMappings.get(LAST)));
            default:
                throw new UnsupportedOperationException("Name not implemented of type: " + configuration);
        }
    }

    private Set<NameHolder> generateSingles(Set<String> sources) {
        return sources.stream()
            .map(this::pathFromClasspath)
            .map(populator::retrieveNames)
            .reduce(new HashSet<>(), this::populateSet);
    }

    private InputStream pathFromClasspath(String classPath) {
        return Optional.ofNullable(this.getClass()
            .getClassLoader()
            .getResourceAsStream(classPath)
        ).orElseThrow(() -> new IllegalArgumentException("Path not found on classpath."));
    }

    private Set<NameHolder> generateCombinations(Set<NameHolder> firstNames,
                                                 Set<NameHolder> lastNames) {
        return firstNames.stream()
            .flatMap(first -> lastNames.stream()
                .map(last -> combineFirstAndLastName(first, last)))
            .collect(Collectors.toSet());
    }

    private NameHolder combineFirstAndLastName(final NameHolder first,
                                               final NameHolder last) {
        final String name = first.getName() + " " + last.getName();
        return new NameHolder(name);
    }

    private Set<NameHolder> populateSet(Set<NameHolder> a, Set<NameHolder> b) {
        a.addAll(b);
        return a;
    }

}
