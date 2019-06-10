package com.scottlogic.deg.generator.decisiontree;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSDecisionNode;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DecisionTreeSimplifier {
    public DecisionTree simplify(DecisionTree originalTree) {
        return new DecisionTree(
            simplify(originalTree.getRootNode()),
            originalTree.getFields(),
            originalTree.getDescription());
    }

    public FSConstraintNode simplify(FSConstraintNode node) {
        if (node.getDecisions().isEmpty())
            return node;

        FSConstraintNode transformedNode = simplifySingleOptionDecisions(node);
        Collection<FSDecisionNode> simplifiedDecisions = transformedNode.getDecisions().stream()
            .map(this::simplify)
            .collect(Collectors.toList());

        return new FSConstraintNode(transformedNode.getFieldSpecs(), simplifiedDecisions);
    }

    private FSDecisionNode simplify(FSDecisionNode decision) {
        List<FSConstraintNode> newNodes = new ArrayList<>();

        for (FSConstraintNode existingOption : decision.getOptions()) {
            FSConstraintNode simplifiedNode = simplify(existingOption);

            // if an option contains no constraints and only one decision, then it can be replaced by the set of options within that decision.
            // this helps simplify the sorts of trees that come from eg A OR (B OR C)
            if (simplifiedNode.getFieldSpecs().isEmpty() && simplifiedNode.getDecisions().size() == 1) {
                newNodes.addAll(
                    simplifiedNode.getDecisions()
                        .iterator().next() //get only member
                        .getOptions());
            } else {
                newNodes.add(simplifiedNode);
            }
        }

        return new FSDecisionNode(newNodes);
    }

    private FSConstraintNode simplifySingleOptionDecisions(FSConstraintNode node) {
        return node.getDecisions()
            .stream()
            .filter(FSDecisionNode -> FSDecisionNode.getOptions().size() == 1)
            .reduce(
                node,
                (parentConstraint, FSDecisionNode) -> {
                    FSConstraintNode firstOption = FSDecisionNode.getOptions().iterator().next();
                    ArrayList<FSDecisionNode> decisions = new ArrayList<>(parentConstraint.getDecisions());
                    HashMap<Field, FieldSpec> fieldSpecs = new HashMap<>(parentConstraint.getFieldSpecs());

                    if (parentConstraint.getFieldSpecs().keySet().stream().noneMatch(firstOption.getFieldSpecs().keySet()::contains)) {
                        decisions.addAll(firstOption.getDecisions());
                        fieldSpecs.putAll(firstOption.getFieldSpecs());
                    }

                    decisions.remove(FSDecisionNode);
                    return new FSConstraintNode(fieldSpecs, decisions);

                },
                (node1, node2) ->
                    new FSConstraintNode(
                        Stream
                            .concat(node1.getFieldSpecs().entrySet().stream(), node2.getFieldSpecs().entrySet().stream())
                            .collect(
                                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                        Stream
                            .concat(node1.getDecisions().stream(), node2.getDecisions().stream())
                            .collect(Collectors.toList())
                    ));
    }
}
