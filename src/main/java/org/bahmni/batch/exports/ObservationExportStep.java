package org.bahmni.batch.exports;

import org.bahmni.batch.observation.ObsFieldExtractor;
import org.bahmni.batch.observation.ObservationProcessor;
import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Form;
import org.bahmni.batch.observation.domain.Obs;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

@Component
@Scope(value="prototype")
public class ObservationExportStep {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    private Resource outputFolder;

    private Form form;

    @Autowired
    private  NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectFactory<ObservationProcessor> observationProcessorFactory;

    public void setOutputFolder(Resource outputFolder) {
        this.outputFolder = outputFolder;
    }

    public Step getStep() {
        return stepBuilderFactory.get(form.getFormName().getName())
                .<Map<String,Object>,List<Obs>>chunk(100)
                .reader(obsReader())
                .processor(observationProcessor())
                .writer(obsWriter())
                .build();
    }

    private JdbcCursorItemReader<Map<String,Object>> obsReader(){
        JdbcCursorItemReader<Map<String,Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql("select obs_id as obsId,obs_group_id as obsGroupId from obs where concept_id="+ form.getFormName().getId());
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }

    private ObservationProcessor observationProcessor(){
        ObservationProcessor observationProcessor = observationProcessorFactory.getObject();
        observationProcessor.setForm(form);
        return observationProcessor;
    }

    private FlatFileItemWriter<List<Obs>> obsWriter() {
        FlatFileItemWriter<List<Obs>> writer = new FlatFileItemWriter<>();
        writer.setResource(outputFolder);

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

    private String getHeader() {
        StringBuilder sb = new StringBuilder();
        String formName = form.getFormName().getName();
        sb.append("ID_"+formName).append(",");
        sb.append("ID_"+formName).append(",");;
        sb.append("TreatmentId");
        for(Concept field : form.getFields()) {
            sb.append(",");
            sb.append(field.getTitle());
        }
        return sb.toString();
    }

    public void setForm(Form form) {
        this.form = form;
    }

}
