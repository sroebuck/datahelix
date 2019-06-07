package com.scottlogic.deg.generator.decisiontree.FieldSpecTree;

import com.scottlogic.deg.generator.decisiontree.ConstraintNode;

import java.util.Collection;

public class FSDecisionNode {
    private Collection<FSConstraintNode> options;

    Collection<FSConstraintNode> getOptions(){
        return options;
    }
}
