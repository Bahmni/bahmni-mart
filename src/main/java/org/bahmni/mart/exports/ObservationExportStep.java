package org.bahmni.mart.exports;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.exports.writer.DatabaseObsWriter;
import org.bahmni.mart.form.ObservationProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.helper.incrementalupdate.ObsIncrementalUpdater;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.bahmni.mart.BatchUtils.stepNumber;

@Component
@Scope(value = "prototype")
public class ObservationExportStep {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private FreeMarkerEvaluator<BahmniForm> freeMarkerEvaluator;

    private BahmniForm form;

    @Autowired
    private ObjectFactory<ObservationProcessor> observationProcessorFactory;

    @Autowired
    private ObjectFactory<DatabaseObsWriter> databaseObsWriterObjectFactory;

    @Autowired
    private ObsIncrementalUpdater obsIncrementalUpdater;

    private JobDefinition jobDefinition;

    public Step getStep() {
        return stepBuilderFactory.get(getStepName())
                .<Map<String, Object>, List<Obs>>chunk(100)
                .reader(obsReader())
                .processor(observationProcessor())
                .writer(getWriter())
                .build();
    }

    private JdbcCursorItemReader<Map<String, Object>> obsReader() {
        String sql = freeMarkerEvaluator.evaluate("obsWithParentSql.ftl", form);
        if (!obsIncrementalUpdater.isMetaDataChanged(form.getFormName().getName())) {
            sql = obsIncrementalUpdater.updateReaderSql(sql, jobDefinition.getName(), "encounter_id");
        }
        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql(sql);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }

    private ObservationProcessor observationProcessor() {
        ObservationProcessor observationProcessor = observationProcessorFactory.getObject();
        observationProcessor.setJobDefinition(jobDefinition);
        observationProcessor.setForm(form);
        return observationProcessor;
    }

    private DatabaseObsWriter getWriter() {
        DatabaseObsWriter writer = databaseObsWriterObjectFactory.getObject();
        writer.setForm(this.form);
        writer.setAddMoreMultiSelectEnabled(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition));
        return writer;
    }

    public void setForm(BahmniForm form) {
        this.form = form;
    }

    public String getStepName() {
        stepNumber++;
        String formName = String.format("Step-%d %s", stepNumber, form.getFormName().getName());
        return formName.substring(0, Math.min(formName.length(), 100));
    }

    public void setJobDefinition(JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
    }
}
