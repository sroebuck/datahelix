package com.scottlogic.deg.generator.reducer;

import com.scottlogic.deg.common.profile.constraints.atomic.*;
import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.ProfileFields;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecFactory;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecMerger;
import com.scottlogic.deg.generator.fieldspecs.RowSpec;
import com.scottlogic.deg.common.profile.RuleInformation;
import com.scottlogic.deg.generator.restrictions.SetRestrictions;
import com.scottlogic.deg.generator.restrictions.StringRestrictionsFactory;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;

class ConstraintReducerTest {

    private final ConstraintReducer constraintReducer = new ConstraintReducer(
        new FieldSpecFactory(new StringRestrictionsFactory()),
        new FieldSpecMerger()
    );

    @Test
    void shouldProduceCorrectFieldSpecsForExample() {
        // ARRANGE
        final Field quantityField = new Field("quantity");
        final Field countryField = new Field("country");
        final Field cityField = new Field("city");

        ProfileFields fieldList = new ProfileFields(
            Arrays.asList(quantityField, countryField, cityField));

        final Set<Object> countryAmong = new HashSet<>(Arrays.asList("UK", "US"));

        final List<AtomicConstraint> constraints = Arrays.asList(
            new IsGreaterThanConstantConstraint(quantityField, 0, rules()),
            new IsGreaterThanConstantConstraint(quantityField, 5, rules()).negate(),
            new IsInSetConstraint(countryField, countryAmong, rules()),
            new IsOfTypeConstraint(cityField, IsOfTypeConstraint.Types.STRING, rules()));

        // ACT
        final RowSpec reducedConstraints = constraintReducer.reduceConstraintsToRowSpec(
            fieldList,
            constraints).get();

        // ASSERT
        FieldSpec quantityFieldSpec = reducedConstraints.getSpecForField(quantityField);
        Assert.assertThat("Quantity fieldspec has no set restrictions", quantityFieldSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Quantity fieldspec has no string restrictions", quantityFieldSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Quantity fieldspec has no null restrictions", quantityFieldSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
            quantityFieldSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Quantity fieldspec has no datetime restrictions",
            quantityFieldSpec.getDateTimeRestrictions(), Is.is(IsNull.nullValue()));
        Assert.assertThat("Quantity fieldspec has numeric restrictions", quantityFieldSpec.getNumericRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Quantity fieldspec has correct lower bound limit",
            quantityFieldSpec.getNumericRestrictions().min.getLimit(), Is.is(BigDecimal.ZERO));
        Assert.assertThat("Quantity fieldspec has exclusive lower bound",
            quantityFieldSpec.getNumericRestrictions().min.isInclusive(), Is.is(false));
        Assert.assertThat("Quantity fieldspec has correct upper bound limit",
            quantityFieldSpec.getNumericRestrictions().max.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Quantity fieldspec has inclusive upper bound",
            quantityFieldSpec.getNumericRestrictions().max.isInclusive(), Is.is(true));

        FieldSpec countryFieldSpec = reducedConstraints.getSpecForField(countryField);
        Assert.assertThat("Country fieldspec has no string restrictions", countryFieldSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Country fieldspec has no null restrictions", countryFieldSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
            quantityFieldSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Country fieldspec has no datetime restrictions",
            countryFieldSpec.getDateTimeRestrictions(), Is.is(IsNull.nullValue()));
        Assert.assertThat("Country fieldspec has no numeric restrictions",
            countryFieldSpec.getNumericRestrictions(), Is.is(IsNull.nullValue()));
        Assert.assertThat("Country fieldspec has set restrictions", countryFieldSpec.getSetRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Country fieldspec set restrictions have no blacklist",
            countryFieldSpec.getBlacklistRestrictions(), nullValue());
        Assert.assertThat("Country fieldspec set restrictions have whitelist",
            countryFieldSpec.getSetRestrictions().getWhitelist(), notNullValue());
        Assert.assertThat("Country fieldspec set restrictions whitelist has correct size",
            countryFieldSpec.getSetRestrictions().getWhitelist().size(), Is.is(2));
        Assert.assertThat("Country fieldspec set restrictions whitelist contains 'UK'",
            countryFieldSpec.getSetRestrictions().getWhitelist().contains("UK"), Is.is(true));
        Assert.assertThat("Country fieldspec set restrictions whitelist contains 'US'",
            countryFieldSpec.getSetRestrictions().getWhitelist().contains("US"), Is.is(true));

        FieldSpec cityFieldSpec = reducedConstraints.getSpecForField(cityField);
        Assert.assertThat("City fieldspec has no set restrictions", cityFieldSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("City fieldspec has no string restrictions", cityFieldSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("City fieldspec has no null restrictions", cityFieldSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("City fieldspec has no datetime restrictions", cityFieldSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("City fieldspec has no numeric restrictions", cityFieldSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("City fieldspec has type restrictions", cityFieldSpec.getTypeRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat(
            "City fieldspec has string type restriction",
            cityFieldSpec.getTypeRestrictions().getAllowedTypes(),
            IsEqual.equalTo(Collections.singleton(IsOfTypeConstraint.Types.STRING)));
    }

    @Test
    void shouldReduceIsGreaterThanConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsGreaterThanConstantConstraint(field, 5, rules()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a numeric type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no upper bound", outputSpec.getNumericRestrictions().max,
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have lower bound", outputSpec.getNumericRestrictions().min,
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restriction lower bound is correct",
            outputSpec.getNumericRestrictions().min.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restriction lower bound is exclusive",
            outputSpec.getNumericRestrictions().min.isInclusive(), Is.is(false));
    }

    @Test
    void shouldReduceNegatedIsGreaterThanConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsGreaterThanConstantConstraint(field, 5, rules()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no lower bound",
            outputSpec.getNumericRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have upper bound",
            outputSpec.getNumericRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions upper bound limit is correct",
            outputSpec.getNumericRestrictions().max.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric resitrctions upper bound is inclusive",
            outputSpec.getNumericRestrictions().max.isInclusive(), Is.is(true));
    }

    @Test
    void shouldReduceIsGreaterThanOrEqualToConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsGreaterThanOrEqualToConstantConstraint(field, 5, rules()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a numeric type restriction", outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no upper bound", outputSpec.getNumericRestrictions().max,
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have lower bound", outputSpec.getNumericRestrictions().min,
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restriction lower bound is correct",
            outputSpec.getNumericRestrictions().min.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restriction lower bound is inclusive",
            outputSpec.getNumericRestrictions().min.isInclusive(), Is.is(true));
    }

    @Test
    void shouldReduceNegatedIsGreaterThanOrEqualToConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsGreaterThanOrEqualToConstantConstraint(field, 5, rules()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no lower bound",
            outputSpec.getNumericRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have upper bound",
            outputSpec.getNumericRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions upper bound limit is correct",
            outputSpec.getNumericRestrictions().max.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restrictions upper bound is exclusive",
            outputSpec.getNumericRestrictions().max.isInclusive(), Is.is(false));
    }

    @Test
    void shouldReduceIsLessThanConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsLessThanConstantConstraint(field, 5, rules()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no lower bound",
            outputSpec.getNumericRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have upper bound",
            outputSpec.getNumericRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions upper bound limit is correct",
            outputSpec.getNumericRestrictions().max.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restrictions upper bound is exclusive",
            outputSpec.getNumericRestrictions().max.isInclusive(), Is.is(false));
    }

    @Test
    void shouldReduceNegatedIsLessThanConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsLessThanConstantConstraint(field, 5, rules()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a numeric type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no upper bound", outputSpec.getNumericRestrictions().max,
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have lower bound", outputSpec.getNumericRestrictions().min,
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restriction lower bound is correct",
            outputSpec.getNumericRestrictions().min.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restriction lower bound is inclusive",
            outputSpec.getNumericRestrictions().min.isInclusive(), Is.is(true));
    }

