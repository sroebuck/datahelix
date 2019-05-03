package com.scottlogic.deg.generator.fieldspecs;

import com.scottlogic.deg.generator.generation.TypeDefinition;
import com.scottlogic.deg.generator.restrictions.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetRestrictionsMergeOperation implements RestrictionMergeOperation {
    private static final SetRestrictionsMerger setRestrictionsMerger = new SetRestrictionsMerger();

    @Override
    public Optional<FieldSpec> applyMergeOperation(FieldSpec left, FieldSpec right, FieldSpec merging) {
        MergeResult<SetRestrictions> mergeResult =
            setRestrictionsMerger.merge(left.getSetRestrictions(), right.getSetRestrictions());

        if (!mergeResult.successful){
            return Optional.of(merging.withSetRestrictions(
                SetRestrictions.fromWhitelist(Collections.emptySet()),
                FieldSpecSource.fromFieldSpecs(left, right)));
        }

        SetRestrictions setRestrictions = mergeResult.restrictions;

        // Filter the set to match any new restrictions
        if (setRestrictions != null &&
            setRestrictions.getWhitelist() != null &&
            !setRestrictions.getWhitelist().isEmpty()) {

            Stream<?> filterStream = setRestrictions.getWhitelist().stream();
            TypeRestrictions typeRestrictions = merging.getTypeRestrictions();

            /* use the type-factory to do this filtering */
            filterStream = typeRestrictions.getAllowedTypes()
                .stream()
                .reduce(
                    filterStream,
                    (values, typeDefinition) -> removeInvalidValues(merging, values, typeDefinition),
                    (prev, td) -> null);

            Set<Object> whitelist = filterStream.collect(Collectors.toCollection(HashSet::new));
            SetRestrictions newSetRestrictions = new SetRestrictions(whitelist,
                setRestrictions.getBlacklist());

            setRestrictions = newSetRestrictions;
        }

        return Optional.of(merging.withSetRestrictions(
            setRestrictions,
            FieldSpecSource.fromFieldSpecs(left, right)));
    }

    private <T> Stream<T> removeInvalidValues(FieldSpec fieldSpec, Stream<T> prev, TypeDefinition td) {
        return prev
            .filter(value -> td.getType().isInstance(value))
            .filter(value -> valueMatches(fieldSpec, td, value));
    }

    private <T> boolean valueMatches(FieldSpec fieldSpec, TypeDefinition typeDefinition, T value) {
        StringRestrictions stringRestrictions = fieldSpec.getStringRestrictions();

        //typeof(value) will be the same as typeDefinition.getType()

        //either ask the typeDefinition if the value matches
        //or eject values based on their string restrictions

        if (typeDefinition.getType() == TypeDefinition.String.getType() && stringRestrictions != null){
            return stringRestrictions.match(value);
        }

        NumericRestrictions numericRestrictions = fieldSpec.getNumericRestrictions();
        if (typeDefinition.getType() == TypeDefinition.Numeric.getType() && numericRestrictions != null){
            return numericRestrictions.match(value);
        }

        DateTimeRestrictions temporalRestrictions = fieldSpec.getDateTimeRestrictions();
        if (typeDefinition.getType() == TypeDefinition.Temporal.getType() && temporalRestrictions != null){
            return temporalRestrictions.match(value);
        }

        return true;
    }
}

