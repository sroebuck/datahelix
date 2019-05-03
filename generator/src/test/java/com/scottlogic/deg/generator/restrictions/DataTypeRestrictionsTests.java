package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.generation.TypeDefinition;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class DataTypeRestrictionsTests {
    @Test
    public void except_withAlreadyExcludedType_shouldReturnSameCollectionOfPermittedTypes(){
        TypeRestrictions exceptStrings = DataTypeRestrictions.createFromWhiteList(
            TypeDefinition.Numeric,
            TypeDefinition.Temporal);

        TypeRestrictions result = exceptStrings.except(TypeDefinition.String);

        Assert.assertThat(result.getAllowedTypes(), containsInAnyOrder(
            TypeDefinition.Numeric,
            TypeDefinition.Temporal));
    }

    @Test
    public void except_withNoTypes_shouldReturnSameCollectionOfPermittedTypes(){
        TypeRestrictions exceptStrings = DataTypeRestrictions.createFromWhiteList(
            TypeDefinition.Numeric,
            TypeDefinition.Temporal);

        TypeRestrictions result = exceptStrings.except();

        Assert.assertThat(result, sameInstance(exceptStrings));
    }

    @Test
    public void except_withPermittedType_shouldReturnSameCollectionExcludingGivenType(){
        TypeRestrictions exceptStrings = DataTypeRestrictions.createFromWhiteList(
            TypeDefinition.Numeric,
            TypeDefinition.Temporal);

        TypeRestrictions result = exceptStrings.except(TypeDefinition.Numeric);

        Assert.assertThat(result.getAllowedTypes(), containsInAnyOrder(
            TypeDefinition.Temporal));
    }

    @Test
    public void except_withLastPermittedType_shouldReturnNoTypesPermitted(){
        TypeRestrictions exceptStrings = DataTypeRestrictions.createFromWhiteList(
            TypeDefinition.Temporal);

        TypeRestrictions result = exceptStrings.except(TypeDefinition.Temporal);

        Assert.assertThat(result.getAllowedTypes(), empty());
    }
}