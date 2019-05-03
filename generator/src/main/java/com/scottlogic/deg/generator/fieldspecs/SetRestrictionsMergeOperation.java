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

            filterStream = restrictSetValues(merging, filterStream, typeRestrictions);

            Set<Object> whitelist = filterStream.collect(Collectors.toCollection(HashSet::new));
            SetRestrictions newSetRestrictions = new SetRestrictions(whitelist,
                setRestrictions.getBlacklist());

            setRestrictions = newSetRestrictions;
        }

        return Optional.of(merging.withSetRestrictions(
            setRestrictions,
            FieldSpecSource.fromFieldSpecs(left, right)));
    }

    private Stream<?> restrictSetValues(FieldSpec fieldSpec, Stream<?> values, TypeRestrictions typeRestrictions) {
        Set<TypeDefinition> allowedTypes = typeRestrictions.getAllowedTypes();

        return values.filter(value ->
            allowedTypes.stream().anyMatch(td -> td.getType().isInstance(value) && td.isValid(value, fieldSpec)));
    }
}

