package com.scottlogic.deg.generator.inputs.validation;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.constraints.atomic.*;
import com.scottlogic.deg.generator.decisiontree.*;
import com.scottlogic.deg.generator.inputs.validation.messages.StringValidationMessage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.scottlogic.deg.generator.constraints.atomic.AtomicConstraintsHelper.getConstraintsForField;

/**
 * Rejects a profile if there are any fields that aren't positively assigned at least one type.
 * A field is assigned to a type if it is null, is in a set, or has an ofType
 * Or a decision will result in it being assigned a type this way
 */
public class TypingRequiredPerFieldValidator implements ProfileValidator {
    private final DecisionTreeFactory decisionTreeFactory;

    @Inject
    public TypingRequiredPerFieldValidator(DecisionTreeFactory decisionTreeFactory) {
        this.decisionTreeFactory = decisionTreeFactory;
    }

    @Override
    public Collection<ValidationAlert> validate(Profile profile) {
        final DecisionTree decisionTree = decisionTreeFactory.analyse(profile).getMergedTree();

        return profile.fields.stream()
            .filter(field -> !validateForField(decisionTree.getRootNode(), field))
            .map(this::toValidationAlert)
            .collect(Collectors.toList());
    }

    private boolean validateForField(ConstraintNode node, Field field) {
        List<AtomicConstraint> constraintsForField = getConstraintsForField(node.getAtomicConstraints(), field);

        if (doesAnyConstraintEnforceType(constraintsForField)) {
            return true;
        }
        return doesAnyDecisionFullyEnforceType(node.getDecisions(), field);
    }

    private boolean doesAnyConstraintEnforceType(Collection<AtomicConstraint> constraints) {
        return constraints.stream()
            .anyMatch(constraint ->
                constraint instanceof IsOfTypeConstraint
                || constraint instanceof IsNullConstraint
                || constraint instanceof IsInSetConstraint);
    }

    private boolean doesAnyDecisionFullyEnforceType(Collection<DecisionNode> decisionNodes, Field field) {
        for (DecisionNode decision : decisionNodes) {
            if (decision.getOptions().stream()
                .allMatch(subtree ->
                    validateForField(subtree, field))) {
                return true;
            }
        }
        return false;
    }

    private ValidationAlert toValidationAlert(Field nonCompliantField) {
        return new ValidationAlert(
            Criticality.ERROR,
            new StringValidationMessage(
                "Field is untyped; add an ofType, equalTo or inSet constraint, or mark it as null"),
            ValidationType.TYPE,
            nonCompliantField);
    }


}
