package org.bahmni.mart.exports;

import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.updatestrategy.ObsIncrementalUpdateStrategy;
import org.bahmni.mart.exports.writer.DatabaseObsWriter;
import org.bahmni.mart.exports.writer.RemovalWriter;
import org.bahmni.mart.form.ObservationProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.bahmni.mart.BatchUtils.stepNumber;

@Component
@Scope(value = "prototype")
public class Form1ObservationExportStep implements ObservationExportStep {

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
    private ObjectFactory<RemovalWriter> removalWriterObjectFactory;

    @Autowired
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Autowired
    @Qualifier("obsIncrementalStrategy")
    private ObsIncrementalUpdateStrategy obsIncrementalUpdater;

    private JobDefinition jobDefinition;

    @Override
    public Step getStep() {
        return stepBuilderFactory.get(getStepName("Insertion Step"))
                .<Map<String, Object>, List<Obs>>chunk(100)
                .reader(obsReader(false))
                .processor(observationProcessor())
                .writer(getWriter())
                .build();
    }

    @Override
    public Step getRemovalStep() {
        return stepBuilderFactory.get(getStepName("Removal Step"))
                .<Map<String, Object>, Map<String, Object>>chunk(100)
                .reader(obsReader(true))
                .writer(getRemovalWriter())
                .build();
    }

    @Override
    public void setJobDefinition(JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
    }

    @Override
    public void setForm(BahmniForm form) {
        this.form = form;
    }

    private JdbcCursorItemReader<Map<String, Object>> obsReader(boolean voided) {
        return getReader(freeMarkerEvaluator.evaluate("obsWithParentSql.ftl", form, voided));
    }

    private JdbcCursorItemReader<Map<String, Object>> getReader(String sql) {
        sql = obsIncrementalUpdater.updateReaderSql(sql, jobDefinition.getName(), "encounter_id",
                form.getFormName().getName());
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
        writer.setJobDefinition(jobDefinition);
        writer.setForm(this.form);
        writer.setAddMoreMultiSelectEnabled(JobDefinitionUtil.isAddMoreMultiSelectEnabled(jobDefinition));
        return writer;
    }

    private RemovalWriter getRemovalWriter() {
        RemovalWriter writer = removalWriterObjectFactory.getObject();
        writer.setJobDefinition(jobDefinition);
        writer.setTableData(formTableMetadataGenerator.getTableData(form));
        return writer;
    }

    public String getStepName(String prefix) {
        stepNumber++;
        String formName = String.format("%s-%d %s", prefix, stepNumber, form.getFormName().getName());
        return formName.substring(0, Math.min(formName.length(), 100));
    }
}
