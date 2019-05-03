package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.generation.fieldvaluesources.FieldValueSource;

public interface FieldValueSourceFactory {
    FieldValueSource createValueSource(FieldSpec fieldSpec);
    Class getUnderlyingDataType();
}
