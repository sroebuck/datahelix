package com.scottlogic.deg.orchestrator.cucumber.testframework.utils;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.decisiontree.FieldSpecTree.ProfileFSConstraintNodeFactory;
import com.scottlogic.deg.generator.decisiontree.MaxStringLengthInjectingDecisionTreeFactory;

public class CucumberDecisionTreeFactory extends MaxStringLengthInjectingDecisionTreeFactory {
    @Inject
    public CucumberDecisionTreeFactory(ProfileFSConstraintNodeFactory underlyingFactory, CucumberTestState state) {
        super(underlyingFactory, state.getMaxStringLength());
    }
}
