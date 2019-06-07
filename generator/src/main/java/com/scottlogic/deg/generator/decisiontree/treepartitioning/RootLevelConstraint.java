package com.scottlogic.deg.generator.decisiontree.treepartitioning;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.generator.decisiontree.DecisionNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSDecisionNode;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class RootLevelConstraint {
    private Object constraint;

    RootLevelConstraint(FSDecisionNode decisionNode) {
        constraint = decisionNode;
    }

    RootLevelConstraint(Map.Entry<Field, FieldSpec> fieldToSpec) {
        constraint = fieldToSpec;
    }

    FSDecisionNode getDecisionNode() {
        return constraint instanceof FSDecisionNode
            ? (FSDecisionNode)constraint
            : null;
    }

    Map.Entry<Field, FieldSpec> getFieldToSpec() {
        return constraint instanceof Map.Entry
            ? (Map.Entry<Field, FieldSpec>)constraint
            : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RootLevelConstraint that = (RootLevelConstraint) o;
        return Objects.equals(constraint, that.constraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(constraint);
    }
}
