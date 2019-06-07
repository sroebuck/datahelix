package com.scottlogic.deg.generator.decisiontree;

import com.google.inject.Inject;
import com.scottlogic.deg.common.profile.Profile;
import com.scottlogic.deg.common.profile.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.common.profile.constraints.atomic.IsStringShorterThanConstraint;
import com.scottlogic.deg.common.util.Defaults;
import com.scottlogic.deg.common.profile.RuleInformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Decorator over a DecisionTreeFactory to inject a &lt;shorterThan X&gt; constraint at the root node for every field
 */
public class MaxStringLengthInjectingDecisionTreeFactory implements DecisionTreeFactory{
    private final int maxLength;
    private final DecisionTreeFactory underlyingFactory;

    @Inject
    public MaxStringLengthInjectingDecisionTreeFactory(ProfileDecisionTreeFactory underlyingFactory) {
        this(underlyingFactory, Defaults.MAX_STRING_LENGTH);
    }

    public MaxStringLengthInjectingDecisionTreeFactory(DecisionTreeFactory underlyingFactory, int maxLength) {
        this.underlyingFactory = underlyingFactory;
        this.maxLength = maxLength;
    }

    @Override
    public DecisionTree analyse(Profile profile) {
        DecisionTree tree = underlyingFactory.analyse(profile);

        Set<RuleInformation> rules = Collections.singleton(createRule());

        List<AtomicConstraint> collect = tree.fields
            .stream()
            .map(field -> new IsStringShorterThanConstraint(field, maxLength + 1, rules))
            .map(a -> (AtomicConstraint) a)
            .collect(Collectors.toList());

        ArrayList<AtomicConstraint> atomicConstraints = new ArrayList<>(tree.rootNode.getAtomicConstraints());
        atomicConstraints.addAll(collect);

        return new DecisionTree(
            new ConstraintNode(atomicConstraints, tree.rootNode.getDecisions()),
            tree.fields,
            tree.description
        );
    }

    private RuleInformation createRule() {
        return new RuleInformation("Auto-injected: String-max-length");
    }
}
