package com.scottlogic.deg.generator.walker.reductive;

import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;

import java.io.IOException;

public interface IterationVisualiser {
    void visualise(FSConstraintNode rootNode, ReductiveState reductiveState) throws IOException;
}
