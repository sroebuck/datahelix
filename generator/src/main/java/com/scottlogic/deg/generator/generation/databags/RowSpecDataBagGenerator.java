package com.scottlogic.deg.generator.generation.databags;
import com.google.inject.Inject;
import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.FieldSpecValueGenerator;
import com.scottlogic.deg.generator.generation.combinationstrategies.CombinationStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class RowSpecDataBagGenerator {
    private final FieldSpecValueGenerator generator;
    private final CombinationStrategy combinationStrategy;

    @Inject
    public RowSpecDataBagGenerator(
        FieldSpecValueGenerator generator,
        CombinationStrategy combinationStrategy)
    {
        this.generator = generator;
        this.combinationStrategy = combinationStrategy;
    }

    public Stream<DataBag> createDataBags(Map<Field, FieldSpec> rowSpec) {
        Stream<Stream<DataBag>> dataBagsForFields =
            rowSpec.entrySet().stream()
                .map(entry ->
                    generator.generate(entry.getValue())
                        .map(value->toDataBag(entry.getKey(), value)));

        return combinationStrategy.permute(dataBagsForFields);
    }

    private DataBag toDataBag(Field field, DataBagValue value) {
        Map<Field, DataBagValue> map = new HashMap<>();
        map.put(field, value);
        return new DataBag(map);
    }
}
