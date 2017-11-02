package org.bahmni.batch;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;
import org.bahmni.batch.exception.BatchResourceException;
import org.bahmni.batch.exports.DrugOrderBaseExportStep;
import org.bahmni.batch.exports.MetaDataCodeDictionaryExportStep;
import org.bahmni.batch.exports.ObservationExportStep;
import org.bahmni.batch.exports.TreatmentRegistrationBaseExportStep;
import org.bahmni.batch.form.FormListProcessor;
import org.bahmni.batch.form.domain.BahmniForm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobFlowBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@PrepareForTest({BatchConfiguration.class, FileUtils.class})
@RunWith(PowerMockRunner.class)
public class BatchConfigurationTest {

    private BatchConfiguration batchConfiguration;

    @Mock
    private ClassPathResource bahmniConfigFolder;

    @Mock
    private Resource zipFolder;

    @Mock
    private Resource freemarkerTemplateLocation;

    @Mock
    private ReportGenerator reportGenerator;

    @Rule
    ExpectedException expectedException = ExpectedException.none();

    @Mock
    private FormListProcessor formListProcessor;

    @Mock
    private JobBuilderFactory jobBuilderFactory;

    @Mock
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    @Mock
    private DrugOrderBaseExportStep drugOrderBaseExportStep;

    @Mock
    private MetaDataCodeDictionaryExportStep metaDataCodeDictionaryExportStep;

    @Mock
    private ObjectFactory<ObservationExportStep> observationExportStepFactory;



    @Before
    public void setUp() throws Exception {
        mockStatic(FileUtils.class);
        batchConfiguration = new BatchConfiguration();
        setValuesForMemberFields(batchConfiguration, "bahmniConfigFolder", bahmniConfigFolder);
        setValuesForMemberFields(batchConfiguration, "reportGenerator", reportGenerator);
        setValuesForMemberFields(batchConfiguration, "zipFolder", zipFolder);
        setValuesForMemberFields(batchConfiguration, "freemarkerTemplateLocation", freemarkerTemplateLocation);
        setValuesForMemberFields(batchConfiguration, "formListProcessor", formListProcessor);
        setValuesForMemberFields(batchConfiguration, "jobBuilderFactory", jobBuilderFactory);
        setValuesForMemberFields(batchConfiguration, "treatmentRegistrationBaseExportStep", treatmentRegistrationBaseExportStep);
        setValuesForMemberFields(batchConfiguration, "drugOrderBaseExportStep", drugOrderBaseExportStep);
        setValuesForMemberFields(batchConfiguration, "metaDataCodeDictionaryExportStep", metaDataCodeDictionaryExportStep);
        setValuesForMemberFields(batchConfiguration, "observationExportStepFactory", observationExportStepFactory);
    }

    @Test
    public void shouldGenerateReportBeforeExitingBatchJob() throws Exception {
        File configFolder = new File("");
        when(bahmniConfigFolder.getFile()).thenReturn(configFolder);
        File reportsFile = Mockito.mock(File.class);
        whenNew(File.class).withArguments(reportsFile, "report.html").thenReturn(reportsFile);
        String generatedReport = "Generated Report";
        when(reportGenerator.generateReport()).thenReturn(generatedReport);

        batchConfiguration.generateReport();

        verify(bahmniConfigFolder, times(1)).getFile();
        verify(reportGenerator, times(1)).generateReport();
    }

    @Test
    public void shouldThrowAnExceptionWhileGeneratingGenerateReport() throws Exception {
        when(bahmniConfigFolder.getFile()).thenThrow(new IOException());
        String zipFileName = "amman-exports-DDMMYYYY.zip";
        when(zipFolder.getFilename()).thenReturn(zipFileName);
        expectedException.expect(BatchResourceException.class);
        expectedException.expectMessage("Unable to write the report file ["+ zipFileName +"]");

        batchConfiguration.generateReport();

        verify(bahmniConfigFolder, times(1)).getFile();
        verify(zipFolder, times(1)).getFilename();
    }

    @Test
    public void shouldAddFreeMarkerConfiguration() throws Exception {
        freemarker.template.Configuration configuration = PowerMockito.mock(freemarker.template.Configuration.class);
        whenNew(freemarker.template.Configuration.class).withArguments(any()).thenReturn(configuration);
        File configurationFile = Mockito.mock(File.class);
        when(freemarkerTemplateLocation.getFile()).thenReturn(configurationFile);

        Configuration freeMarkerConfiguration = batchConfiguration.freeMarkerConfiguration();

        Assert.assertEquals(configuration, freeMarkerConfiguration);
        verify(configuration, times(1)).setDirectoryForTemplateLoading(configurationFile);
        verify(configuration, times(1)).setDefaultEncoding("UTF-8");
        verify(configuration, times(1)).setTemplateExceptionHandler(any(TemplateExceptionHandler.class));
    }

