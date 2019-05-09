package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.DataGeneratorBaseTypes;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;

public interface DataTypeFactory {
    /**
     * Create a FieldValueSource that can produce values for this data type that adhere to the given restrictions
     *
     * @param fieldSpec The restrictions the emitted data must adhere to
     * @return A reference to a type that can produce data for the data type
     */
    FieldValueSource createValueSource(FieldSpec fieldSpec);

    /**
     * Expose the underlying generator data type that this (custom) data extends
     *
     * @return The underlying data type for this data
     */
    DataGeneratorBaseTypes getUnderlyingDataType();


    /**
     * Determine if the provided value is a valid value for this data type taking into account the restrictions also
     *
     * @param value The value to be interrogated
     * @param fieldSpec The restrictions the data must adhere to
     * @return Whether the value is considered value for this data type or not
     */
    boolean isValid(Object value, FieldSpec fieldSpec);
}
