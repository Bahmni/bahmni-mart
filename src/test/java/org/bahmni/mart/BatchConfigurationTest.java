package org.bahmni.mart;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;
import org.bahmni.mart.exports.ObservationExportStep;
import org.bahmni.mart.exports.TreatmentRegistrationBaseExportStep;
import org.bahmni.mart.form.FormListProcessor;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableGeneratorStep;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobFlowBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.ArrayList;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({BatchConfiguration.class, FileUtils.class})
@RunWith(PowerMockRunner.class)
public class BatchConfigurationTest {

    @Mock
    private Resource freemarkerTemplateLocation;

    @Mock
    private FormListProcessor formListProcessor;

    @Mock
    private JobBuilderFactory jobBuilderFactory;

    @Mock
    private ObjectFactory<ObservationExportStep> observationExportStepFactory;

    @Mock
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Mock
    private TableGeneratorStep tableGeneratorStep;

    @Mock
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;


    private BatchConfiguration batchConfiguration;

    @Before
    public void setUp() throws Exception {
        mockStatic(FileUtils.class);
        batchConfiguration = new BatchConfiguration();
    }

    @Test
    public void shouldAddFreeMarkerConfiguration() throws Exception {
        Configuration configuration = PowerMockito.mock(Configuration.class);
        whenNew(Configuration.class).withArguments(any()).thenReturn(configuration);
        File configurationFile = Mockito.mock(File.class);
        when(freemarkerTemplateLocation.getFile()).thenReturn(configurationFile);

        Configuration freeMarkerConfiguration = batchConfiguration.freeMarkerConfiguration();

        assertEquals(configuration, freeMarkerConfiguration);
        verify(configuration, times(1)).setDefaultEncoding("UTF-8");
        verify(configuration, times(1)).setClassForTemplateLoading(any(), eq("/templates"));
        verify(configuration, times(1)).setTemplateExceptionHandler(any(TemplateExceptionHandler.class));
    }

    @Test
    public void shouldCompleteDataExportWithObsForms() throws Exception {
        setValuesForMemberFields(batchConfiguration, "formListProcessor", formListProcessor);
        setValuesForMemberFields(batchConfiguration, "jobBuilderFactory", jobBuilderFactory);
        setValuesForMemberFields(batchConfiguration, "treatmentRegistrationBaseExportStep",
                treatmentRegistrationBaseExportStep);
        setValuesForMemberFields(batchConfiguration, "formListProcessor", formListProcessor);
        setValuesForMemberFields(batchConfiguration, "formTableMetadataGenerator", formTableMetadataGenerator);
        setValuesForMemberFields(batchConfiguration, "observationExportStepFactory", observationExportStepFactory);
        setValuesForMemberFields(batchConfiguration, "tableGeneratorStep", tableGeneratorStep);

        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        BahmniForm medicalHistoryForm = new BahmniForm();
        BahmniForm fstg = new BahmniForm();
        bahmniForms.add(medicalHistoryForm);
        bahmniForms.add(fstg);

        when(formListProcessor.retrieveAllForms()).thenReturn(bahmniForms);
        JobBuilder jobBuilder = Mockito.mock(JobBuilder.class);
        when(jobBuilderFactory.get(BatchConfiguration.FULL_DATA_EXPORT_JOB_NAME)).thenReturn(jobBuilder);
        when(jobBuilder.incrementer(any(RunIdIncrementer.class))).thenReturn(jobBuilder);
        when(jobBuilder.preventRestart()).thenReturn(jobBuilder);
        Step treatmentStep = Mockito.mock(Step.class);
        when(treatmentRegistrationBaseExportStep.getStep()).thenReturn(treatmentStep);

        JobFlowBuilder jobFlowBuilder = Mockito.mock(JobFlowBuilder.class);
        when(jobBuilder.flow(treatmentStep)).thenReturn(jobFlowBuilder);
        FlowJobBuilder flowJobBuilder = Mockito.mock(FlowJobBuilder.class);
        when(jobFlowBuilder.end()).thenReturn(flowJobBuilder);
        Job expectedJob = Mockito.mock(Job.class);
        when(flowJobBuilder.build()).thenReturn(expectedJob);

        ObservationExportStep medicalHistoryObservationExportStep = Mockito.mock(ObservationExportStep.class);
        ObservationExportStep fstgObservationExportStep = Mockito.mock(ObservationExportStep.class);
        when(observationExportStepFactory.getObject()).thenReturn(medicalHistoryObservationExportStep)
                .thenReturn(fstgObservationExportStep);
        Step medicalHistoryObservationStep = Mockito.mock(Step.class);
        Step fstgObservationStep = Mockito.mock(Step.class);
        when(medicalHistoryObservationExportStep.getStep()).thenReturn(medicalHistoryObservationStep);
        when(fstgObservationExportStep.getStep()).thenReturn(fstgObservationStep);

        Job actualJob = batchConfiguration.completeDataExport();

        assertEquals(expectedJob, actualJob);
        verify(formListProcessor, times(1)).retrieveAllForms();
        verify(jobBuilderFactory, times(1)).get(BatchConfiguration.FULL_DATA_EXPORT_JOB_NAME);
        verify(observationExportStepFactory, times(2)).getObject();
        verify(medicalHistoryObservationExportStep, times(1)).setForm(medicalHistoryForm);
        verify(fstgObservationExportStep, times(1)).setForm(fstg);
        verify(jobFlowBuilder, times(1)).next(medicalHistoryObservationStep);
        verify(jobFlowBuilder, times(1)).next(fstgObservationStep);
        verify(formTableMetadataGenerator, times(1)).getTableDataList();
        verify(tableGeneratorStep, atLeastOnce()).createTables(formTableMetadataGenerator.getTableDataList());
        verify(formTableMetadataGenerator, times(2)).addMetadataForForm(any(BahmniForm.class));
    }

