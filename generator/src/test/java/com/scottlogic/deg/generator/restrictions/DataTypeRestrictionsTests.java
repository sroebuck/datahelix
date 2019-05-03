package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.constraints.atomic.IsOfTypeConstraint;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.*;

class DataTypeRestrictionsTests {
    @Test
    public void except_withAlreadyExcludedType_shouldReturnSameCollectionOfPermittedTypes(){
        TypeRestrictions exceptStrings = DataTypeRestrictions.createFromWhiteList(
            BigDecimal.class,
            OffsetDateTime.class);

        TypeRestrictions result = exceptStrings.except(String.class);

        Assert.assertThat(result.getAllowedTypes(), containsInAnyOrder(
            BigDecimal.class,
            OffsetDateTime.class));
    }

    @Test
    public void except_withNoTypes_shouldReturnSameCollectionOfPermittedTypes(){
        TypeRestrictions exceptStrings = DataTypeRestrictions.createFromWhiteList(
            BigDecimal.class,
            OffsetDateTime.class);

        TypeRestrictions result = exceptStrings.except();

        Assert.assertThat(result, sameInstance(exceptStrings));
    }

    @Test
    public void except_withPermittedType_shouldReturnSameCollectionExcludingGivenType(){
        TypeRestrictions exceptStrings = DataTypeRestrictions.createFromWhiteList(
            BigDecimal.class,
            OffsetDateTime.class);

        TypeRestrictions result = exceptStrings.except(BigDecimal.class);

        Assert.assertThat(result.getAllowedTypes(), containsInAnyOrder(
            OffsetDateTime.class));
    }

    @Test
    public void except_withLastPermittedType_shouldReturnNoTypesPermitted(){
        TypeRestrictions exceptStrings = DataTypeRestrictions.createFromWhiteList(
            OffsetDateTime.class);

        TypeRestrictions result = exceptStrings.except(OffsetDateTime.class);

        Assert.assertThat(result.getAllowedTypes(), empty());
    }
}