package org.bahmni.batch.exports;

import org.apache.commons.io.IOUtils;
import org.bahmni.batch.Person;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.PassThroughFieldExtractor;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ColumnMapRowMapper;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;


public class BaseExportStep {
    private DataSource dataSource;

    private StepBuilderFactory stepBuilderFactory;

    private Resource sqlResource;

    private Resource outputFolder;

    private String exportName;

    public BaseExportStep(StepBuilderFactory stepBuilderFactory, DataSource dataSource, Resource sqlResource, Resource outputFolder, String exportName) {
        this.dataSource = dataSource;
        this.stepBuilderFactory = stepBuilderFactory;
        this.sqlResource = sqlResource;
        this.outputFolder = outputFolder;
        this.exportName = exportName;
    }

    private String reportSql() {
        try (InputStream is = sqlResource.getInputStream()) {
            return IOUtils.toString(is);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load " + exportName +".sql .Unable to continue");
        }
    }

    public Step getStep() {
        return stepBuilderFactory.get(exportName)
                .<Person, String>chunk(100)
                .reader(jdbcItemReader())
                .writer(flatFileItemWriter())
                .build();
    }

    private JdbcCursorItemReader jdbcItemReader() {
        JdbcCursorItemReader reader = new JdbcCursorItemReader();
        reader.setDataSource(dataSource);
        reader.setSql(reportSql());
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }

    private FlatFileItemWriter<String> flatFileItemWriter() {
        FlatFileItemWriter<String> writer = new FlatFileItemWriter<String>();
        writer.setResource(outputFolder);
        DelimitedLineAggregator delimitedLineAggregator = new DelimitedLineAggregator();
        delimitedLineAggregator.setDelimiter(",");
        delimitedLineAggregator.setFieldExtractor(new PassThroughFieldExtractor());
        writer.setLineAggregator(delimitedLineAggregator);
        return writer;
    }
}
