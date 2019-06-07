package com.scottlogic.deg.generator.walker.reductive.fieldselectionstrategy;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.generator.decisiontree.ConstraintNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSDecisionNode;
import com.scottlogic.deg.generator.decisiontree.visualisation.BaseVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FieldAppearanceAnalyser {

    Map<Field, Integer> fieldAppearances = new HashMap<>();

    public void visit(FSConstraintNode constraintNode){
        constraintNode.getFieldSpecs().keySet()
            .forEach(this::countField);

        constraintNode.getDecisions()
            .forEach(this::visit);
    }

    private void visit(FSDecisionNode decisionNode) {
        decisionNode.getOptions()
            .forEach(this::visit);
    }

    private void countField(Field field) {
        fieldAppearances.compute(field, (k, count) -> count == null ? 1 : count+1);
    }
}
