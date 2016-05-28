package org.bahmni.batch.exports;

import org.bahmni.batch.exception.BatchResourceException;
import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.helper.FreeMarkerEvaluator;
import org.bahmni.batch.observation.ObsFieldExtractor;
import org.bahmni.batch.observation.ObservationProcessor;
import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Obs;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

@Component
@Scope(value = "prototype")
public class ObservationExportStep {

    public static final String FILE_NAME_EXTENSION = ".csv";

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Value("${outputFolder}")
    public Resource outputFolder;

    @Autowired
    private FreeMarkerEvaluator<BahmniForm> freeMarkerEvaluator;

    private BahmniForm form;

    @Autowired
    private ObjectFactory<ObservationProcessor> observationProcessorFactory;

    public void setOutputFolder(Resource outputFolder) {
        this.outputFolder = outputFolder;
    }

    public Step getStep() {
        return stepBuilderFactory.get(getStepName())
                .<Map<String, Object>, List<Obs>>chunk(100)
                .reader(obsReader())
                .processor(observationProcessor())
                .writer(obsWriter())
                .build();
    }

    private JdbcCursorItemReader<Map<String, Object>> obsReader() {
        String sql = freeMarkerEvaluator.evaluate("obsWithParentSql.ftl",form);
        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql(sql);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }

    private ObservationProcessor observationProcessor() {
        ObservationProcessor observationProcessor = observationProcessorFactory.getObject();
        observationProcessor.setForm(form);
        return observationProcessor;
    }

    private FlatFileItemWriter<List<Obs>> obsWriter() {

        FlatFileItemWriter<List<Obs>> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(getOutputFile()));

        DelimitedLineAggregator delimitedLineAggregator = new DelimitedLineAggregator();
        delimitedLineAggregator.setDelimiter(",");
        delimitedLineAggregator.setFieldExtractor(new ObsFieldExtractor(form));

        writer.setLineAggregator(delimitedLineAggregator);
        writer.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write(getHeader());
            }
        });

        return writer;
    }

    private File getOutputFile(){
        File outputFile;

        try {
            outputFile = new File(outputFolder.getFile(),form.getDisplayName() + FILE_NAME_EXTENSION);
        }
        catch (IOException e) {
            throw new BatchResourceException("Unable to create a file in the outputFolder ["+ outputFolder.getFilename()+"]",e);
        }

        return outputFile;
    }

    private String getHeader() {
        StringBuilder sb = new StringBuilder();

        sb.append("ID_" + form.getDisplayName()).append(",");
        if (form.getParent() != null) {
            sb.append("ID_" + form.getParent().getDisplayName()).append(",");
        }

        sb.append("TreatmentId");
        for (Concept field : form.getFields()) {
            sb.append(",");
            sb.append(field.getFormattedTitle());
        }
        return sb.toString();
    }


    public void setForm(BahmniForm form) {
        this.form = form;
    }

    public String getStepName() {
        String formName = form.getFormName().getName();
        return formName.substring(0,Math.min(formName.length(), 100));
    }
}
