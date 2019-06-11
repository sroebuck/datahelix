package com.scottlogic.deg.generator.decisiontree.treepartitioning;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.ProfileFields;
import com.scottlogic.deg.common.profile.constraints.atomic.IsInSetConstraint;
import com.scottlogic.deg.common.profile.RuleInformation;
import com.scottlogic.deg.common.profile.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.generator.decisiontree.DecisionNode;
import com.scottlogic.deg.generator.decisiontree.DecisionTree;
import com.scottlogic.deg.generator.decisiontree.ConstraintNode;
import com.scottlogic.deg.generator.decisiontree.DecisionNode;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

class ConstraintToFieldMapperTests {
    @Test
    void shouldFindConstraintMappings() {
        givenFields("A");

        final AtomicConstraint constraint = new IsInSetConstraint(new Field("A"), Collections.singleton("test-value"), rules());
        givenConstraints(constraint);
        givenFields("A");

        expectMapping(constraint, "A");
    }

    @Test
    void shouldFindRootDecisionNodeMapping() {
        givenFields("B");

        final AtomicConstraint constraint = new IsInSetConstraint(new Field("B"), Collections.singleton("test-value"), rules());
        final DecisionNode decision = new DecisionNode(
            new ConstraintNode(constraint));

        givenDecisions(decision);

        expectMapping(decision, "B");
    }

    @Test
    void shouldCreateCorrectNumberOfMappings() {
        givenFields("A", "B", "C");

        final AtomicConstraint constraintA = new IsInSetConstraint(new Field("A"), Collections.singleton("test-value"), rules());
        final AtomicConstraint constraintB = new IsInSetConstraint(new Field("B"), Collections.singleton("test-value"), rules());
        final AtomicConstraint constraintC = new IsInSetConstraint(new Field("C"), Collections.singleton("test-value"), rules());

        givenConstraints(constraintA, constraintB, constraintC);

        expectMappingCount(3);
    }

    @Test
    void shouldMapTopLevelConstraintsToNestedFields() {
        givenFields("A", "B", "C", "D", "E", "F");

        final AtomicConstraint constraintA = new IsInSetConstraint(new Field("A"), Collections.singleton("test-value"), rules());
        final AtomicConstraint constraintB = new IsInSetConstraint(new Field("B"), Collections.singleton("test-value"), rules());
        final AtomicConstraint constraintC = new IsInSetConstraint(new Field("C"), Collections.singleton("test-value"), rules());
        final AtomicConstraint constraintD = new IsInSetConstraint(new Field("D"), Collections.singleton("test-value"), rules());
        final AtomicConstraint constraintE = new IsInSetConstraint(new Field("E"), Collections.singleton("test-value"), rules());
        final AtomicConstraint constraintF = new IsInSetConstraint(new Field("F"), Collections.singleton("test-value"), rules());

        final DecisionNode decisionABC = new DecisionNode(
            new ConstraintNode(
                Collections.emptyList(),
                Arrays.asList(
                    new DecisionNode(new ConstraintNode(constraintA)),
                    new DecisionNode(new ConstraintNode(constraintB)),
                    new DecisionNode(new ConstraintNode(constraintC))
                )
            )
        );

        final DecisionNode decisionDEF = new DecisionNode(
            new ConstraintNode(
                Collections.emptyList(),
                Collections.singletonList(
                    new DecisionNode(
                        new ConstraintNode(constraintD),
                        new ConstraintNode(constraintE),
                        new ConstraintNode(constraintF))
                )
            )
        );

        givenDecisions(decisionABC, decisionDEF);

        expectMapping(decisionABC, "A", "B", "C");
        expectMapping(decisionDEF, "D", "E", "F");
    }

    @BeforeEach
    void beforeEach() {
        constraintsList = new ArrayList<>();
        decisionsList = new ArrayList<>();
        fields = new ProfileFields(Collections.emptyList());
        mappings = null;
    }

    private List<AtomicConstraint> constraintsList;
    private List<DecisionNode> decisionsList;
    private ProfileFields fields;
    private Map<RootLevelConstraint, Set<Field>> mappings;

    private void givenConstraints(AtomicConstraint... constraints) {
        constraintsList = Arrays.asList(constraints);
    }

    private void givenDecisions(DecisionNode... decisions) {
        decisionsList = Arrays.asList(decisions);
    }

    private void givenFields(String... fieldNames) {
        fields = new ProfileFields(
            Arrays.stream(fieldNames)
                .map(Field::new)
                .collect(Collectors.toList()));
    }

    private void getMappings() {
//        mappings = new ConstraintToFieldMapper()
//            .mapConstraintsToFields(new DecisionTree(
//                new ConstraintNode(constraintsList, decisionsList),
//                fields,
//                "Decision Tree"
//            ));
    }

    private void expectMapping(AtomicConstraint constraint, String... fieldsAsString) {
        if (mappings == null)
            getMappings();

        final Field[] fields = Arrays.stream(fieldsAsString)
            .map(Field::new)
            .toArray(Field[]::new);

//        Assert.assertThat(mappings.get(new RootLevelConstraint(constraint)), Matchers.hasItems(fields));
    }

    private void expectMapping(DecisionNode decision, String... fieldsAsString) {
        if (mappings == null)
            getMappings();

        final Field[] fields = Arrays.stream(fieldsAsString)
            .map(Field::new)
            .toArray(Field[]::new);

//        Assert.assertThat(mappings.get(new RootLevelConstraint(decision)), Matchers.hasItems(fields));
    }

    private void expectMappingCount(int mappingsCount) {
        if (mappings == null)
            getMappings();

        Assert.assertThat(mappings, Matchers.aMapWithSize(mappingsCount));
    }

    private static Set<RuleInformation> rules(){
        return Collections.singleton(new RuleInformation());
    }
}
