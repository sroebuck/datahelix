package com.scottlogic.deg.generator.walker.reductive;

import com.google.inject.Inject;
import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.generator.decisiontree.*;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSDecisionNode;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecHelper;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecMerger;
import com.scottlogic.deg.generator.fieldspecs.RowSpecMerger;
import com.scottlogic.deg.generator.generation.databags.DataBagValue;
import com.scottlogic.deg.generator.reducer.ConstraintReducer;

import java.util.*;
import java.util.stream.Collectors;

public class ReductiveTreePruner {

    private final RowSpecMerger rowSpecMerger;
    private final FieldSpecHelper fieldSpecHelper;

    @Inject
    public ReductiveTreePruner(RowSpecMerger rowSpecMerger, FieldSpecHelper fieldSpecHelper) {
        this.rowSpecMerger = rowSpecMerger;
        this.fieldSpecHelper = fieldSpecHelper;
    }

    /**
     * Prunes a tree of any branches that are contradictory to the value of the nextFixedField
     * @param constraintNode The Tree to be pruned
     * @param value the field and value to prune for.
     * @return A pruned tree if the new tree is valid, Merged.contradictory otherwise
     */
    public Merged<FSConstraintNode> pruneConstraintNode(FSConstraintNode constraintNode, Field field, DataBagValue value) {
        Map<Field, FieldSpec> fieldToSpec = new HashMap<>();
        fieldToSpec.put(field, fieldSpecHelper.getFieldSpecForValue(value));
        return pruneFSConstraintNode(constraintNode, fieldToSpec);
    }

    private Merged<FSConstraintNode> pruneFSConstraintNode(FSConstraintNode constraintNode, Map<Field, FieldSpec> parentFieldSpecs) {
        Merged<Map<Field, FieldSpec>> newFieldSpecs = combineConstraintsWithParent(constraintNode, parentFieldSpecs);
        if (newFieldSpecs.isContradictory()){
            return Merged.contradictory();
        }

        PrunedConstraintState state = new PrunedConstraintState(newFieldSpecs.get(), rowSpecMerger);
        for (FSDecisionNode decision : constraintNode.getDecisions()) {
            Merged<FSDecisionNode> prunedFSDecisionNode = pruneFSDecisionNode(decision, newFieldSpecs.get());
            if (prunedFSDecisionNode.isContradictory()) {
                return Merged.contradictory();
            }

            boolean successful = state.addPrunedDecision(prunedFSDecisionNode.get());
            if (!successful){
                return Merged.contradictory();
            }
        }

        if (state.hasPulledUpDecisions()){
            return pruneFSConstraintNode(
                state.getNewFSConstraintNode(),
                state.fieldSpecs);
        }

        return Merged.of(state.getNewFSConstraintNode());
    }

    private Merged<FSDecisionNode> pruneFSDecisionNode(FSDecisionNode decisionNode,  Map<Field, FieldSpec> fieldSpecs) {
        Collection<FSConstraintNode> newFSConstraintNodes = new ArrayList<>();

        for (FSConstraintNode FSConstraintNode : decisionNode.getOptions()) {
            pruneFSConstraintNode(FSConstraintNode, fieldSpecs).ifPresent(newFSConstraintNodes::add);
        }

        if (newFSConstraintNodes.isEmpty()) {
            return Merged.contradictory();
        }

        return Merged.of(new FSDecisionNode(newFSConstraintNodes));
    }

    private Merged<Map<Field, FieldSpec>> combineConstraintsWithParent(FSConstraintNode constraintNode, Map<Field, FieldSpec> parentFieldSpecs) {
        Map<Field, FieldSpec> relevantConstraints = getRelevantConstraints(constraintNode.getFieldSpecs(), parentFieldSpecs.keySet());

        Optional<Map<Field, FieldSpec>> merged = rowSpecMerger.merge(relevantConstraints, parentFieldSpecs);

        return merged.map(Merged::of).orElseGet(Merged::contradictory);
    }

    private Map<Field, FieldSpec> getRelevantConstraints(Map<Field, FieldSpec> fieldSpecs, Set<Field> relevantFields) {
        return fieldSpecs.entrySet().stream()
            .filter(e->relevantFields.contains(e.getKey()))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue));
    }
}
