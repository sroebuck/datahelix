package com.scottlogic.deg.generator.decisiontree;

import com.scottlogic.deg.common.profile.constraints.atomic.AtomicConstraint;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConstraintNode {

    public static final ConstraintNode empty = new ConstraintNode(Collections.emptySet(), Collections.emptySet());

    private final Collection<AtomicConstraint> atomicConstraints;
    private final Collection<DecisionNode> decisions;

    public ConstraintNode(Collection<AtomicConstraint> atomicConstraints, Collection<DecisionNode> decisions) {
        this.atomicConstraints = Collections.unmodifiableCollection(atomicConstraints);
        this.decisions = Collections.unmodifiableCollection(decisions);
    }

    public ConstraintNode(AtomicConstraint... atomicConstraints) {
        this(
            Arrays.asList(atomicConstraints),
            Collections.emptySet()
        );
    }

    public Collection<AtomicConstraint> getAtomicConstraints() {
        return new HashSet<>(atomicConstraints);
    }

    public Collection<DecisionNode> getDecisions() {
        return decisions;
    }

    public String toString(){
        if (decisions.isEmpty())
            return atomicConstraints.size() > 5
                ? String.format("%d constraints", atomicConstraints.size())
                : Objects.toString(atomicConstraints);

        if (atomicConstraints.isEmpty())
            return decisions.size() > 5
                ? String.format("%d decisions", decisions.size())
                : Objects.toString(decisions);

        return String.format(
            "Decision: %s, Constraints: %s",
            decisions.size() > 5
                ? String.format("%d decisions", decisions.size())
                : Objects.toString(decisions),
            atomicConstraints.size() > 5
                ? String.format("%d constraints", atomicConstraints.size())
                : Objects.toString(atomicConstraints));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstraintNode that = (ConstraintNode) o;
        return Objects.equals(atomicConstraints, that.atomicConstraints) &&
            Objects.equals(decisions, that.decisions);
    }

    public int hashCode() {
        return Objects.hash(atomicConstraints, decisions);
    }

    public ConstraintNode accept(NodeVisitor visitor){
        Stream<DecisionNode> decisionNodeStream = getDecisions().stream().map(d -> d.accept(visitor));

        return visitor.visit(
            new ConstraintNode(
                new ArrayList<>(atomicConstraints),
                decisionNodeStream.collect(Collectors.toSet())));
    }

    public static ConstraintNode merge(Iterator<ConstraintNode> constraintNodeIterator) {
        Collection<AtomicConstraint> atomicConstraints = new ArrayList<>();
        Collection<DecisionNode> decisions = new ArrayList<>();

        while (constraintNodeIterator.hasNext()) {
            ConstraintNode constraintNode = constraintNodeIterator.next();

            atomicConstraints.addAll(constraintNode.getAtomicConstraints());
            decisions.addAll(constraintNode.getDecisions());
        }

        return new ConstraintNode(atomicConstraints, decisions);
    }

}

