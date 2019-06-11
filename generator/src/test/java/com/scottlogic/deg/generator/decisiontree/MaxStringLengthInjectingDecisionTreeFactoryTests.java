package com.scottlogic.deg.generator.decisiontree;

import com.scottlogic.deg.common.profile.Field;
import com.scottlogic.deg.common.profile.Profile;
import com.scottlogic.deg.common.profile.ProfileFields;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.FSConstraintNode;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.ProfileFSConstraintNodeFactory;
import com.scottlogic.deg.generator.restrictions.StringRestrictions;
import com.scottlogic.deg.generator.restrictions.StringRestrictionsFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MaxStringLengthInjectingDecisionTreeFactoryTests {
    @Test
    public void analyse_withMultipleFields_shouldAddMaxLengthConstraintToEveryField(){
        ProfileFSConstraintNodeFactory underlyingFactory = mock(ProfileFSConstraintNodeFactory.class);
        MaxStringLengthInjectingDecisionTreeFactory factory = new MaxStringLengthInjectingDecisionTreeFactory(
            underlyingFactory,
            1000);
        Profile profile = mock(Profile.class);
        ProfileFields fields = new ProfileFields(Collections.singletonList(new Field("field 1")));
        DecisionTree underlyingTree = new DecisionTree(
            new FSConstraintNode(new HashMap<>(), Collections.emptySet()),
            fields,
            "description"
        );
        when(underlyingFactory.create(profile)).thenReturn(underlyingTree);

        DecisionTree result = factory.create(profile);
        StringRestrictions stringRestrictions = result.getRootNode().getFieldSpecs().values().iterator().next().getStringRestrictions();

        StringRestrictions expected = new StringRestrictionsFactory().forMaxLength(1000);
        assertThat(stringRestrictions, sameBeanAs(expected));

    }
}