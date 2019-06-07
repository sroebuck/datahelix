package com.scottlogic.deg.orchestrator.cucumber.testframework.utils;

import com.google.inject.Inject;
import com.scottlogic.deg.common.profile.Profile;
import com.scottlogic.deg.generator.config.detail.*;
import com.scottlogic.deg.orchestrator.guice.AllConfigSource;
import com.scottlogic.deg.orchestrator.violate.ViolateConfigSource;
import com.scottlogic.deg.output.guice.OutputFormat;
import com.scottlogic.deg.profile.v0_1.AtomicConstraintType;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class CucumberGenerationConfigSource implements AllConfigSource, ViolateConfigSource {

    private final CucumberTestState state;

    private static final File MOCK_FILE = new File("mockFilePath");

    private static final Path MOCK_PATH = MOCK_FILE.toPath();

    @Inject
    public CucumberGenerationConfigSource(CucumberTestState state) {
        this.state = state;
    }

    @Override
    public boolean requireFieldTyping() {
        return state.requireFieldTyping;
    }

    @Override
    public DataGenerationType getGenerationType() {
        return state.dataGenerationType;
    }

    @Override
    public CombinationStrategyType getCombinationStrategyType() {
        return state.combinationStrategyType;
    }

    @Override
    public TreeWalkerType getWalkerType() {
        return state.walkerType;
    }

    @Override
    public List<AtomicConstraintType> getConstraintsToNotViolate() {
        return state.getConstraintsToNotViolate();
    }

    @Override
    public MonitorType getMonitorType() {
        return MonitorType.QUIET;
    }

    @Override
    public long getMaxRows() {
        return state.maxRows;
    }

    @Override
    public boolean shouldDoPartitioning() {
        return false;
    }

    @Override
    public boolean dontOptimise() {
        return false;
    }

    @Override
    public Path getOutputPath() {
        return MOCK_PATH;
    }

    @Override
    public boolean isEnableTracing() {
        return false;
    }

    @Override
    public File getProfileFile() {
        return MOCK_FILE;
    }

    @Override
    public boolean isSchemaValidationEnabled() {
        return false;
    }

    @Override
    public Path getFirstNamePath() {
        return MOCK_PATH;
    }

    @Override
    public Path getLastNamePath() {
        return MOCK_PATH;
    }

    @Override
    public boolean overwriteOutputFiles() {
        return false;
    }

    @Override
    public boolean visualiseReductions() {
        return false;
    }

    @Override
    public OutputFormat getOutputFormat() {
        return null;
    }
}
