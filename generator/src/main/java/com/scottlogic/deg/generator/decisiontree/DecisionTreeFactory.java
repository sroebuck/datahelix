package com.scottlogic.deg.generator.decisiontree;

import com.scottlogic.deg.common.profile.Profile;

public interface DecisionTreeFactory {
    DecisionTree create(Profile profile);
}
