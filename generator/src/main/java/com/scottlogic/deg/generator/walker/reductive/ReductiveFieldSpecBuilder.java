package com.scottlogic.deg.generator.walker.reductive;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSDecisionNode;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;

import java.util.*;

public class ReductiveFieldSpecBuilder {

    /**
     * creates a FieldSpec for a field for the current state of the tree
     * FieldSpec to be used for generating values
     * @param rootNode of the tree to create the fieldSpec for
     * @param field to create the fieldSpec for
     * @return fieldSpec with mustContains restriction if not contradictory, otherwise Optional.empty()
     */
    public Set<FieldSpec> getDecisionFieldSpecs(FSConstraintNode rootNode, Field field) {
        return getSpecsForConstraint(rootNode, field, new HashSet<>());
    }

    public Set<FieldSpec> getSpecsForConstraint(FSConstraintNode rootNode, Field field, Set<FieldSpec> set){
        FieldSpec fieldSpec = rootNode.getFieldSpecs().get(field);

        if (field != null) {
            set.add(fieldSpec);
            if (hasSet(fieldSpec)) {
                return set;
            }
        }

        for (FSDecisionNode decision : rootNode.getDecisions()) {
            set.addAll(getSpecsForDecision(decision, field, set));
        }

        return set;
    }

    private Set<FieldSpec> getSpecsForDecision(FSDecisionNode decision, Field field, Set<FieldSpec> set) {
        for (FSConstraintNode constraintNode : decision.getOptions()) {
            set.addAll(getSpecsForConstraint(constraintNode, field, set));
        }
        return set;
    }

    private boolean hasSet(FieldSpec fieldSpec) {
        return fieldSpec != null && fieldSpec.getSetRestrictions() != null;
    }

}