    @Test
    public void shouldCompleteDataExportWithoutObsForms() throws Exception {
        setValuesForMemberFields(batchConfiguration, "formListProcessor", formListProcessor);
        setValuesForMemberFields(batchConfiguration, "jobBuilderFactory", jobBuilderFactory);
        setValuesForMemberFields(batchConfiguration, "treatmentRegistrationBaseExportStep",
                treatmentRegistrationBaseExportStep);
        setValuesForMemberFields(batchConfiguration, "formListProcessor", formListProcessor);
        setValuesForMemberFields(batchConfiguration, "formTableMetadataGenerator", formTableMetadataGenerator);
        setValuesForMemberFields(batchConfiguration, "observationExportStepFactory", observationExportStepFactory);
        setValuesForMemberFields(batchConfiguration, "tableGeneratorStep", tableGeneratorStep);

        when(formListProcessor.retrieveAllForms()).thenReturn(new ArrayList<>());
        JobBuilder jobBuilder = Mockito.mock(JobBuilder.class);
        when(jobBuilderFactory.get(BatchConfiguration.FULL_DATA_EXPORT_JOB_NAME)).thenReturn(jobBuilder);
        when(jobBuilder.incrementer(any(RunIdIncrementer.class))).thenReturn(jobBuilder);
        when(jobBuilder.preventRestart()).thenReturn(jobBuilder);
        Step treatmentStep = Mockito.mock(Step.class);
        when(treatmentRegistrationBaseExportStep.getStep()).thenReturn(treatmentStep);

        JobFlowBuilder jobFlowBuilder = Mockito.mock(JobFlowBuilder.class);
        when(jobBuilder.flow(treatmentStep)).thenReturn(jobFlowBuilder);
        FlowJobBuilder flowJobBuilder = Mockito.mock(FlowJobBuilder.class);
        when(jobFlowBuilder.end()).thenReturn(flowJobBuilder);
        Job expectedJob = Mockito.mock(Job.class);
        when(flowJobBuilder.build()).thenReturn(expectedJob);

        Job actualJob = batchConfiguration.completeDataExport();

        assertEquals(expectedJob, actualJob);
        verify(formListProcessor, times(1)).retrieveAllForms();
        verify(jobBuilderFactory, times(1)).get(BatchConfiguration.FULL_DATA_EXPORT_JOB_NAME);
        verify(observationExportStepFactory, times(0)).getObject();
        verify(formTableMetadataGenerator, times(1)).getTableDataList();
        verify(tableGeneratorStep, atLeastOnce()).createTables(formTableMetadataGenerator.getTableDataList());
        verify(formTableMetadataGenerator, times(0)).addMetadataForForm(any(BahmniForm.class));
    }
}