package com.scottlogic.deg.generator.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.scottlogic.deg.generator.generation.GenerationConfigSource;
import com.scottlogic.deg.generator.outputs.datasetwriters.*;
import org.apache.kafka.clients.producer.KafkaProducer;

public class DataSetWriterProvider implements Provider<DataSetWriter> {
    private final GenerationConfigSource configSource;
    private final CsvDataSetWriter csvWriter;
    private final JsonDataSetWriter jsonWriter;
    private final KafkaDataSetWriter kafkaDataSetWriter;

    @Inject
    public DataSetWriterProvider(GenerationConfigSource configSource, CsvDataSetWriter csvWriter, JsonDataSetWriter jsonWriter,
                                 KafkaDataSetWriter kafkaDataSetWriter) {
        this.configSource = configSource;
        this.csvWriter = csvWriter;
        this.jsonWriter = jsonWriter;
        this.kafkaDataSetWriter = kafkaDataSetWriter;
    }

    @Override
    public DataSetWriter get() {
        DataSetWriter outputWriter = getBasicDataSetWriter();

        if (configSource.isEnableTracing()) {
            return new MultiDataSetWriter(outputWriter, new SourceTracingDataSetWriter());
        }
        return outputWriter;
    }

    private DataSetWriter getBasicDataSetWriter() {
        switch (configSource.getOutputFormat()){
            case CSV:
                return csvWriter;
            case JSON:
                return jsonWriter;
            case KAFKA:
                return kafkaDataSetWriter;
        }

        throw new RuntimeException(String.format("Unknown output format %s, options are CSV or JSON", configSource.getOutputFormat()));
    }
}
