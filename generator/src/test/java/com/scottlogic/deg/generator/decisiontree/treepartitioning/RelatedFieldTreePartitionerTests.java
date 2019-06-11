package com.scottlogic.deg.generator.decisiontree.treepartitioning;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.ProfileFields;
import com.scottlogic.deg.common.profile.constraints.atomic.IsInSetConstraint;
import com.scottlogic.deg.common.profile.RuleInformation;
import com.scottlogic.deg.common.profile.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.generator.decisiontree.*;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSDecisionNode;
import com.scottlogic.deg.generator.decisiontree.testutils.*;
import com.scottlogic.deg.generator.decisiontree.testutils.EqualityComparer;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.restrictions.SetRestrictions;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;


class RelatedFieldTreePartitionerTests {
    private static final FSConstraintNode emptyConstraint
        = new FSConstraintNode(new HashMap<>(), Collections.emptySet());

    @Test
    void shouldSplitTreeIntoPartitions() {
        givenTree(
            tree(fields("A", "B", "C", "D", "E", "F"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B")
                    ),
                    decision(
                        constraint("C"),
                        constraint("D")
                    ),
                    decision(
                        constraint("E"),
                        constraint("F")
                    )
        )));

        expectTrees(
            tree(fields("A", "B"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B")
                    ))),
            tree(fields("C", "D"),
                constraint(
                    decision(
                        constraint("C"),
                        constraint("D")
                    ))),
            tree(fields("E", "F"),
                constraint(
                    decision(
                        constraint("E"),
                        constraint("F")
                    ))
        ));
    }

    @Test
    void shouldPartitionTwoNodesCorrectly() {
        givenTree(
            tree(fields("A", "B", "C", "D", "E", "F"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B"),
                        constraint("E")
                    ),
                    decision(
                        constraint("C"),
                        constraint("D")
                    ),
                    decision(
                        constraint("E"),
                        constraint("F")
                    )
        )));

        expectTrees(
            tree(fields("A", "B", "E", "F"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B"),
                        constraint("E")
                    ),
                    decision(
                        constraint("E"),
                        constraint("F")
                    ))),
            tree(fields("C", "D"),
                constraint(
                    decision(
                        constraint("C"),
                        constraint("D")
                    ))
            ));
    }

    @Test
    void shouldNotPartition() {
        givenTree(
            tree(fields("A", "B", "C", "D", "E", "F", "G"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B"),
                        constraint("C")
                    ),
                    decision(
                        constraint("C"),
                        constraint("D"),
                        constraint("E")
                    ),
                    decision(
                        constraint("E"),
                        constraint("F"),
                        constraint("G")
                    )
        )));

        expectTrees(
            tree(fields("A", "B", "C", "D", "E", "F", "G"),
                constraint(
                    decision(
                        constraint("A"),
                        constraint("B"),
                        constraint("C")
                    ),
                    decision(
                        constraint("C"),
                        constraint("D"),
                        constraint("E")
                    ),
                    decision(
                        constraint("E"),
                        constraint("F"),
                        constraint("G")
                    )
                )
            ));
    }

    @Test
    void shouldPartitionConstraintsCorrectly() {
        givenTree(
            tree(fields("A", "B", "C"),
                constraint(
                    new String[] {"A", "B", "C"},
                    decision(constraint("A")),
                    decision(constraint("B")),
                    decision(constraint("C"))
                )
        ));

        expectTrees(
            tree(fields("A"),
                constraint(
                    new String[] {"A"},
                    decision(constraint("A"))
                )),
            tree(fields("B"),
                constraint(
                    new String[] {"B"},
                    decision(constraint("B"))
                )),
            tree(fields("C"),
                constraint(
                    new String[] {"C"},
                    decision(constraint("C"))
                ))
        );
    }

    @Test
    void shouldNotErrorIfFieldsNotConstrained() {
        givenTree(
            tree(fields("A", "B"),
                constraint("A")));

        expectTrees(
            tree(fields("A"),
                constraint("A")),
            tree(fields("B"),
                emptyConstraint));
    }

    @Test
    void shouldNotErrorIfNoFieldsConstrained() {
        givenTree(
            tree(fields("A", "B", "C"),
                emptyConstraint));

        expectTrees(
            tree(fields("A"), emptyConstraint),
            tree(fields("B"), emptyConstraint),
            tree(fields("C"), emptyConstraint));
    }

    private FSConstraintNode constraint(String... fieldNames) {
        return constraint(fieldNames, new FSDecisionNode[0]);
    }

    private FSConstraintNode constraint(FSDecisionNode... decisions) {
        return constraint(new String[0], decisions);
    }

    private FSConstraintNode constraint(String[] fieldNames, FSDecisionNode... decisions) {
        return new FSConstraintNode(
            Stream.of(fieldNames).collect(
                Collectors.toMap(
                    Field::new,
                    field->FieldSpec.Empty.withSetRestrictions(SetRestrictions.fromWhitelist(Collections.singleton(field))))),
            Arrays.asList(decisions));
    }

    private AtomicConstraint atomicConstraint(String fieldName) {
        AtomicConstraint constraint = this.constraints.get(fieldName);

        if (constraint == null) {
            constraint = new IsInSetConstraint(new Field(fieldName), Collections.singleton("sample-value"), rules());
            this.constraints.put(fieldName, constraint);
        }

        return constraint;
    }

    private FSDecisionNode decision(FSConstraintNode... constraints) {
        return new FSDecisionNode(Arrays.asList(constraints));
    }

    private ProfileFields fields(String... fieldNames) {
        return new ProfileFields(
            Stream.of(fieldNames)
                .map(Field::new)
                .collect(Collectors.toList()));
    }

    private DecisionTree tree(ProfileFields fields, FSConstraintNode rootNode) {
        return new DecisionTree(rootNode, fields, "Decision Tree");
    }

    @BeforeEach
    void beforeEach() {
        constraints = new HashMap<>();
        decisionTree = null;
    }

    private Map<String, AtomicConstraint> constraints;
    private DecisionTree decisionTree;

    private void givenTree(DecisionTree decisionTree) {
        this.decisionTree = decisionTree;
    }

    private void expectTrees(DecisionTree... decisionTrees) {
        List<DecisionTree> partitionedTrees = new RelatedFieldTreePartitioner()
            .splitTreeIntoPartitions(decisionTree)
            .collect(Collectors.toList());

        assertThat(partitionedTrees, sameBeanAs(Arrays.asList(decisionTrees)));
    }

    private static Set<RuleInformation> rules(){
        return Collections.singleton(new RuleInformation());
    }
}
