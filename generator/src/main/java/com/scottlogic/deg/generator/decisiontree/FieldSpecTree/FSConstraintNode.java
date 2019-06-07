package com.scottlogic.deg.generator.decisiontree.FieldSpecTree;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.generator.decisiontree.DecisionNode;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;

import java.util.Collection;
import java.util.Map;

public class FSConstraintNode {
    private final Map<Field, FieldSpec> fieldSpecs;
    private final Collection<DecisionNode> decisions;

    public FSConstraintNode(Map<Field, FieldSpec> fieldSpecs, Collection<DecisionNode> decisions) {
        this.fieldSpecs = fieldSpecs;
        this.decisions = decisions;
    }

    Collection<DecisionNode> getDecisions(){
        return decisions;
    }

    public Map<Field, FieldSpec> getFieldSpecs() {
        return fieldSpecs;
    }
}
