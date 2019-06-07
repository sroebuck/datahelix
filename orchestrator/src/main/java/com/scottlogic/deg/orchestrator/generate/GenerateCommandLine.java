package com.scottlogic.deg.orchestrator.generate;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.scottlogic.deg.generator.config.detail.*;
import com.scottlogic.deg.orchestrator.guice.AllConfigSource;
import com.scottlogic.deg.orchestrator.guice.AllModule;
import com.scottlogic.deg.output.guice.OutputFormat;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;

import static com.scottlogic.deg.generator.config.detail.CombinationStrategyType.MINIMAL;
import static com.scottlogic.deg.common.util.Defaults.DEFAULT_MAX_ROWS;
import static com.scottlogic.deg.generator.config.detail.DataGenerationType.RANDOM;
import static com.scottlogic.deg.output.guice.OutputFormat.CSV;
import static com.scottlogic.deg.generator.config.detail.TreeWalkerType.REDUCTIVE;

/**
 * This class holds the generate specific command line options.
 */
@picocli.CommandLine.Command(
    name = "generate",
    description = "Produces data using any options provided.",
    descriptionHeading = "%nDescription:%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    abbreviateSynopsis = true)
public class GenerateCommandLine implements AllConfigSource, Runnable {

    @Override
    public void run() {
        Module container = new AllModule(this);
        Injector injector = Guice.createInjector(container);

        Runnable task = injector.getInstance(GenerateExecute.class);

        task.run();
    }

    @CommandLine.Parameters(index = "0", description = "The path of the profile json file.")
    File profileFile;

    @CommandLine.Option(
        names = {"--no-optimise"},
        description = "Prevents tree optimisation",
        hidden = true)
    boolean dontOptimise;

    @CommandLine.Option(
        names = "--help",
        usageHelp = true,
        description = "Display these available command line options")
    boolean help;

    @CommandLine.Option(
        names = {"--replace"},
        description = "Defines whether to overwrite/replace existing output files")
    boolean overwriteOutputFiles = false;

    @CommandLine.Option(
        names = { "--enable-schema-validation" },
        description = "Enables schema validation")
    boolean enableSchemaValidation = false;

    @CommandLine.Parameters(index = "1", description = "The path to write the generated data file to.")
    private Path outputPath;

    @CommandLine.Option(names = {"-t", "--generation-type"},
        description = "Determines the type of data generation performed (${COMPLETION-CANDIDATES})",
        hidden = true)
    private DataGenerationType generationType = RANDOM;

    @CommandLine.Option(names = {"-c", "--combination-strategy"},
        description = "Determines the type of combination strategy used (${COMPLETION-CANDIDATES})",
        hidden = true)
    @SuppressWarnings("unused")
    private CombinationStrategyType combinationType = MINIMAL;

    @CommandLine.Option(
        names = {"--no-partition"},
        description = "Prevents tree partitioning",
        hidden = true)
    private boolean dontPartitionTrees;

    @CommandLine.Option(names = {"-w", "--walker-type"},
        description = "Determines the tree walker that should be used (${COMPLETION-CANDIDATES})",
        hidden = true)
    private TreeWalkerType walkerType = REDUCTIVE;

    @CommandLine.Option(
        names = {"-n", "--max-rows"},
        description = "Defines the maximum number of rows that should be generated")
    private long maxRows = DEFAULT_MAX_ROWS;

    @CommandLine.Option(
        names = {"--trace-constraints"},
        description = "Defines whether constraint tracing is enabled for the output",
        hidden = true)
    private boolean enableTracing;

    @CommandLine.Option(
        names = {"--quiet"},
        description = "Turns OFF default monitoring")
    private Boolean quiet = false;

    @CommandLine.Option(
        names = {"--verbose"},
        description = "Turns ON system out monitoring")
    private Boolean verbose = false;

    @CommandLine.Option(
        names = {"--visualise-reductions"},
        description = "Visualise each tree reduction",
        hidden = true)
    private Boolean visualiseReductions = false;

    @CommandLine.Option(
        names = {"-o"},
        description = "Output format (${COMPLETION-CANDIDATES})")
    private OutputFormat outputFormat = CSV;

    @CommandLine.Option(
        names = {"--allow-untyped-fields"},
        description = "Remove the need for each field to have at least one compliant typing constraint applied")
    private boolean allowUntypedFields = false;

    @CommandLine.Option(
        names = {"--first-name-source"},
        description = "The path of a first name CSV to override the default first names")
    private Path firstNameSource;

    @CommandLine.Option(
        names = {"--lst-name-source"},
        description = "The path of a first name CSV to override the default lst names")
    private Path lastNameSource;

    public boolean shouldDoPartitioning() {
        return !this.dontPartitionTrees;
    }

    @Override
    public boolean dontOptimise() {
        return this.dontOptimise;
    }

    @Override
    public File getProfileFile() {
        return this.profileFile;
    }

    @Override
    public boolean isSchemaValidationEnabled() {
        return this.enableSchemaValidation;
    }

    @Override
    public Path getFirstNamePath() {
        return firstNameSource;
    }

    @Override
    public Path getLastNamePath() {
        return lastNameSource;
    }

    @Override
    public boolean overwriteOutputFiles() {
        return this.overwriteOutputFiles;
    }

    @Override
    public Path getOutputPath() {
        return this.outputPath;
    }

    @Override
    public boolean isEnableTracing() {
        return this.enableTracing;
    }

    @Override
    public DataGenerationType getGenerationType() {
        return this.generationType;
    }

    @Override
    public CombinationStrategyType getCombinationStrategyType() {
        return this.combinationType;
    }

    @Override
    public TreeWalkerType getWalkerType() {
        return this.walkerType;
    }

    @Override
    public boolean requireFieldTyping(){
        return !allowUntypedFields;
    }

    @Override
    public MonitorType getMonitorType() {
        if (this.verbose) {
            return MonitorType.VERBOSE;
        }
        if (this.quiet) {
            return MonitorType.QUIET;
        }
        return MonitorType.STANDARD;
    }

    @Override
    public long getMaxRows() {
        return maxRows;
    }

    @Override
    public boolean visualiseReductions() {
        return visualiseReductions;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }
}
