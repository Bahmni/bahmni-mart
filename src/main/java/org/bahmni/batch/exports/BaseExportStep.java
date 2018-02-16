package org.bahmni.batch.exports;

import org.bahmni.batch.BatchUtils;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.PassThroughFieldExtractor;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ColumnMapRowMapper;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;


public class BaseExportStep {
    private DataSource dataSource;

    private StepBuilderFactory stepBuilderFactory;

    private Resource sqlResource;

    private Resource outputFolder;

    private String exportName;

    private String headers;

    private String sql;
    private static final String DELIMITER = ",";

    public BaseExportStep(StepBuilderFactory stepBuilderFactory, DataSource dataSource, Resource sqlResource, Resource outputFolder, String exportName, String headers) {
        this.dataSource = dataSource;
        this.stepBuilderFactory = stepBuilderFactory;
        this.sqlResource = sqlResource;
        this.outputFolder = outputFolder;
        this.exportName = exportName;
        this.headers = headers;
    }

    public Step getStep() {
        return stepBuilderFactory
                .get(exportName)
                .<String, String>chunk(50)
                .reader(jdbcItemReader())
                .writer(flatFileItemWriter())
                .build();
    }

    private JdbcCursorItemReader jdbcItemReader() {
        JdbcCursorItemReader reader = new JdbcCursorItemReader();
        reader.setDataSource(dataSource);
        reader.setSql(sql);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }

    private FlatFileItemWriter<String> flatFileItemWriter() {
        FlatFileItemWriter<String> writer = new FlatFileItemWriter<String>();
        writer.setResource(outputFolder);
        DelimitedLineAggregator delimitedLineAggregator = new DelimitedLineAggregator();
        delimitedLineAggregator.setDelimiter(DELIMITER);
        delimitedLineAggregator.setFieldExtractor(new PassThroughFieldExtractor());
        writer.setLineAggregator(delimitedLineAggregator);
        writer.setHeaderCallback(w -> w.write(getHeaders()));
        return writer;
    }

    public String getHeaders() {
        return headers;
    }

    @PostConstruct
    public void postConstruct() {
        this.sql = BatchUtils.convertResourceOutputToString(sqlResource);
    }
}
