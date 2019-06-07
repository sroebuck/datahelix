package com.scottlogic.deg.generator.walker.reductive.fieldselectionstrategy;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSDecisionNode;

import java.util.HashMap;
import java.util.Map;

public class FieldAppearanceAnalyser {

    Map<Field, Integer> fieldAppearances = new HashMap<>();

    public void count(FSConstraintNode constraintNode){
        constraintNode.getFieldSpecs().keySet()
            .forEach(this::countField);

        constraintNode.getDecisions()
            .forEach(this::count);
    }

    private void count(FSDecisionNode decisionNode) {
        decisionNode.getOptions()
            .forEach(this::count);
    }

    private void countField(Field field) {
        fieldAppearances.compute(field, (k, count) -> count == null ? 1 : count+1);
    }
}