    @Test
    void shouldReduceIsLessThanOrEqualToConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsLessThanOrEqualToConstantConstraint(field, 5, rules()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a Numeric type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no lower bound",
            outputSpec.getNumericRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have upper bound",
            outputSpec.getNumericRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions upper bound limit is correct",
            outputSpec.getNumericRestrictions().max.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restrictions upper bound is inclusive",
            outputSpec.getNumericRestrictions().max.isInclusive(), Is.is(true));
    }

    @Test
    void shouldReduceNegatedIsLessThanOrEqualToConstantConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsLessThanOrEqualToConstantConstraint(field, 5, rules()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a numeric type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have no upper bound", outputSpec.getNumericRestrictions().max,
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec numeric restrictions have lower bound", outputSpec.getNumericRestrictions().min,
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec numeric restriction lower bound is correct",
            outputSpec.getNumericRestrictions().min.getLimit(), Is.is(BigDecimal.valueOf(5)));
        Assert.assertThat("Fieldspec numeric restriction lower bound is exclusive",
            outputSpec.getNumericRestrictions().min.isInclusive(), Is.is(false));
    }

    @Test
    void shouldreduceIsAfterConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final OffsetDateTime testTimestamp = OffsetDateTime.of(2018, 2, 4, 23, 25, 16, 0, ZoneOffset.UTC);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsAfterConstantDateTimeConstraint(field, testTimestamp, rules()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a datetime type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no upper bound",
            outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have lower bound",
            outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct lower bound limit",
            outputSpec.getDateTimeRestrictions().min.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have exclusive lower bound",
            outputSpec.getDateTimeRestrictions().min.isInclusive(), Is.is(false));
    }

    @Test
    void shouldreduceNegatedIsAfterConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final OffsetDateTime testTimestamp = OffsetDateTime.of(2018, 2, 4, 23, 25, 16,0, ZoneOffset.UTC);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsAfterConstantDateTimeConstraint(field, testTimestamp, rules()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a datetime type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no lower bound",
            outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have upper bound",
            outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct upper bound limit",
            outputSpec.getDateTimeRestrictions().max.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have inclusive upper bound",
            outputSpec.getDateTimeRestrictions().max.isInclusive(), Is.is(true));
    }

    @Test
    void shouldreduceIsAfterOrEqualToConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final OffsetDateTime testTimestamp = OffsetDateTime.of(2018, 2, 4, 23, 25, 16, 0, ZoneOffset.UTC);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsAfterOrEqualToConstantDateTimeConstraint(field, testTimestamp, rules()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has no type restrictions", outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no upper bound",
            outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have lower bound",
            outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct lower bound limit",
            outputSpec.getDateTimeRestrictions().min.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have inclusive lower bound",
            outputSpec.getDateTimeRestrictions().min.isInclusive(), Is.is(true));
    }

    @Test
    void shouldreduceNegatedIsAfterOrEqualToConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final OffsetDateTime testTimestamp = OffsetDateTime.of(2018, 2, 4, 23, 25, 16, 0, ZoneOffset.UTC);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsAfterOrEqualToConstantDateTimeConstraint(field, testTimestamp, rules()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a DateTime type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no lower bound",
            outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have upper bound",
            outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct upper bound limit",
            outputSpec.getDateTimeRestrictions().max.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have exclusive upper bound",
            outputSpec.getDateTimeRestrictions().max.isInclusive(), Is.is(false));
    }

    @Test
    void shouldreduceIsBeforeConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final OffsetDateTime testTimestamp = OffsetDateTime.of(2018, 2, 4, 23, 25, 16, 0, ZoneOffset.UTC);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsBeforeConstantDateTimeConstraint(field, testTimestamp, rules()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a DateTime type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no lower bound",
            outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have upper bound",
            outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct upper bound limit",
            outputSpec.getDateTimeRestrictions().max.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have exclusive upper bound",
            outputSpec.getDateTimeRestrictions().max.isInclusive(), Is.is(false));
    }

    @Test
    void shouldreduceNegatedIsBeforeConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final OffsetDateTime testTimestamp = OffsetDateTime.of(2018, 2, 4, 23, 25, 16, 0, ZoneOffset.UTC);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsBeforeConstantDateTimeConstraint(field, testTimestamp, rules()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a datetime type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no upper bound",
            outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have lower bound",
            outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct lower bound limit",
            outputSpec.getDateTimeRestrictions().min.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have inclusive lower bound",
            outputSpec.getDateTimeRestrictions().min.isInclusive(), Is.is(true));
    }

    @Test
    void shouldreduceIsBeforeOrEqualToConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final OffsetDateTime testTimestamp = OffsetDateTime.of(2018, 2, 4, 23, 25, 16, 0, ZoneOffset.UTC);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsBeforeOrEqualToConstantDateTimeConstraint(field, testTimestamp, rules()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a DateTime type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no lower bound",
            outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have upper bound",
            outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct upper bound limit",
            outputSpec.getDateTimeRestrictions().max.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have inclusive upper bound",
            outputSpec.getDateTimeRestrictions().max.isInclusive(), Is.is(true));
    }

    @Test
    void shouldreduceNegatedIsBeforeorEqualToConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final OffsetDateTime testTimestamp = OffsetDateTime.of(2018, 2, 4, 23, 25, 16, 0, ZoneOffset.UTC);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsBeforeOrEqualToConstantDateTimeConstraint(field, testTimestamp, rules()).negate());

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a datetime type constraint", outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have no upper bound",
            outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have lower bound",
            outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct lower bound limit",
            outputSpec.getDateTimeRestrictions().min.getLimit(), Is.is(testTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have exclusive lower bound",
            outputSpec.getDateTimeRestrictions().min.isInclusive(), Is.is(false));
    }

    @Test
    void shouldMergeAndReduceIsAfterConstantDateTimeConstraintWithIsBeforeConstantDateTimeConstraint() {
        final Field field = new Field("test0");
        final OffsetDateTime startTimestamp = OffsetDateTime.of(2013, 11, 19, 10, 43, 12, 0, ZoneOffset.UTC);
        final OffsetDateTime endTimestamp = OffsetDateTime.of(2018, 2, 4, 23, 25, 8, 0, ZoneOffset.UTC);
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Arrays.asList(
            new IsAfterConstantDateTimeConstraint(field, startTimestamp, rules()),
            new IsBeforeConstantDateTimeConstraint(field, endTimestamp, rules()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a DateTime type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have lower bound",
            outputSpec.getDateTimeRestrictions().min, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct lower bound limit",
            outputSpec.getDateTimeRestrictions().min.getLimit(), Is.is(startTimestamp));
        Assert.assertThat("Fieldspect datetime restrictions have exclusive lower bound",
            outputSpec.getDateTimeRestrictions().min.isInclusive(), Is.is(false));
        Assert.assertThat("Fieldspec datetime restrictions have upper bound",
            outputSpec.getDateTimeRestrictions().max, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec datetime restrictions have correct upper bound limit",
            outputSpec.getDateTimeRestrictions().max.getLimit(), Is.is(endTimestamp));
        Assert.assertThat("Fieldspec datetime restrictions have exclusive upper bound",
            outputSpec.getDateTimeRestrictions().max.isInclusive(), Is.is(false));
    }

    @Test
    void shouldReduceMatchesRegexConstraint() {
        final Field field = new Field("test0");
        String pattern = ".*\\..*";
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new MatchesRegexConstraint(field, Pattern.compile(pattern), rules()));

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a String type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.notNullValue()));
    }

    @Test
    void shouldReduceSingleFormatConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));

        List<AtomicConstraint> constraints = Arrays.asList(
            new FormatConstraint(field, "Hello '$1'", rules())
        );

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a String type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has string restrictions", outputSpec.getStringRestrictions(),

            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec format restrictions has a value",
            outputSpec.getFormatRestrictions(), Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec format restrictions has a value",
            outputSpec.getFormatRestrictions().formatString, Is.is("Hello '$1'"));
    }

    @Test
    void shouldNotReduceMultipleFormatConstraint() {
        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));

        List<AtomicConstraint> constraints = Arrays.asList(
            new FormatConstraint(field, "Lorem '$1'", rules()),
            new FormatConstraint(field, "Ipsum '$1'", rules())
        );

        Assertions.assertThrows(
            UnsupportedOperationException.class,
            () -> constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints));
    }

    @Test
    void shouldReduceStringLongerThanConstraint() {
        final Field field = new Field("test0");

        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsStringLongerThanConstraint(field, 5, rules())
        );

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a String type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.notNullValue()));
    }

    @Test
    void shouldReduceStringShorterThanConstraint() {
        final Field field = new Field("test0");

        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new IsStringShorterThanConstraint(field, 5, rules())
        );

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec is not null", outputSpec, Is.is(IsNull.notNullValue()));
        Assert.assertThat("Fieldspec has a string constrint", outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.notNullValue()));
    }

    @Test
    void shouldReduceStringHasLengthConstraint() {
        final Field field = new Field("test0");

        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));
        List<AtomicConstraint> constraints = Collections.singletonList(
            new StringHasLengthConstraint(field, 5, rules())
        );

        RowSpec testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints).get();

        Assert.assertThat("Output is not null", testOutput, Is.is(IsNull.notNullValue()));
        FieldSpec outputSpec = testOutput.getSpecForField(field);
        Assert.assertThat("Fieldspec has a String type constraint",
            outputSpec.getTypeRestrictions().getAllowedTypes(),
            containsInAnyOrder(IsOfTypeConstraint.Types.values()));
        Assert.assertThat("Fieldspec has no set restrictions", outputSpec.getSetRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no null restrictions", outputSpec.getNullRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no numeric restrictions", outputSpec.getNumericRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has no datetime restrictions", outputSpec.getDateTimeRestrictions(),
            Is.is(IsNull.nullValue()));
        Assert.assertThat("Fieldspec has string restrictions", outputSpec.getStringRestrictions(),
            Is.is(IsNull.notNullValue()));
    }

    @Test
    void whenHasNumericRestrictions_shouldFilterSet() {

        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));

        List<AtomicConstraint> constraints = Arrays.asList(
            new IsOfTypeConstraint(field, IsOfTypeConstraint.Types.NUMERIC, rules()),
            new IsInSetConstraint(field, new HashSet<>(Arrays.asList(1, "lorem", 5, "ipsum", 2)), rules())
        );

        Optional<RowSpec> testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints);

        FieldSpec spec = testOutput.get().getSpecForField(field);

        SetRestrictions setRestrictions = spec.getSetRestrictions();

        Assert.assertThat(setRestrictions.getWhitelist(), containsInAnyOrder(1, 5, 2));
    }

    @Test
    void whenHasStringRestrictions_shouldOnlyFilterStringsInSet() {

        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));

        OffsetDateTime datetimeValue = OffsetDateTime.of(2001, 02, 03, 04, 05, 06, 0, ZoneOffset.UTC);
        List<AtomicConstraint> constraints = Arrays.asList(
            new MatchesRegexConstraint(field, Pattern.compile("(lorem|ipsum)"), rules()),
            new IsInSetConstraint(field, new HashSet<>(Arrays.asList(1, "lorem", 5, "ipsum", 2, "foo", datetimeValue)), rules())
        );

        Optional<RowSpec> testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints);

        FieldSpec spec = testOutput.get().getSpecForField(field);

        SetRestrictions setRestrictions = spec.getSetRestrictions();

        Assert.assertThat(setRestrictions.getWhitelist(), containsInAnyOrder("lorem", "ipsum", 1, 5, 2, datetimeValue));
    }

    @Test
    void whenHasNumericRestrictions_shouldOnlyFilterNumericValuesInSet() {

        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));

        OffsetDateTime datetimeValue = OffsetDateTime.of(2001, 02, 03, 04, 05, 06, 0, ZoneOffset.UTC);
        List<AtomicConstraint> constraints = Arrays.asList(
            new IsGreaterThanOrEqualToConstantConstraint(field, 2, rules()),
            new IsInSetConstraint(field, new HashSet<>(Arrays.asList(1, "lorem", 5, "ipsum", 2, datetimeValue)), rules())
        );

        Optional<RowSpec> testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints);

        FieldSpec spec = testOutput.get().getSpecForField(field);

        SetRestrictions setRestrictions = spec.getSetRestrictions();

        Assert.assertThat(setRestrictions.getWhitelist(), containsInAnyOrder("lorem", "ipsum", 5, 2, datetimeValue));
    }

    @Test
    void whenHasDateTimeRestrictions_shouldOnlyFilterDateTimeValuesInSet() {

        final Field field = new Field("test0");
        ProfileFields profileFields = new ProfileFields(Collections.singletonList(field));

        OffsetDateTime datetimeValue = OffsetDateTime.of(2001, 02, 03, 04, 05, 06, 0, ZoneOffset.UTC);
        OffsetDateTime oneHourLaterDateTimeValue = datetimeValue.plusHours(1);
        List<AtomicConstraint> constraints = Arrays.asList(
            new IsAfterConstantDateTimeConstraint(field, datetimeValue, rules()),
            new IsInSetConstraint(field, new HashSet<>(Arrays.asList(1, "lorem", 5, "ipsum", 2, datetimeValue, oneHourLaterDateTimeValue)), rules())
        );

        Optional<RowSpec> testOutput = constraintReducer.reduceConstraintsToRowSpec(profileFields, constraints);

        FieldSpec spec = testOutput.get().getSpecForField(field);

        SetRestrictions setRestrictions = spec.getSetRestrictions();

        Assert.assertThat(setRestrictions.getWhitelist(), containsInAnyOrder("lorem", "ipsum", 1, 5, 2, oneHourLaterDateTimeValue));
    }
    
    @Test
    public void shouldReduceConstraintsCorrectlyWhereOneIsViolated(){
        Field field = new Field("field");
        AtomicConstraint violatedConstraint = new ViolatedAtomicConstraint(new IsOfTypeConstraint(field, IsOfTypeConstraint.Types.STRING, rules()).negate());
        AtomicConstraint ofLengthConstraint = new IsStringShorterThanConstraint(field, 100, rules());
        AtomicConstraint matchesRegexConstraint = new MatchesRegexConstraint(field, Pattern.compile("[a-z]{2,}"), rules());

        Optional<FieldSpec> result = this.constraintReducer.reduceConstraintsToFieldSpec(
            Arrays.asList(violatedConstraint, ofLengthConstraint, matchesRegexConstraint));

        Assert.assertThat(result.isPresent(), is(true));
        Assert.assertThat(result.get().getTypeRestrictions().getAllowedTypes(), not(hasItem(IsOfTypeConstraint.Types.STRING)));
        Assert.assertThat(result.get().getTypeRestrictions().getAllowedTypes(), not(empty()));
    }

    @Test
    public void shouldReduceConstraintsToNullAllowedFieldSpecOnly() {
        Field field = new Field("field");
        AtomicConstraint ofTypeString = new IsOfTypeConstraint(field, IsOfTypeConstraint.Types.STRING, rules());

        Optional<FieldSpec> result = this.constraintReducer.reduceConstraintsToFieldSpec(
            Arrays.asList(ofTypeString, ofTypeString.negate()));

        Assert.assertThat(result.isPresent(), is(true));
        Assert.assertThat(result.get().getTypeRestrictions().getAllowedTypes(), empty());
    }

    private static Set<RuleInformation> rules(){
        return Collections.singleton(new RuleInformation());
    }
}
