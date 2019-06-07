package com.scottlogic.deg.generator.walker;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.constraints.atomic.IsInSetConstraint;
import com.scottlogic.deg.generator.decisiontree.ConstraintNode;
import com.scottlogic.deg.generator.decisiontree.DecisionNode;
import com.scottlogic.deg.generator.decisiontree.ConstraintNode;
import com.scottlogic.deg.generator.decisiontree.DecisionNode;

import java.util.Arrays;
import java.util.Collections;

public class IteratorTestHelper {
    static ConstraintNode endConstraint(String name) { return constraint(name); }
    static DecisionNode singleDecision() { return new DecisionNode(endConstraint("single")); }
    static DecisionNode doubleDecision(String name) {
        return new DecisionNode(
            endConstraint(name + " left"), endConstraint(name + " right")); }
    static ConstraintNode constraintSingle() {
        return constraint("constraintSingle", singleDecision()); }
    static ConstraintNode constraintDouble() { return constraint("constraintDouble", doubleDecision("")); }

    static ConstraintNode constraintSingleDouble() { return constraint("constraintSingleDouble",
        singleDecision(), doubleDecision("right")); }

    static ConstraintNode constraintDoubleDouble(String name) { return constraint(name,
        doubleDecision(" left"), doubleDecision(" right")); }

    static ConstraintNode constraintTripleDouble() { return constraint("constraintTripleDouble",
        doubleDecision("left"), doubleDecision("middle"), doubleDecision("right")); }

    static ConstraintNode constraintDoubleLayered(){return constraint("constraintDoubleLayered",
            doubleDecision("left"), new DecisionNode(constraintDoubleDouble("right")));
    }

    static ConstraintNode constraintBiggy(){ return constraint("constraintBiggy",
            new DecisionNode(constraintDoubleDouble("left left"), endConstraint("left right")),
            new DecisionNode(constraintDoubleDouble("right left"), endConstraint("right right")));
    }



    private static ConstraintNode constraint(String name, DecisionNode... decisions){
        return new ConstraintNode(
            Collections.singletonList(new IsInSetConstraint(new Field(name), Collections.singleton(name), Collections.emptySet())),
            Arrays.asList(decisions));
    }
}
