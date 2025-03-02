package com.scottlogic.deg.generator.generation;

import com.google.inject.Inject;
import com.scottlogic.deg.common.output.GeneratedObject;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class VelocityMonitor implements ReductiveDataGeneratorMonitor {
    private static final BigDecimal millisecondsInSecond = BigDecimal.valueOf(1_000);
    private static final BigDecimal nanoSecondsInMillisecond = BigDecimal.valueOf(1_000_000);

    private OffsetDateTime startedGenerating;
    private long rowsSinceLastSample;
    private BigInteger rowsEmitted;
    private Timer timer;
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private long previousVelocity = 0;

    private final PrintWriter writer;

    @Inject
    public VelocityMonitor(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public void generationStarting() {
        startedGenerating = OffsetDateTime.now(ZoneOffset.UTC);
        rowsSinceLastSample = 0;
        rowsEmitted = BigInteger.ZERO;

        println("Generation started at: " + timeFormatter.format(startedGenerating) + "\n");
        println("Number of rows | Velocity (rows/sec) | Velocity trend");
        println("---------------+---------------------+---------------");

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                reportVelocity(rowsSinceLastSample);
                rowsSinceLastSample = 0;
            }
        }, 1000L, 1000L);
    }
    
    @Override
    public void rowEmitted(GeneratedObject item) {
        rowsSinceLastSample++;
        rowsEmitted = rowsEmitted.add(BigInteger.ONE);
    }

    @Override
    public void endGeneration() {
        timer.cancel();

        OffsetDateTime finished = OffsetDateTime.now(ZoneOffset.UTC);
        Duration totalDuration = Duration.between(startedGenerating, finished);

        //Get the total duration of the generator in milliseconds
        BigDecimal nanoSecondsAsMilliseconds = BigDecimal
            .valueOf(totalDuration.getNano())
            .divide(nanoSecondsInMillisecond, RoundingMode.DOWN); //get the number of nanoSeconds in the duration and divide to convert them into milliseconds
        BigDecimal secondsAsMilliseconds = BigDecimal
            .valueOf(totalDuration.getSeconds()) //get the total number of seconds and multiply them by 1000 to convert them to milliseconds
            .multiply(millisecondsInSecond);
        BigDecimal totalMilliseconds = nanoSecondsAsMilliseconds.add(secondsAsMilliseconds);

        //Work out the average velocity for the generator as a whole by using the formula
        // (<rowsEmitted>/<totalMilliseconds>)*1000 = <rowsEmitted>/second
        BigInteger averageRowsPerSecond = new BigDecimal(rowsEmitted)
            .setScale(2, RoundingMode.UNNECESSARY)
            .divide(totalMilliseconds, RoundingMode.HALF_UP)
            .multiply(millisecondsInSecond).toBigInteger();

        println(
            "%-14s | %-19d | Finished",
            rowsEmitted.toString(),
            averageRowsPerSecond);

        println(
            "\nGeneration finished at: %s",
            timeFormatter.format(finished));
    }

    private void reportVelocity(long rowsSinceLastSample) {
        String trend = rowsSinceLastSample > previousVelocity ? "+" : "-";
        println(
            "%-14s | %-19d | %s",
            rowsEmitted.toString(),
            rowsSinceLastSample,
            trend);
        previousVelocity = rowsSinceLastSample;
    }

    private void println(String message) {
        writer.println(message);
    }

    private void println(String message, Object... args) {
        writer.format(message, args);
        writer.println();
    }
}
