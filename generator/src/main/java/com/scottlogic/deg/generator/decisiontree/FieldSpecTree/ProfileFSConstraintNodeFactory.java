package com.scottlogic.deg.generator.decisiontree.FieldSpecTree;

import com.google.inject.Inject;
import com.scottlogic.deg.common.ValidationException;
import com.scottlogic.deg.common.profile.Profile;
import com.scottlogic.deg.common.profile.constraints.Constraint;
import com.scottlogic.deg.common.profile.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.AndConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.ConditionalConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.NegatedGrammaticalConstraint;
import com.scottlogic.deg.common.profile.constraints.grammatical.OrConstraint;
import com.scottlogic.deg.generator.reducer.ConstraintReducer;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfileFSConstraintNodeFactory {
    private final ConstraintReducer constraintReducer;

    @Inject
    public ProfileFSConstraintNodeFactory(ConstraintReducer constraintReducer) {
        this.constraintReducer = constraintReducer;
    }

    public FSConstraintNode create(Profile profile){
        Set<Constraint> constraints = profile.getRules().stream()
            .flatMap(rule -> rule.constraints.stream())
            .collect(Collectors.toSet());

        return convertAndConstraint(new AndConstraint(constraints));
    }

    private FSConstraintNode convertAndConstraint(AndConstraint andConstraint) {
        ConstraintNodeDetails constraintNodeDetails = getConstraintNodeDetails(andConstraint.subConstraints);
        constraintReducer.reduceConstraintsToRowSpec()
        return null;
    }

    private ConstraintNodeDetails getConstraintNodeDetails(Collection<Constraint> subConstraints) {
        ConstraintNodeDetails acc = new ConstraintNodeDetails();

        for (Constraint subConstraint : subConstraints) {
            if (subConstraint instanceof AtomicConstraint) {
                acc.atomics.add((AtomicConstraint) subConstraint);
            }
            else if (subConstraint instanceof ConditionalConstraint){
                acc.conditionals.add((ConditionalConstraint) subConstraint);
            }
            else if (subConstraint instanceof OrConstraint){
                acc.orConstraints.add((OrConstraint) subConstraint);
            }
            else if (subConstraint instanceof AndConstraint){
                ConstraintNodeDetails sub = getConstraintNodeDetails(((AndConstraint) subConstraints).subConstraints);
                acc.atomics.addAll(sub.atomics);
                acc.conditionals.addAll(sub.conditionals);
                acc.orConstraints.addAll(sub.orConstraints);
            }
            else if (subConstraint instanceof NegatedGrammaticalConstraint){
                ConstraintNodeDetails sub = getConstraintNodeDetails(Arrays.asList(
                    getNegatedConstraint(((NegatedGrammaticalConstraint) subConstraint).negatedConstraint)));
                acc.atomics.addAll(sub.atomics);
                acc.conditionals.addAll(sub.conditionals);
                acc.orConstraints.addAll(sub.orConstraints);
            }
            else {
                throw new ValidationException("unexpected constraint in profile");
            }
        }
        return acc;
    }

    private Constraint getNegatedConstraint(Constraint negatedConstraint) {
        // ¬AND(X, Y, Z) reduces to OR(¬X, ¬Y, ¬Z)
        if (negatedConstraint instanceof AndConstraint) {
            Collection<Constraint> subConstraints = ((AndConstraint) negatedConstraint).subConstraints;

            return new OrConstraint(negateEach(subConstraints));
        }
        // ¬OR(X, Y, Z) reduces to AND(¬X, ¬Y, ¬Z)
        else if (negatedConstraint instanceof OrConstraint) {
            Collection<Constraint> subConstraints = ((OrConstraint) negatedConstraint).subConstraints;

            return new AndConstraint(negateEach(subConstraints));
        }
        // ¬IF(X, then: Y) reduces to AND(X, ¬Y)
        // ¬IF(X, then: Y, else: Z) reduces to OR(AND(X, ¬Y), AND(¬X, ¬Z))
        if (negatedConstraint instanceof ConditionalConstraint){
            ConditionalConstraint conditional = (ConditionalConstraint) negatedConstraint;

            AndConstraint positiveNegation =
                new AndConstraint(conditional.condition, conditional.whenConditionIsTrue.negate());

            if (conditional.whenConditionIsFalse == null) {
                return positiveNegation;
            }

            Constraint negativeNegation =
                new AndConstraint(conditional.condition.negate(), conditional.whenConditionIsFalse.negate());

            return new OrConstraint(positiveNegation, negativeNegation);
        }
        if (negatedConstraint instanceof NegatedGrammaticalConstraint){
            return ((NegatedGrammaticalConstraint) negatedConstraint).negatedConstraint;
        }
        if (negatedConstraint instanceof AtomicConstraint) {
            return negatedConstraint.negate();
        }
        throw new ValidationException("unexpected constraint in profile");
    }

    private static Collection<Constraint> negateEach(Collection<Constraint> constraints) {
        return constraints.stream()
            .map(Constraint::negate)
            .collect(Collectors.toList());
    }

    private class ConstraintNodeDetails {
        Set<AtomicConstraint> atomics = new HashSet<>();
        Set<ConditionalConstraint> conditionals = new HashSet<>();
        Set<OrConstraint> orConstraints = new HashSet<>();
    }
}
