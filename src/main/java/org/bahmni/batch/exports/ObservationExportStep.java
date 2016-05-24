package org.bahmni.batch.exports;

import org.apache.commons.io.FileUtils;
import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.observation.DynamicObsQuery;
import org.bahmni.batch.observation.ObsFieldExtractor;
import org.bahmni.batch.observation.ObservationProcessor;
import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Obs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
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

    private static final Logger log = LoggerFactory.getLogger(ObservationExportStep.class);

    public static final String FILE_NAME_EXTENSION = ".csv";

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Value("${outputFolder}")
    public Resource outputFolder;


    private DynamicObsQuery dynamicObsQuery;

    private BahmniForm form;

    @Autowired
    private ObjectFactory<ObservationProcessor> observationProcessorFactory;

    public void setOutputFolder(Resource outputFolder) {
        this.outputFolder = outputFolder;
    }

    public Step getStep() {
        return stepBuilderFactory.get(form.getFormName().getName())
                .<Map<String, Object>, List<Obs>>chunk(100)
                .reader(obsReader())
                .processor(observationProcessor())
                .writer(obsWriter())
                .build();
    }

    private JdbcCursorItemReader<Map<String, Object>> obsReader() {
        String sql = "select o.concept_id as conceptId,\n" + "       o.obs_id as id,\n"
                + "       coalesce(o.value_boolean,DATE_FORMAT(o.value_datetime, '%d/%b/%Y'),o.value_numeric,o.value_text,cv.concept_short_name,cv.concept_full_name) as value,\n"
                + "       'abcd' as treatmentNumber,\n" + "       obs_con.concept_full_name as conceptName\n" + "from\n"
                + "  obs o\n" + "  join concept_view obs_con on(o.concept_id = obs_con.concept_id)\n"
                + "  left outer join concept codedConcept on o.value_coded = codedConcept.concept_id\n"
                + "  left outer join concept_view cv on (cv.concept_id = codedConcept.concept_id)\n" + "where\n"
                + "  o.obs_group_id in (802833,802834,802837,802839,802842)\n"
                + "  and obs_con.concept_id  in (1191,1192,1196,1194,1843,1844,2077,2470,2471,1204,1205,5575)\n"
                + "  and o.voided=0";
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
            throw new RuntimeException("Unable to create a file in the outputFolder ["+ outputFolder.getFilename()+"]");
        }

        return outputFile;
    }

    private String getHeader() {
        StringBuilder sb = new StringBuilder();

        sb.append("ID_" + form.getDisplayName()).append(",");
        if (form.getParent() != null) {
            sb.append("ID_" + form.getParent().getDisplayName()).append(",");
            ;
        }

        sb.append("TreatmentId");
        for (Concept field : form.getFields()) {
            sb.append(",");
            sb.append(field.getTitle());
        }
        return sb.toString();
    }


    public void setForm(BahmniForm form) {
        this.form = form;
    }

}
