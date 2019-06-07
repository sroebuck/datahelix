package com.scottlogic.deg.generator.decisiontree;

import com.scottlogic.deg.common.profile.constraints.atomic.AtomicConstraint;

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

    public ConstraintNode simplify(ConstraintNode node) {
        if (node.getDecisions().isEmpty())
            return node;

        ConstraintNode transformedNode = simplifySingleOptionDecisions(node);
        Collection<DecisionNode> simplifiedDecisions = transformedNode.getDecisions().stream()
            .map(this::simplify)
            .collect(Collectors.toList());

        return new ConstraintNode(transformedNode.getAtomicConstraints(), simplifiedDecisions);
    }

    private DecisionNode simplify(DecisionNode decision) {
        List<ConstraintNode> newNodes = new ArrayList<>();

        for (ConstraintNode existingOption : decision.getOptions()) {
            ConstraintNode simplifiedNode = simplify(existingOption);

            // if an option contains no constraints and only one decision, then it can be replaced by the set of options within that decision.
            // this helps simplify the sorts of trees that come from eg A OR (B OR C)
            if (simplifiedNode.getAtomicConstraints().isEmpty() && simplifiedNode.getDecisions().size() == 1) {
                newNodes.addAll(
                    simplifiedNode.getDecisions()
                        .iterator().next() //get only member
                        .getOptions());
            } else {
                newNodes.add(simplifiedNode);
            }
        }

        return new DecisionNode(newNodes);
    }

    private ConstraintNode simplifySingleOptionDecisions(ConstraintNode node) {
        return node.getDecisions()
            .stream()
            .filter(decisionNode -> decisionNode.getOptions().size() == 1)
            .reduce(
                node,
                (parentConstraint, decisionNode) -> {
                    ConstraintNode firstOption = decisionNode.getOptions().iterator().next();
                    ArrayList<DecisionNode> decisions = new ArrayList<>(parentConstraint.getDecisions());
                    ArrayList<AtomicConstraint> atomicConstraints = new ArrayList<>(parentConstraint.getAtomicConstraints());

                    if (!parentConstraint.getAtomicConstraints().stream().anyMatch(firstOption.getAtomicConstraints()::contains)) {
                        decisions.addAll(firstOption.getDecisions());
                        atomicConstraints.addAll(firstOption.getAtomicConstraints());
                    }

                    decisions.remove(decisionNode);
                    return new ConstraintNode(atomicConstraints, decisions);

                },
                (node1, node2) ->
                    new ConstraintNode(
                        Stream
                            .concat(node1.getAtomicConstraints().stream(), node2.getAtomicConstraints().stream())
                            .collect(Collectors.toList()),
                        Stream
                            .concat(node1.getDecisions().stream(), node2.getDecisions().stream())
                            .collect(Collectors.toList())
                    ));
    }
}
