package com.scottlogic.deg.generator.inputs;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.DataTypeImports;
import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.constraints.*;
import com.scottlogic.deg.generator.constraints.grammatical.AndConstraint;
import com.scottlogic.deg.generator.constraints.grammatical.ConditionalConstraint;
import com.scottlogic.deg.generator.constraints.grammatical.OrConstraint;
import com.scottlogic.deg.schemas.v0_1.ConstraintDTO;

import java.util.Set;

public class MainConstraintReader implements ConstraintReader {
    private final AtomicConstraintReaderLookup atomicConstraintReaderLookup;

    @Inject
    public MainConstraintReader(AtomicConstraintReaderLookup readerLookup) {
        this.atomicConstraintReaderLookup = readerLookup;
    }

    @Override
    public Constraint apply(
        ConstraintDTO dto,
        ProfileFields fields,
        Set<RuleInformation> rules, DataTypeImports imports)
        throws InvalidProfileException {

        if (dto == null) {
            throw new InvalidProfileException("Constraint is null");
        }

        if (dto.is == null) {
            throw new InvalidProfileException("Couldn't recognise 'is' property, it must be set to a value");
        }

        if (dto.is != ConstraintDTO.undefined) {
            ConstraintReader subReader = this.atomicConstraintReaderLookup.getByTypeCode((String) dto.is);

            if (subReader == null) {
                throw new InvalidProfileException("Couldn't recognise constraint type from DTO: " + dto.is);
            }

            try {
                return subReader.apply(dto, fields, rules, imports);
            } catch (IllegalArgumentException e) {
                throw new InvalidProfileException(e.getMessage());
            }
        }

        if (dto.not != null) {
            return this.apply(dto.not, fields, rules, imports).negate();
        }

        if (dto.allOf != null) {
            if (dto.allOf.isEmpty()) {
                throw new InvalidProfileException("AllOf must contain at least one constraint.");
            }
            return new AndConstraint(
                JsonProfileReader.mapDtos(
                    dto.allOf,
                    subConstraintDto -> this.apply(
                        subConstraintDto,
                        fields,
                        rules, imports)));
        }

        if (dto.anyOf != null) {
            return new OrConstraint(
                JsonProfileReader.mapDtos(
                    dto.anyOf,
                    subConstraintDto -> this.apply(
                        subConstraintDto,
                        fields,
                        rules, imports)));
        }

        if (dto.if_ != null) {
            return new ConditionalConstraint(
                this.apply(
                    dto.if_,
                    fields,
                    rules, imports),
                this.apply(
                    dto.then,
                    fields,
                    rules, imports),
                dto.else_ != null
                    ? this.apply(
                        dto.else_,
                        fields,
                        rules, imports)
                    : null);
        }

        throw new InvalidProfileException("Couldn't interpret constraint");
    }
}
