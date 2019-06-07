package com.scottlogic.deg.generator.fieldspecs;

import com.google.inject.Inject;
import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.ProfileFields;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RowSpecMerger {
    private final FieldSpecMerger fieldSpecMerger;

    @Inject
    public RowSpecMerger(FieldSpecMerger fieldSpecMerger) {
        this.fieldSpecMerger = fieldSpecMerger;
    }

    public Optional<Map<Field, FieldSpec>> merge(Map<Field, FieldSpec> rowSpec1, Map<Field, FieldSpec> rowSpec2) {

        Map<Field, FieldSpec> merged = new HashMap<>(rowSpec1);

        for (Map.Entry<Field, FieldSpec> entry : rowSpec2.entrySet()) {
            if (!merged.containsKey(entry.getKey())){
                merged.put(entry.getKey(), entry.getValue());
            }
            Optional<FieldSpec> merge = fieldSpecMerger.merge(entry.getValue(), merged.get(entry.getKey()));
            if (!merge.isPresent()){
                return Optional.empty();
            }
            merged.put(entry.getKey(), merge.get());
        }

        return Optional.of(merged);
    }
}
