package com.scottlogic.deg.generator.decisiontree.FieldSpecTree;

import java.util.Collection;

public class FSDecisionNode {
    private Collection<FSConstraintNode> options;

    public FSDecisionNode(Collection<FSConstraintNode> options) {
        this.options = options;
    }

    public Collection<FSConstraintNode> getOptions(){
        return options;
    }
}
