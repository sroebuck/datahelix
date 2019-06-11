package com.scottlogic.deg.generator.walker.reductive;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.generator.decisiontree.ConstraintNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSDecisionNode;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecMerger;
import com.scottlogic.deg.generator.fieldspecs.RowSpecMerger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

class PrunedConstraintState {

    private final Collection<FSDecisionNode> newDecisionNodes = new ArrayList<>();
    private final RowSpecMerger rowSpecMerger;

    public Map<Field, FieldSpec> fieldSpecs;
    private boolean hasPulledUpDecisions;

    PrunedConstraintState(Map<Field, FieldSpec> fieldSpecs, RowSpecMerger rowSpecMerger){
        this.fieldSpecs = fieldSpecs;
        this.rowSpecMerger = rowSpecMerger;
    }

    boolean addPrunedDecision(FSDecisionNode prunedDecisionNode) {
        if (!onlyOneOption(prunedDecisionNode)) {
            newDecisionNodes.add(prunedDecisionNode);
            return true;
        }

        hasPulledUpDecisions = true;
        FSConstraintNode remainingConstraintNode = getOnlyRemainingOption(prunedDecisionNode);

        Optional<Map<Field, FieldSpec>> merged = rowSpecMerger.merge(remainingConstraintNode.getFieldSpecs(), fieldSpecs);
        if (!merged.isPresent()){
            return false;
        }
        fieldSpecs = merged.get();

        newDecisionNodes.addAll(remainingConstraintNode.getDecisions());
        return true;
    }

    private boolean onlyOneOption(FSDecisionNode prunedDecisionNode) {
        return prunedDecisionNode.getOptions().size() == 1;
    }

    private FSConstraintNode getOnlyRemainingOption(FSDecisionNode prunedDecisionNode) {
        return prunedDecisionNode.getOptions().iterator().next();
    }

    public boolean hasPulledUpDecisions() {
        return hasPulledUpDecisions;
    }

    public FSConstraintNode getNewFSConstraintNode() {
        return new FSConstraintNode(fieldSpecs, newDecisionNodes);
    }
}
