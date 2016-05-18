package org.bahmni.batch.exports;

import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.observation.FormSqlGenerator;
import org.bahmni.batch.observation.ObsFieldExtractor;
import org.bahmni.batch.observation.ObservationProcessor;
import org.bahmni.batch.observation.domain.Concept;
import org.bahmni.batch.observation.domain.Obs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

@Component
@Scope(value="prototype")
public class ObservationExportStep {

    private static final Logger log = LoggerFactory.getLogger(ObservationExportStep.class);

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    private Resource outputFolder;
    
    private BahmniForm form;

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
        String sql = constructSql();
        log.debug("Sql..");
        log.debug(sql);

        JdbcCursorItemReader<Map<String,Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql(sql);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }

    protected String constructSql() {
        String sql =  "SELECT %s FROM obs obs0 %s";
        int depth = form.getDepthToParent();
        StringBuilder join = new StringBuilder();
        String selectClause = " obs0.obs_id";
        for(int i = 1; i <= depth; i++){
            String parentTableAlias =  "obs"+i;
            String childTableAlias = "obs" +(i-1);
            String joinSql = " JOIN obs %s on ( %s.obs_id = %s.obs_group_id )";

           join = join.append(String.format(joinSql, new String []{parentTableAlias,parentTableAlias,childTableAlias}));
            if( i == depth)
                selectClause = selectClause + "," + parentTableAlias+".obs_id as parent_obs_id";
        }
        return String.format(sql,new String[]{selectClause,join.toString()});
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
        String formName = form.getDisplayName();
        sb.append("ID_"+formName).append(",");
        sb.append("ID_"+formName).append(",");;
        sb.append("TreatmentId");
        for(Concept field : form.getFields()) {
            sb.append(",");
            sb.append(field.getTitle());
        }
        return sb.toString();
    }


    public void setForm(BahmniForm form) {
        this.form = form;
    }

}
