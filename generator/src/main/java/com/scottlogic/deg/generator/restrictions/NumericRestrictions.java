package com.scottlogic.deg.generator.restrictions;

import java.math.BigDecimal;
import java.util.Objects;

public class NumericRestrictions {
    public static final int DEFAULT_NUMERIC_SCALE = 20;
    private final int numericScale;
    private final boolean negateGranularity;
    public NumericLimit<BigDecimal> min;
    public NumericLimit<BigDecimal> max;

    public NumericRestrictions(){
        this(DEFAULT_NUMERIC_SCALE, false);
    }

    public NumericRestrictions(int numericScale){
        this(numericScale, false);
    }

    public NumericRestrictions(int numericScale, boolean negateGranularity){
        this.numericScale = numericScale;
        this.negateGranularity = negateGranularity;
    }

    public NumericRestrictions(ParsedGranularity granularity, boolean negateGranularity) {
        numericScale = granularity.getNumericGranularity().scale();
        this.negateGranularity = negateGranularity;
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

    private boolean isCorrectScale(BigDecimal inputNumber) {
        //noinspection SimplifiableConditionalExpression
        return negateGranularity
            ? inputNumber.stripTrailingZeros().scale() > numericScale
            : inputNumber.stripTrailingZeros().scale() <= numericScale;
    }

    @Override
    public String toString() {
        return String.format(
            "%s%s%s%s%s%s",
            min != null ? min.toString(">") : "",
            min != null && max != null ? " and " : "",
            max != null ? max.toString("<") : "",
            negateGranularity ? "not(" : "",
            numericScale != 20 ? "granular-to " + numericScale : "",
            negateGranularity ? ")" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumericRestrictions that = (NumericRestrictions) o;
        return Objects.equals(min, that.min) &&
            Objects.equals(max, that.max) &&
            Objects.equals(numericScale, that.numericScale) &&
            Objects.equals(negateGranularity, that.negateGranularity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, numericScale, negateGranularity);
    }

    public boolean isGranularityNegated() {
        return negateGranularity;
    }
}
