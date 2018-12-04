package com.scottlogic.deg.generator;

import com.scottlogic.deg.generator.decisiontree.NoopDecisionTreeOptimiser;
import com.scottlogic.deg.generator.decisiontree.tree_partitioning.NoopTreePartitioner;
import com.scottlogic.deg.generator.generation.DataGenerator;
import com.scottlogic.deg.generator.generation.GenerationConfig;
import com.scottlogic.deg.generator.inputs.InvalidProfileException;
import com.scottlogic.deg.generator.outputs.dataset_writers.CsvDataSetWriter;
import com.scottlogic.deg.generator.outputs.targets.DirectoryOutputTarget;
import com.scottlogic.deg.generator.walker.DecisionTreeWalkerFactory;
import com.scottlogic.deg.generator.walker.RuntimeDecisionTreeWalkerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@CommandLine.Command(
    name = "generateTestCases",
    description = "Generates valid and violating data using a profile file.",
    mixinStandardHelpOptions = true,
    version = "1.0")
public class GenerateTestCases implements Runnable {
    @CommandLine.Parameters(index = "0", description = "The path of the profile json file.")
    private File profileFile;

    @CommandLine.Parameters(index = "1", description = "The directory into which generated data should be saved.")
    private Path outputDir;

    @CommandLine.Option(names = {"-t", "--t"},
        description = "Determines the type of data generation performed (FULL_SEQUENTIAL, INTERESTING, RANDOM).",
        defaultValue = "INTERESTING")
    private GenerationConfig.DataGenerationType generationType = GenerationConfig.DataGenerationType.INTERESTING;

    @CommandLine.Option(names = {"-c", "--c"},
        description = "Determines the type of combination strategy used (pinning, exhaustive, minimal).",
        defaultValue = "PINNING")
    private GenerationConfig.CombinationStrategyType combinationType = GenerationConfig.CombinationStrategyType.PINNING;

    @CommandLine.Option(names = {"-w", "--w"},
        description = "Determines the tree walker that should be used.",
        defaultValue = "CARTESIAN_PRODUCT",
        hidden = true)
    private GenerationConfig.TreeWalkerType walkerType = GenerationConfig.TreeWalkerType.CARTESIAN_PRODUCT;

    @Override
    public void run() {
        GenerationConfig config = new GenerationConfig(
            generationType,
            walkerType,
            combinationType);

        DecisionTreeWalkerFactory walkerFactory = new RuntimeDecisionTreeWalkerFactory(config);

        try {
            new GenerationEngine(
                new DirectoryOutputTarget(outputDir, new CsvDataSetWriter()),
                new DataGenerator(
                    walkerFactory.getDecisionTreeWalker(),
                    new NoopTreePartitioner(),
                    new NoopDecisionTreeOptimiser()))
                .generateTestCases(profileFile.toPath(), config);
        } catch (IOException | InvalidProfileException e) {
            e.printStackTrace();
        }
    }
}
