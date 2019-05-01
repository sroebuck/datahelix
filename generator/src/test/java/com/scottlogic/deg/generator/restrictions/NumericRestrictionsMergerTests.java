package com.scottlogic.deg.generator.restrictions;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;

class NumericRestrictionsMergerTests {
    @Test
    public void withNoRestrictions_shouldReturnSuccessWithNoRestrictions(){
        assertMergeOutput("null merges with null to give null", null, null, null);
    }

    @Test
    public void withOnlyLeftNumericRestrictions_shouldReturnLeftRestrictions(){
        NumericRestrictionsMerger merger = new NumericRestrictionsMerger();
        NumericRestrictions left = NumericRestrictions.unrestrictive;

        MergeResult<NumericRestrictions> result = merger.merge(left, null);

        Assert.assertThat(result, not(nullValue()));
        Assert.assertThat(result.successful, is(true));
        Assert.assertThat(result.restrictions, is(sameInstance(left)));
    }

    @Test
    public void withOnlyRightNumericRestrictions_shouldReturnLeftRestrictions(){
        NumericRestrictionsMerger merger = new NumericRestrictionsMerger();
        NumericRestrictions right = NumericRestrictions.unrestrictive;

        MergeResult<NumericRestrictions> result = merger.merge(null, right);

        Assert.assertThat(result, not(nullValue()));
        Assert.assertThat(result.successful, is(true));
        Assert.assertThat(result.restrictions, is(sameInstance(right)));
    }

    @Test
    public void withNonContradictoryNumericRestrictions_shouldReturnMergedRestrictions() {
        assertMergeOutput(
            "0 <= X <= 10 and 1 < X < 10 should become 1 < X < 10",
            NumericRestrictions.unrestrictive
                .withRange(
                    NumericLimit.inclusive(BigDecimal.ZERO),
                    NumericLimit.inclusive(BigDecimal.TEN)),
            NumericRestrictions.unrestrictive
                .withRange(
                    NumericLimit.exclusive(BigDecimal.ONE),
                    NumericLimit.exclusive(BigDecimal.TEN)),

            NumericRestrictions.unrestrictive
                .withRange(
                    NumericLimit.exclusive(BigDecimal.ONE),
                    NumericLimit.exclusive(BigDecimal.TEN)));
    }

    @Test
    public void withLessThanOrEqualAndGreaterThanOrEqualSameNumber_shouldReturnMergedRestrictions() {
        assertMergeOutput(
            "X >= 10 and X <= 10 should become 10 >= X >= 10",
            NumericRestrictions.unrestrictive
                .withMinimum(
                    NumericLimit.inclusive(BigDecimal.TEN)),
            NumericRestrictions.unrestrictive
                .withMaximum(
                    NumericLimit.inclusive(BigDecimal.TEN)),

            NumericRestrictions.unrestrictive
                .withRange(
                    NumericLimit.inclusive(BigDecimal.TEN),
                    NumericLimit.inclusive(BigDecimal.TEN)));
    }

    @Test
    public void withContradictoryNumericRestrictions_shouldReturnUnsuccessful(){
        assertMergeFails(
            "Can't merge 0 >= X > 10 with 10 > X > 20",
            NumericRestrictions.unrestrictive
                .withRange(
                    NumericLimit.inclusive(BigDecimal.ZERO),
                    NumericLimit.exclusive(BigDecimal.TEN)),

            NumericRestrictions.unrestrictive
                .withRange(
                    NumericLimit.exclusive(BigDecimal.TEN),
                    NumericLimit.exclusive(BigDecimal.valueOf(20))));
    }

    @Test
    public void withScaleEqualToRange_shouldReturnSuccessful(){
        assertMergeSucceeds(
            "0 <= X <= 1 is compatible with X is integer",
            NumericRestrictions.unrestrictive
                .withRange(
                    NumericLimit.inclusive(BigDecimal.ZERO),
                    NumericLimit.inclusive(BigDecimal.ONE)),
            withGranularity("1"));
    }

    @Test
    public void withScaleEqualToRangeExclusiveMax_shouldReturnSuccessful(){
        assertMergeSucceeds(
            "0 <= X < 1 is compatible with X is integer",
            NumericRestrictions.unrestrictive
                .withRange(
                    NumericLimit.inclusive(BigDecimal.ZERO),
                    NumericLimit.exclusive(BigDecimal.ONE)),
            withGranularity("1"));
    }

    @Test
    public void withScaleEqualToRangeExclusiveMin_shouldReturnSuccessful(){
        assertMergeSucceeds(
            "0 < X <= 1 is compatible with X is integer",
            NumericRestrictions.unrestrictive
                .withRange(
                    NumericLimit.exclusive(BigDecimal.ZERO),
                    NumericLimit.inclusive(BigDecimal.ONE)),
            withGranularity("1"));
    }

    @Test
    public void withScaleLargerThan_shouldReturnUnsuccessful(){
        assertMergeFails(
            "Can't merge 0 > X > 1 with X is integer",
            NumericRestrictions.unrestrictive
                .withRange(
                    NumericLimit.exclusive(BigDecimal.ZERO),
                    NumericLimit.exclusive(BigDecimal.ONE)),
            withGranularity("1"));
    }

    @Test
    public void smallerScaleExlusiveLimit_shouldReturnSuccessful() {
        assertMergeSucceeds(
            "Can merge 0 > X > 1 with X granularTo 0.1",
            NumericRestrictions.unrestrictive
                .withRange(
                    NumericLimit.exclusive(BigDecimal.ZERO),
                    NumericLimit.exclusive(BigDecimal.ONE)),
            withGranularity("0.1"));
    }

    private static void assertMergeFails(String reason, NumericRestrictions a, NumericRestrictions b) {
        NumericRestrictionsMerger merger = new NumericRestrictionsMerger();
        MergeResult<NumericRestrictions> result = merger.merge(a, b);

        Assert.assertThat("Merge should never return null", result, not(nullValue()));
        Assert.assertThat(reason, result.successful, is(false));
    }

    private static void assertMergeSucceeds(String reason, NumericRestrictions a, NumericRestrictions b) {
        MergeResult<NumericRestrictions> result = new NumericRestrictionsMerger().merge(a, b);

        Assert.assertThat("Merge should never return null", result, not(nullValue()));
        Assert.assertThat(reason, result.successful, is(true));
    }

    private static void assertMergeOutput(
            String reason,
            NumericRestrictions a,
            NumericRestrictions b,
            NumericRestrictions expectedResult) {

        MergeResult<NumericRestrictions> result = new NumericRestrictionsMerger().merge(a, b);

        Assert.assertThat("Merge should never return null", result, not(nullValue()));
        Assert.assertThat(result.successful, is(true));
        Assert.assertThat(reason, result.restrictions, equalTo(expectedResult));
    }

    private static NumericRestrictions withGranularity(String granularity) {
        return NumericRestrictions.unrestrictive.withGranularity(
            ParsedGranularity.parse(
                new BigDecimal(granularity)));
    }
}
