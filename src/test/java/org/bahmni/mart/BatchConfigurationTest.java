package org.bahmni.mart;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;
import org.bahmni.mart.config.FormStepConfigurer;
import org.bahmni.mart.config.MartJSONReader;
import org.bahmni.mart.config.MetaDataStepConfigurer;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.view.ViewExecutor;
import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.bahmni.mart.exports.TreatmentRegistrationBaseExportStep;
import org.bahmni.mart.exports.template.EAVJobTemplate;
import org.bahmni.mart.exports.template.SimpleJobTemplate;
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
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobFlowBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.ArrayList;
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
    private ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Resource freemarkerTemplateLocation;

    @Mock
    private JobBuilderFactory jobBuilderFactory;

    @Mock
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    @Mock
    private FormStepConfigurer formStepConfigurer;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private MartJSONReader martJSONReader;

    @Mock
    private ViewExecutor viewExecutor;

    @Mock
    private SimpleJobTemplate simpleJobTemplate;

    @Mock
    private EAVJobTemplate eavJobTemplate;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job expectedJob;

    @Mock
    private MetaDataStepConfigurer metaDataStepConfigurer;

    private JobFlowBuilder jobFlowBuilder;

    private BatchConfiguration batchConfiguration;

    @Before
    public void setUp() throws Exception {
        mockStatic(FileUtils.class);
        batchConfiguration = new BatchConfiguration();
        setValuesForMemberFields(batchConfiguration, "formStepConfigurer", formStepConfigurer);
        setValuesForMemberFields(batchConfiguration, "jobDefinitionReader", jobDefinitionReader);
        setValuesForMemberFields(batchConfiguration, "simpleJobTemplate", simpleJobTemplate);
        setValuesForMemberFields(batchConfiguration, "jobLauncher", jobLauncher);
        setValuesForMemberFields(batchConfiguration, "jobBuilderFactory", jobBuilderFactory);
        setValuesForMemberFields(batchConfiguration, "metaDataStepConfigurer", metaDataStepConfigurer);
        setValuesForMemberFields(batchConfiguration, "treatmentRegistrationBaseExportStep",
                treatmentRegistrationBaseExportStep);
        setValuesForMemberFields(batchConfiguration, "eavJobTemplate", eavJobTemplate);
        setValuesForMemberFields(batchConfiguration, "martJSONReader", martJSONReader);
        setValuesForMemberFields(batchConfiguration, "viewExecutor", viewExecutor);

        JobBuilder jobBuilder = mock(JobBuilder.class);
        when(jobBuilderFactory.get(BatchConfiguration.OBS_DATA_FLATTENING_JOB_NAME)).thenReturn(jobBuilder);
        when(jobBuilder.incrementer(any(RunIdIncrementer.class))).thenReturn(jobBuilder);
        when(jobBuilder.preventRestart()).thenReturn(jobBuilder);
        Step treatmentStep = mock(Step.class);
        when(treatmentRegistrationBaseExportStep.getStep()).thenReturn(treatmentStep);

        jobFlowBuilder = mock(JobFlowBuilder.class);
        when(jobBuilder.flow(treatmentStep)).thenReturn(jobFlowBuilder);
        FlowJobBuilder flowJobBuilder = mock(FlowJobBuilder.class);
        when(jobFlowBuilder.end()).thenReturn(flowJobBuilder);
        when(flowJobBuilder.build()).thenReturn(expectedJob);
        when(jobDefinitionReader.getConceptReferenceSource()).thenReturn("");
        when(martJSONReader.getViewDefinitions()).thenReturn(new ArrayList<>());
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
    public void shouldRunObsJob() throws Exception {
        JobDefinition jobDefinition = mock(JobDefinition.class);

        when(jobDefinition.getReaderSql()).thenReturn("Some sql");
        when(jobDefinition.getTableName()).thenReturn("Some table");
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Arrays.asList(jobDefinition));
        when(jobDefinition.getType()).thenReturn("obs");

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(jobBuilderFactory, times(1)).get(BatchConfiguration.OBS_DATA_FLATTENING_JOB_NAME);
        verify(formStepConfigurer, times(1)).createTables();
        verify(formStepConfigurer, times(1)).registerSteps(jobFlowBuilder);
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));

    }

    @Test
    public void shouldRunJobsForValidJobConfiguration() throws Exception {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        JobDefinition jobDefinition1 = mock(JobDefinition.class);

        when(jobDefinition.getName()).thenReturn("job");
        when(jobDefinition1.getName()).thenReturn("job1");

        when(jobDefinition.getType()).thenReturn("generic");
        when(jobDefinition1.getType()).thenReturn("generic");

        when(jobDefinition.getTableName()).thenReturn("table");
        when(jobDefinition1.getTableName()).thenReturn("table1");

        when(jobDefinition.getReaderSql()).thenReturn("Some sql");
        when(jobDefinition1.getReaderSql()).thenReturn("Some sql");


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
        verify(viewExecutor, times(1)).execute(any());
    }

    @Test
    public void shouldNotRunJobsForInvalidJobConfiguration() throws Exception {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        JobDefinition jobDefinition1 = mock(JobDefinition.class);

        when(jobDefinition.getName()).thenReturn("job1");
        when(jobDefinition1.getName()).thenReturn("job1");

        when(jobDefinition.getTableName()).thenReturn("table");
        when(jobDefinition1.getTableName()).thenReturn("table1");

        when(jobDefinition.getType()).thenReturn("generic");
        when(jobDefinition1.getType()).thenReturn("generic");

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

    @Test
    public void shouldAddMetaDataStepGivenConceptReferenceSource() throws Exception {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Arrays.asList(jobDefinition));
        when(jobDefinition.getType()).thenReturn("obs");
        when(jobDefinitionReader.getConceptReferenceSource()).thenReturn("Bahmni-Internal");

        batchConfiguration.run();

        verify(jobBuilderFactory, times(1)).get(BatchConfiguration.OBS_DATA_FLATTENING_JOB_NAME);
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
        verify(metaDataStepConfigurer, times(1)).createTables();
        verify(metaDataStepConfigurer, times(1)).registerSteps(any(FlowBuilder.class));
    }

    @Test
    public void shouldNotAddMetaDataStepGivenNoConceptReferenceSource() throws Exception {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Arrays.asList(jobDefinition));
        when(jobDefinition.getType()).thenReturn("obs");

        batchConfiguration.run();

        verify(jobBuilderFactory, times(1)).get(BatchConfiguration.OBS_DATA_FLATTENING_JOB_NAME);
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
        verify(metaDataStepConfigurer, times(0)).createTables();
        verify(metaDataStepConfigurer, times(0)).registerSteps(any(FlowBuilder.class));
    }

    @Test
    public void shouldRunEavJob() throws Exception {
        JobDefinition jobDefinition = mock(JobDefinition.class);
        Job job = mock(Job.class);

        when(jobDefinition.getReaderSql()).thenReturn("Some sql");
        when(jobDefinition.getTableName()).thenReturn("Some table");
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Arrays.asList(jobDefinition));
        when(jobDefinition.getType()).thenReturn("eav");
        when(eavJobTemplate.buildJob(jobDefinition)).thenReturn(job);

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
        verify(eavJobTemplate, times(1)).buildJob(jobDefinition);
    }
}