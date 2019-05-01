package com.scottlogic.deg.generator.restrictions;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

import static com.scottlogic.deg.generator.utils.NumberUtils.coerceToBigDecimal;

public class NumericRestrictions {
    public static final int DEFAULT_NUMERIC_SCALE = 20;

    private final int numericScale;
    @Nullable public final NumericLimit<BigDecimal> min;
    @Nullable public final NumericLimit<BigDecimal> max;

    protected NumericRestrictions() {
        numericScale = DEFAULT_NUMERIC_SCALE;
        min = null;
        max = null;
    }

    protected NumericRestrictions(
            @Nullable NumericLimit<BigDecimal> min,
            @Nullable NumericLimit<BigDecimal> max,
            int numericScale) {
        this.min = min;
        this.max = max;
        this.numericScale = numericScale;
    }

    public final static NumericRestrictions unrestrictive = new NumericRestrictions();

    public NumericRestrictions withGranularity(ParsedGranularity granularity) {
        return new NumericRestrictions(this.min, this.max, granularity.getNumericGranularity().scale());
    }

    // ideally we'd remove this method (to hide the temporary connection between granularity and decimal scale)
    public NumericRestrictions withDecimalScale(int decimalScale) {
        return new NumericRestrictions(this.min, this.max, decimalScale);
    }

    public NumericRestrictions withMinimum(NumericLimit<BigDecimal> min) {
        return new NumericRestrictions(min, this.max, this.numericScale);
    }

    public NumericRestrictions withMaximum(NumericLimit<BigDecimal> max) {
        return new NumericRestrictions(this.min, max, this.numericScale);
    }

    public NumericRestrictions withRange(NumericLimit<BigDecimal> min, NumericLimit<BigDecimal> max) {
        return new NumericRestrictions(min, max, this.numericScale);
    }

    public int getNumericScale() {
        return this.numericScale;
    }

    public static boolean isNumeric(Object o){
        return o instanceof Number;
    }

    public boolean match(Object o) {
        if (!NumericRestrictions.isNumeric(o)) {
            return false;
        }

        BigDecimal n = new BigDecimal(o.toString());

        if(min != null){
            if(n.compareTo(min.getLimit()) < (min.isInclusive() ? 0 : 1))
            {
                return false;
            }
        }

        if(max != null){
            if(n.compareTo(max.getLimit()) > (max.isInclusive() ? 0 : -1))
            {
                return false;
            }
        }

        return isCorrectScale(n);
    }

    public BigDecimal getStepSize() {
        return BigDecimal.ONE.scaleByPowerOfTen(numericScale * -1);
    }

    private boolean isCorrectScale(Number inputNumber) {
        BigDecimal inputAsBigDecimal = coerceToBigDecimal(inputNumber);
        return inputAsBigDecimal.scale() <= numericScale;
    }

    @Override
    public String toString() {
        return String.format(
            "%s%s%s%s",
            min != null ? min.toString(">") : "",
            min != null && max != null ? " and " : "",
            max != null ? max.toString("<") : "",
            numericScale != 20 ? "granular-to " + numericScale : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumericRestrictions that = (NumericRestrictions) o;
        return Objects.equals(min, that.min) &&
            Objects.equals(max, that.max) &&
            Objects.equals(numericScale, that.numericScale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, numericScale);
    }
}
