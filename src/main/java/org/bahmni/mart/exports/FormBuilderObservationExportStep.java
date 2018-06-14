package org.bahmni.mart.exports;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.form.FormBuilderObservationProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
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

import static java.util.Objects.isNull;
import static org.bahmni.mart.BatchUtils.stepNumber;

@Component
@Scope(value = "prototype")
public class FormBuilderObservationExportStep {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    private BahmniForm form;

    @Autowired
    private ObjectFactory<FormBuilderObservationProcessor> observationProcessorFactory;

    @Autowired
    private ObjectFactory<DatabaseObsWriter> databaseObsWriterObjectFactory;

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
        String sql = String.format("SELECT encounter_id, obs_id FROM obs WHERE form_namespace_and_path LIKE " +
                "'Bahmni^%s%%' AND obs_group_id is NULL AND voided = false GROUP BY encounter_id",
                getRootForm(form).getFormName().getName());

        JdbcCursorItemReader<Map<String, Object>> reader = new JdbcCursorItemReader<>();
        reader.setDataSource(dataSource);
        reader.setSql(sql);
        reader.setRowMapper(new ColumnMapRowMapper());
        return reader;
    }

    private BahmniForm getRootForm(BahmniForm form) {
        return isNull(form.getRootForm())? form : form.getRootForm();
    }

    private FormBuilderObservationProcessor observationProcessor() {
        FormBuilderObservationProcessor observationProcessor = observationProcessorFactory.getObject();
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
