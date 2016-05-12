package org.bahmni.batch.exports;

import org.bahmni.batch.BatchUtils;
import org.bahmni.batch.observation.ObservationProcessor;
import org.bahmni.batch.observation.domain.Form;
import org.bahmni.batch.observation.domain.Obs;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.PassThroughFieldExtractor;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
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
                .<Integer,List<Obs>>chunk(100)
                .reader(obsReader())
                .processor(observationProcessor())
//                .writer(obsWriter())
                .build();
    }

    private JdbcCursorItemReader obsReader(){
        //TODO: Need obs_id and obs_group_id as part of the query
        JdbcCursorItemReader reader = new JdbcCursorItemReader();
        reader.setDataSource(dataSource);
        reader.setSql("select obs_id from obs where concept_id="+ form.getFormName().getId());
        reader.setRowMapper(new SingleColumnRowMapper<Integer>());
        return reader;
    }

    private ObservationProcessor observationProcessor(){
        ObservationProcessor observationProcessor = observationProcessorFactory.getObject();
        observationProcessor.setForm(form);
        return observationProcessor;
    }

    private FlatFileItemWriter<Map<String,Object>> obsWriter() {
        FlatFileItemWriter<Map<String,Object>> writer = new FlatFileItemWriter<>();
        writer.setResource(outputFolder);
        DelimitedLineAggregator delimitedLineAggregator = new DelimitedLineAggregator();
        delimitedLineAggregator.setDelimiter(",");
        delimitedLineAggregator.setFieldExtractor(new PassThroughFieldExtractor());
        writer.setLineAggregator(delimitedLineAggregator);
        //TODO: HeaderCallback needs to be provided
        return writer;
    }

    public void setForm(Form form) {
        this.form = form;
    }

}
