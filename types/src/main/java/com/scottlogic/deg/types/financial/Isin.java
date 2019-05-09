package com.scottlogic.deg.types.financial;

import com.scottlogic.deg.generator.DataGeneratorBaseTypes;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.DataTypeFactory;
import com.scottlogic.deg.generator.generation.IsinStringGenerator;
import com.scottlogic.deg.generator.generation.StringGenerator;
import com.scottlogic.deg.generator.generation.fieldvaluesources.CannedValuesFieldValueSource;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;
import com.scottlogic.deg.generator.restrictions.StringRestrictions;

public class Isin implements DataTypeFactory {
    private static final StringGenerator isinGenerator = new IsinStringGenerator();

    @Override
    public FieldValueSource createValueSource(FieldSpec fieldSpec) {
        if (fieldSpec.getStringRestrictions() != null){
            Impact impactOnGeneration = getImpactOnValueProduction(fieldSpec.getStringRestrictions());

            if (impactOnGeneration != Impact.NONE){
                return CannedValuesFieldValueSource.of(); //No values possible
            }
        }

        return isinGenerator.asFieldValueSource();
    }

    @Override
    public DataGeneratorBaseTypes getUnderlyingDataType() {
        return DataGeneratorBaseTypes.STRING;
    }

    @Override
    public boolean isValid(Object value, FieldSpec fieldSpec) {
        return value instanceof String && isinGenerator.match((String)value);
    }


    /**
     * Calculate if the given TextualRestrictions could impact the ability to create some or all values
     * If there are ANY regex statements then they COULD affect the ability to create values (therefore yield true)
     * Get the length of the codes that would be produced, e.g. an ISIN is 12 characters.
     * Check if any length restrictions exist that would prevent strings of this length being produced.
     *
     * @param textualRestrictions The other restrictions type to check
     */
    private Impact getImpactOnValueProduction(StringRestrictions textualRestrictions) {
        boolean hasRegexRestrictions = !textualRestrictions.getContainingRegex().isEmpty()
            || !textualRestrictions.getMatchingRegex().isEmpty()
            || !textualRestrictions.getNotMatchingRegex().isEmpty()
            || !textualRestrictions.getNotContainingRegex().isEmpty();

        if (hasRegexRestrictions){
            return Impact.POTENTIAL; //because we dont know they wouldn't affect the values - see #487
        }

        int maxLength = textualRestrictions.getMaxLength() != null ? textualRestrictions.getMaxLength() : Integer.MAX_VALUE;
        int minLength = textualRestrictions.getMinLength() != null ? textualRestrictions.getMinLength() : 0;
        int codeLength = IsinStringGenerator.ISIN_LENGTH;

        return (codeLength < minLength || codeLength > maxLength || textualRestrictions.getExcludedLengths().contains(codeLength))
            ? Impact.CONFIRMED
            : Impact.NONE;
    }

    @Override
    public boolean canProduceAnyValues(FieldSpec fieldSpec) {
        StringRestrictions stringRestrictions = fieldSpec.getStringRestrictions();
        if (stringRestrictions == null){
            return true;
        }

        switch (getImpactOnValueProduction(fieldSpec.getStringRestrictions())){
            case NONE:
                return true;
            case CONFIRMED:
                return false;
            case POTENTIAL:
                return true; //May be able to produce some values, but cannot confirm nor deny
        }

        return true;
    }

    private enum Impact
    {
        /**
         * There is the potential for some impact, but it cannot be confirmed or denied
         */
        POTENTIAL,

        /**
         * There is no impact on the production of values
         */
        NONE,

        /**
         * There is a confirmed impact on the generation of values
         */
        CONFIRMED
    }
}
