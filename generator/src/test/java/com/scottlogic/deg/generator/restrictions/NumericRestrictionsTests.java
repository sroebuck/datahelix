package com.scottlogic.deg.generator.restrictions;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class NumericRestrictionsTests {
    @Test
    void equals_whenOtherObjectIsNull_returnsFalse() {
        assertNotEqual(
            NumericRestrictions.unrestrictive,
            null);
    }

    @Test
    void equals_whenOtherObjectIsStringNotTypeNumericRestrictions_returnsFalse() {
        assertNotEqual(
            NumericRestrictions.unrestrictive,
        "Test");
    }

    @Test
    void equals_whenOtherObjectIsIntNotTypeNumericRestrictions_returnsFalse() {
        assertNotEqual(
            NumericRestrictions.unrestrictive,
            1);
    }

    @Test
    void equals_whenNumericRestrictionsAreEqual_returnsTrue() {
        assertEqual(
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(0), false),
                    new NumericLimit<>(new BigDecimal(2), false)),
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(0), false),
                    new NumericLimit<>(new BigDecimal(2), false)));
    }

    @Test
    void equals_whenNumericRestrictionsNumericLimitMinValuesAreNotEqual_returnsFalse() {
        assertNotEqual(
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(1), false),
                    new NumericLimit<>(new BigDecimal(2), false)),
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(0), false),
                    new NumericLimit<>(new BigDecimal(2), false)));
    }

    @Test
    void equals_whenOneNumericRestrictionsLimitMinValueIsNull_returnsFalse() {
        assertNotEqual(
            NumericRestrictions.unrestrictive
                .withMaximum(
                    new NumericLimit<>(new BigDecimal(2), false)),
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(0), false),
                    new NumericLimit<>(new BigDecimal(2), false)));
    }

    @Test
    void equals_whenNumericRestrictionsLimitMinValuesAreNull_returnsTrue() {
        assertEqual(
            NumericRestrictions.unrestrictive
                .withMaximum(new NumericLimit<>(new BigDecimal(2), false)),
            NumericRestrictions.unrestrictive
                .withMaximum(new NumericLimit<>(new BigDecimal(2), false)));
    }

    @Test
    void equals_whenNumericRestrictionsLimitMaxValuesAreNotEqual_returnsFalse() {
        assertNotEqual(
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(0), true),
                    new NumericLimit<>(new BigDecimal(2), true)),
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(0), true),
                    new NumericLimit<>(new BigDecimal(3), true)));
    }

    @Test
    void equals_whenOneNumericRestrictionsLimitMaxValueIsNull_returnsFalse() {
        assertNotEqual(
            NumericRestrictions.unrestrictive
                .withMinimum(
                    new NumericLimit<>(new BigDecimal(0), false)),
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(0), false),
                    new NumericLimit<>(new BigDecimal(2), false)));
    }

    @Test
    void equals_whenNumericRestrictionsLimitMaxValuesAreNull_returnsTrue() {
        assertEqual(
            NumericRestrictions.unrestrictive
                .withMinimum(new NumericLimit<>(new BigDecimal(0), false)),
            NumericRestrictions.unrestrictive
                .withMinimum(new NumericLimit<>(new BigDecimal(0), false)));
    }

    @Test
    void equals_whenNumericRestrictionsLimitsMinInclusiveValuesAreNotEqual_returnsFalse() {
        assertNotEqual(
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(0), true),
                    new NumericLimit<>(new BigDecimal(2), false)),
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(0), false),
                    new NumericLimit<>(new BigDecimal(2), false)));
    }

    @Test
    void equals_whenNumericRestrictionsLimitsMaxInclusiveValuesAreNotEqual_returnsFalse() {
        assertNotEqual(
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(0), true),
                    new NumericLimit<>(new BigDecimal(2), false)),
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(0), true),
                    new NumericLimit<>(new BigDecimal(2), true)));
    }


    @Test
    void equals_whenNumericRestrictionsLimitsAreEqualAndNegative_returnsTrue() {
        assertEqual(
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(-1), false),
                    new NumericLimit<>(new BigDecimal(-1), false)),
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(-1), false),
                    new NumericLimit<>(new BigDecimal(-1), false)));
    }


    @Test
    void equals_whenOneNumericRestrictionsLimitIsOfScientificNotationButAllValuesAreEqual_returnsTrue() {
        assertEqual(
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(50), false),
                    new NumericLimit<>(new BigDecimal(100), false)),
            NumericRestrictions.unrestrictive
                .withRange(
                    new NumericLimit<>(new BigDecimal(5E1), false),
                    new NumericLimit<>(new BigDecimal(100), false)));
    }

    @Test
    public void shouldBeEqualIfNumericScaleIsTheSame(){
        assertEqual(
            granularityRestriction(0.1),
            granularityRestriction(0.1));
    }

    @Test
    public void shouldBeUnequalIfNumericScalesAreDifferent(){
        assertNotEqual(
            granularityRestriction(0.1),
            granularityRestriction(0.01));
    }

    private static NumericRestrictions granularityRestriction(double granularity){
        return NumericRestrictions.unrestrictive
            .withGranularity(ParsedGranularity.parse(BigDecimal.valueOf(granularity)));
    }

    private static void assertEqual(Object a, Object b) {
        Assert.assertEquals(a, b);
        Assert.assertEquals(b, a);

        if (a != null && b != null) Assert.assertEquals(a.hashCode(), b.hashCode());
    }

    private static void assertNotEqual(Object a, Object b) {
        Assert.assertNotEquals(a, b);
        Assert.assertNotEquals(b, a);

        // not sure about this; hash collisions aren't intrinsically functional errors
        if (a != null && b != null) Assert.assertNotEquals(a.hashCode(), b.hashCode());
    }
}
