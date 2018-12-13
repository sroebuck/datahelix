package com.scottlogic.deg.generator.decisiontree;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.Rule;
import com.scottlogic.deg.generator.inputs.RuleInformation;
import com.scottlogic.deg.generator.constraints.grammatical.ConditionalConstraint;
import com.scottlogic.deg.generator.constraints.atomic.IsEqualToConstantConstraint;
import com.scottlogic.deg.generator.constraints.atomic.IsInSetConstraint;
import com.scottlogic.deg.generator.reducer.ConstraintReducer;
import com.scottlogic.deg.generator.restrictions.FieldSpecFactory;
import com.scottlogic.deg.generator.restrictions.FieldSpecMerger;
import com.scottlogic.deg.generator.restrictions.RowSpec;
import com.scottlogic.deg.generator.restrictions.RowSpecMerger;
import com.scottlogic.deg.generator.walker.CartesianProductDecisionTreeWalker;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class DecisionTreeToRowSpecsTests {
    private final FieldSpecMerger fieldSpecMerger = new FieldSpecMerger();
    private final CartesianProductDecisionTreeWalker dTreeWalker = new CartesianProductDecisionTreeWalker(
            new ConstraintReducer(
                    new FieldSpecFactory(),
                    fieldSpecMerger
            ),
            new RowSpecMerger(
                    fieldSpecMerger
            )
    );
    private final ProfileDecisionTreeFactory dTreeGenerator = new ProfileDecisionTreeFactory();

    private ConstraintNode reduceRules(DecisionTreeCollection profile) {
        return ConstraintNode.merge(
            profile.getDecisionTrees()
                .stream()
                .map(DecisionTree::getRootNode)
                .iterator()
        );
    }

    @Test
    public void test() {
        final DecisionTreeCollection dTree = dTreeGenerator.analyse(makeProfile());
        final List<RowSpec> rowSpecs = dTreeWalker.walk(new DecisionTree(reduceRules(dTree), dTree.getFields(), "DecisionTreeToRowSpecsTests"))
                .collect(Collectors.toList());
        Assert.assertThat(rowSpecs, Is.is(IsNull.notNullValue()));
    }

    private Profile makeProfile() {
        final Field country = new Field("country");
        final Field currency = new Field("currency");
        final Field city = new Field("city");
        return new Profile(
            Arrays.asList(country, currency, city),
            Arrays.asList(
                new Rule(
                    "US country constrains city",
                    Collections.singletonList(
                        new ConditionalConstraint(
                            new IsEqualToConstantConstraint(
                                country,
                                "US",
                                rule()
                            ),
                            new IsInSetConstraint(
                                city,
                                new HashSet<>(Arrays.asList("New York", "Washington DC")),
                                rule()
                            )
                        )
                    )
                ),
                new Rule(
                    "GB country constrains city",
                    Collections.singletonList(
                        new ConditionalConstraint(
                            new IsEqualToConstantConstraint(
                                country,
                                "GB",
                                rule()
                            ),
                            new IsInSetConstraint(
                                city,
                                new HashSet<>(Arrays.asList("Bristol", "London")),
                                rule()
                            )
                        )
                    )
                ),
                new Rule(
                    "US country constrains currency",
                    Collections.singletonList(
                        new ConditionalConstraint(
                            new IsEqualToConstantConstraint(
                                country,
                                "US",
                                rule()
                            ),
                            new IsEqualToConstantConstraint(
                                currency,
                                "USD",
                                rule()
                            )
                        )
                    )
                ),
                new Rule(
                    "GB country constrains currency",
                    Collections.singletonList(
                        new ConditionalConstraint(
                            new IsEqualToConstantConstraint(
                                country,
                                "GB",
                                rule()
                            ),
                            new IsEqualToConstantConstraint(
                                currency,
                                "GBP",
                                rule()
                            )
                        )
                    )
                )
            )
        );
    }

    private static RuleInformation rule(){
        return RuleInformation.fromDescription("rule");
    }
}