    @Test
    public void shouldCompleteDataExportWithObsForms() throws Exception {
        ArrayList<BahmniForm> bahmniForms = new ArrayList<>();
        BahmniForm medicalHistoryForm = new BahmniForm();
        BahmniForm fstg = new BahmniForm();
        bahmniForms.add(medicalHistoryForm);
        bahmniForms.add(fstg);

        when(formListProcessor.retrieveAllForms()).thenReturn(bahmniForms);
        JobBuilder jobBuilder = Mockito.mock(JobBuilder.class);
        when(jobBuilderFactory.get("ammanExports")).thenReturn(jobBuilder);
        when(jobBuilder.incrementer(any(RunIdIncrementer.class))).thenReturn(jobBuilder);
        when(jobBuilder.preventRestart()).thenReturn(jobBuilder);
        when(jobBuilder.listener(any(JobCompletionNotificationListener.class))).thenReturn(jobBuilder);
        Step treatmentStep = Mockito.mock(Step.class);
        when(treatmentRegistrationBaseExportStep.getStep()).thenReturn(treatmentStep);
        JobFlowBuilder jobFlowBuilder = Mockito.mock(JobFlowBuilder.class);
        when(jobBuilder.flow(treatmentStep)).thenReturn(jobFlowBuilder);
        Step drugOrderStep = Mockito.mock(Step.class);
        when(drugOrderBaseExportStep.getStep()).thenReturn(drugOrderStep);
        FlowBuilder<FlowJobBuilder> completeDataExport = (FlowBuilder<FlowJobBuilder>) mock(FlowBuilder.class);
        when(jobFlowBuilder.next(drugOrderStep)).thenReturn(completeDataExport);
        Step metaDataStep = Mockito.mock(Step.class);
        when(metaDataCodeDictionaryExportStep.getStep()).thenReturn(metaDataStep);
        when(completeDataExport.next(metaDataStep)).thenReturn(completeDataExport);
        FlowJobBuilder flowJobBuilder = Mockito.mock(FlowJobBuilder.class);
        when(completeDataExport.end()).thenReturn(flowJobBuilder);
        Job expectedJob = Mockito.mock(Job.class);
        when(flowJobBuilder.build()).thenReturn(expectedJob);

        ObservationExportStep medicalHistoryObservationExportStep = Mockito.mock(ObservationExportStep.class);
        ObservationExportStep fstgObservationExportStep = Mockito.mock(ObservationExportStep.class);
        when(observationExportStepFactory.getObject()).thenReturn(medicalHistoryObservationExportStep).thenReturn(fstgObservationExportStep);
        Step medicalHistoryObservationStep = Mockito.mock(Step.class);
        Step fstgObservationStep = Mockito.mock(Step.class);
        when(medicalHistoryObservationExportStep.getStep()).thenReturn(medicalHistoryObservationStep);
        when(fstgObservationExportStep.getStep()).thenReturn(fstgObservationStep);

        Job actualJob = batchConfiguration.completeDataExport();

        Assert.assertEquals(expectedJob, actualJob);
        verify(formListProcessor, times(1)).retrieveAllForms();
        verify(jobBuilderFactory, times(1)).get("ammanExports");
        verify(treatmentRegistrationBaseExportStep, times(1)).getStep();
        verify(drugOrderBaseExportStep, times(1)).getStep();
        verify(metaDataCodeDictionaryExportStep, times(1)).getStep();
        verify(observationExportStepFactory, times(2)).getObject();
        verify(medicalHistoryObservationExportStep, times(1)).setForm(medicalHistoryForm);
        verify(fstgObservationExportStep, times(1)).setForm(fstg);
        verify(completeDataExport, times(1)).next(medicalHistoryObservationStep);
        verify(completeDataExport, times(1)).next(fstgObservationStep);
    }

    private void setValuesForMemberFields(Object batchConfiguration, String fieldName, Object valueForMemberField) throws NoSuchFieldException, IllegalAccessException {
        Field f1 = batchConfiguration.getClass().getDeclaredField(fieldName);
        f1.setAccessible(true);
        f1.set(batchConfiguration, valueForMemberField);
    }
}