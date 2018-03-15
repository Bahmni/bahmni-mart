package org.bahmni.mart;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;
import org.bahmni.mart.config.FormStepConfigurer;
import org.bahmni.mart.config.ProgramDataStepConfigurer;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.bahmni.mart.exports.SimpleJobTemplate;
import org.bahmni.mart.exports.TreatmentRegistrationBaseExportStep;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobFlowBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PrepareForTest({BatchConfiguration.class, FileUtils.class})
@RunWith(PowerMockRunner.class)
public class BatchConfigurationTest {

    @Rule
    ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Resource freemarkerTemplateLocation;

    @Mock
    private JobBuilderFactory jobBuilderFactory;

    @Mock
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    @Mock
    private FormStepConfigurer formStepConfigurer;

    @Mock
    private ProgramDataStepConfigurer programDataStepConfigurer;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private SimpleJobTemplate simpleJobTemplate;

    @Mock
    private JobLauncher jobLauncher;

    private BatchConfiguration batchConfiguration;

    @Before
    public void setUp() throws Exception {
        mockStatic(FileUtils.class);
        batchConfiguration = new BatchConfiguration();
        setValuesForMemberFields(batchConfiguration, "formStepConfigurer", formStepConfigurer);
        setValuesForMemberFields(batchConfiguration, "programDataStepConfigurer", programDataStepConfigurer);
        setValuesForMemberFields(batchConfiguration, "jobDefinitionReader", jobDefinitionReader);
        setValuesForMemberFields(batchConfiguration, "simpleJobTemplate", simpleJobTemplate);
        setValuesForMemberFields(batchConfiguration, "jobLauncher", jobLauncher);
    }

    @Test
    public void shouldAddFreeMarkerConfiguration() throws Exception {
        Configuration configuration = PowerMockito.mock(Configuration.class);
        whenNew(Configuration.class).withArguments(any()).thenReturn(configuration);
        File configurationFile = mock(File.class);
        when(freemarkerTemplateLocation.getFile()).thenReturn(configurationFile);

        Configuration freeMarkerConfiguration = batchConfiguration.freeMarkerConfiguration();

        assertEquals(configuration, freeMarkerConfiguration);
        verify(configuration, times(1)).setDefaultEncoding("UTF-8");
        verify(configuration, times(1)).setClassForTemplateLoading(any(), eq("/templates"));
        verify(configuration, times(1)).setTemplateExceptionHandler(any(TemplateExceptionHandler.class));
    }

    @Test
    public void shouldCompleteDataExport() throws Exception {
        setValuesForMemberFields(batchConfiguration, "jobBuilderFactory", jobBuilderFactory);
        setValuesForMemberFields(batchConfiguration, "treatmentRegistrationBaseExportStep",
                treatmentRegistrationBaseExportStep);

        JobBuilder jobBuilder = mock(JobBuilder.class);
        when(jobBuilderFactory.get(BatchConfiguration.FULL_DATA_EXPORT_JOB_NAME)).thenReturn(jobBuilder);
        when(jobBuilder.incrementer(any(RunIdIncrementer.class))).thenReturn(jobBuilder);
        when(jobBuilder.preventRestart()).thenReturn(jobBuilder);
        Step treatmentStep = mock(Step.class);
        when(treatmentRegistrationBaseExportStep.getStep()).thenReturn(treatmentStep);

        JobFlowBuilder jobFlowBuilder = mock(JobFlowBuilder.class);
        when(jobBuilder.flow(treatmentStep)).thenReturn(jobFlowBuilder);
        FlowJobBuilder flowJobBuilder = mock(FlowJobBuilder.class);
        when(jobFlowBuilder.end()).thenReturn(flowJobBuilder);
        Job expectedJob = mock(Job.class);
        when(flowJobBuilder.build()).thenReturn(expectedJob);

        Job actualJob = batchConfiguration.completeDataExport();

        assertEquals(expectedJob, actualJob);
        verify(jobBuilderFactory, times(1)).get(BatchConfiguration.FULL_DATA_EXPORT_JOB_NAME);
        verify(formStepConfigurer, times(1)).createTables();
        verify(formStepConfigurer, times(1)).registerSteps(jobFlowBuilder);
    }

    @Test
    public void shouldNotRunJobsForValidJobConfiguration() throws Exception {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        JobDefinition jobDefinition1 = mock(JobDefinition.class);

        when(jobDefinition.getName()).thenReturn("job");
        when(jobDefinition1.getName()).thenReturn("job1");

        when(jobDefinition.getTableName()).thenReturn("table");
        when(jobDefinition1.getTableName()).thenReturn("table1");

        List<JobDefinition> jobDefinitions = Arrays.asList(jobDefinition, jobDefinition1);
        Job job = mock(Job.class);
        Job job1 = mock(Job.class);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(jobDefinitions);
        when(simpleJobTemplate.buildJob(jobDefinition)).thenReturn(job);
        when(simpleJobTemplate.buildJob(jobDefinition1)).thenReturn(job1);

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(simpleJobTemplate, times(1)).buildJob(jobDefinition);
        verify(simpleJobTemplate, times(1)).buildJob(jobDefinition1);
        verify(jobLauncher, times(2)).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    public void shouldNotRunJobsForInvalidJobConfiguration() throws Exception {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        JobDefinition jobDefinition1 = mock(JobDefinition.class);

        when(jobDefinition.getName()).thenReturn("job1");
        when(jobDefinition1.getName()).thenReturn("job1");

        when(jobDefinition.getTableName()).thenReturn("table");
        when(jobDefinition1.getTableName()).thenReturn("table1");

        List<JobDefinition> jobDefinitions = Arrays.asList(jobDefinition, jobDefinition1);
        Job job = mock(Job.class);
        Job job1 = mock(Job.class);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(jobDefinitions);
        when(simpleJobTemplate.buildJob(jobDefinition)).thenReturn(job);
        when(simpleJobTemplate.buildJob(jobDefinition1)).thenReturn(job1);

        expectedException.expect(InvalidJobConfiguration.class);
        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(simpleJobTemplate, times(0)).buildJob(jobDefinition);
        verify(simpleJobTemplate, times(0)).buildJob(jobDefinition1);
        verify(jobLauncher, times(0)).run(any(Job.class), any(JobParameters.class));

    }
}