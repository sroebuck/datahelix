package com.scottlogic.deg.generator.fieldspecs;

import com.google.inject.Inject;
import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.ProfileFields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RowSpecMerger {
    private final FieldSpecMerger fieldSpecMerger;

    @Inject
    public RowSpecMerger(FieldSpecMerger fieldSpecMerger) {
        this.fieldSpecMerger = fieldSpecMerger;
    }

    public Optional<Map<Field, FieldSpec>> merge(Collection<Map<Field, FieldSpec>> rowSpecs) {
        if (rowSpecs.isEmpty()) {
            throw new UnsupportedOperationException();
        }

        final ProfileFields fields = new ProfileFields(new ArrayList<>(rowSpecs.iterator().next().keySet()));

        final Map<Field, Optional<FieldSpec>> fieldToFieldSpec = fields
            .stream()
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    field -> rowSpecs
                        .stream()
                        .map(x -> x.get(field))
                        .reduce(
                            Optional.of(FieldSpec.Empty),
                            (acc, next) -> acc.flatMap(fieldSpec -> fieldSpecMerger.merge(fieldSpec, next)),
                            (opt1, opt2) -> opt1.flatMap(
                                fieldSpec1 -> opt2.flatMap(
                                    fieldSpec2 -> fieldSpecMerger.merge(fieldSpec1, fieldSpec2)
                                )
                            )
                        )
                )
            );

        return Optional.of(fieldToFieldSpec)
            .filter(map -> map.values().stream().allMatch(Optional::isPresent))
            .map(map -> map
                .entrySet()
                .stream()
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get()
                    )
                )
            );

    }
}
