package com.scottlogic.deg.generator.decisiontree.treepartitioning;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.util.FlatMappingSpliterator;
import com.scottlogic.deg.generator.decisiontree.ConstraintNode;
import com.scottlogic.deg.generator.decisiontree.DecisionTree;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSDecisionNode;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Given a decision tree, find which constraints and decisions act on which fields and return a map from them to fields
 */
class ConstraintToFieldMapper {

    private class ConstraintToFields {
        public RootLevelConstraint constraint;
        public Set<Field> fields;

        ConstraintToFields(RootLevelConstraint constraint, Set<Field> fields) {
            this.constraint = constraint;
            this.fields = fields;
        }

        ConstraintToFields(RootLevelConstraint constraint, Field field) {
            this.constraint = constraint;
            this.fields = Collections.singleton(field);
        }
    }

    Map<RootLevelConstraint, Set<Field>> mapConstraintsToFields(DecisionTree decisionTree){
        return mapConstraintToFields(decisionTree.getRootNode())
            .collect(
                Collectors.toMap(
                    map -> map.constraint,
                    map -> map.fields
                ));
    }

    private Set<Field> getFieldsForDecision(FSDecisionNode decision) {
        return decision.getOptions().stream()
            .flatMap(this::mapConstraintToFields)
            .flatMap(objectField -> objectField.fields.stream())
            .collect(Collectors.toSet());
    }

    private Stream<ConstraintToFields> mapConstraintToFields(FSConstraintNode node) {
        return Stream.concat(
            node.getFieldSpecs().entrySet()
                .stream()
                .map(fieldToSpec -> new ConstraintToFields(new RootLevelConstraint(fieldToSpec), fieldToSpec.getKey())),
            node.getDecisions()
                .stream()
                .map(decision -> new ConstraintToFields(
                    new RootLevelConstraint(decision),
                    getFieldsForDecision(decision))
                ));
    }
}
