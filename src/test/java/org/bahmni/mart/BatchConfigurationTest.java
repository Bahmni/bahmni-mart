package org.bahmni.mart;

import org.apache.commons.io.FileUtils;
import org.bahmni.mart.config.stepconfigurer.BacteriologyStepConfigurer;
import org.bahmni.mart.config.stepconfigurer.DiagnosesStepConfigurer;
import org.bahmni.mart.config.stepconfigurer.FormStepConfigurer;
import org.bahmni.mart.config.MartJSONReader;
import org.bahmni.mart.config.stepconfigurer.MetaDataStepConfigurer;
import org.bahmni.mart.config.stepconfigurer.OrderStepConfigurer;
import org.bahmni.mart.config.stepconfigurer.RspStepConfigurer;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.JobDefinitionValidator;
import org.bahmni.mart.config.procedure.ProcedureDefinition;
import org.bahmni.mart.config.procedure.ProcedureExecutor;
import org.bahmni.mart.config.stepconfigurer.DispositionStepConfigurer;
import org.bahmni.mart.config.view.RspViewDefinition;
import org.bahmni.mart.config.view.ViewDefinition;
import org.bahmni.mart.config.view.ViewExecutor;
import org.bahmni.mart.exception.InvalidJobConfiguration;
import org.bahmni.mart.exports.TreatmentRegistrationBaseExportStep;
import org.bahmni.mart.exports.template.EAVJobTemplate;
import org.bahmni.mart.exports.template.SimpleJobTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.jdbc.BadSqlGrammarException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.BatchConfiguration.REGISTRATION_SECOND_PAGE;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({FileUtils.class, JobDefinitionValidator.class})
@RunWith(PowerMockRunner.class)
public class BatchConfigurationTest {

    @Mock
    private JobBuilderFactory jobBuilderFactory;

    @Mock
    private TreatmentRegistrationBaseExportStep treatmentRegistrationBaseExportStep;

    @Mock
    private FormStepConfigurer formStepConfigurer;

    @Mock
    private BacteriologyStepConfigurer bacteriologyStepConfigurer;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private MartJSONReader martJSONReader;

    @Mock
    private ViewExecutor viewExecutor;

    @Mock
    private ObjectFactory simpleJobTemplateFactory;

    @Mock
    private ObjectFactory eavJobTemplateFactory;

    @Mock
    private SimpleJobTemplate simpleJobTemplate;

    @Mock
    private EAVJobTemplate eavJobTemplate;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job job;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private MetaDataStepConfigurer metaDataStepConfigurer;

    @Mock
    private OrderStepConfigurer orderStepConfigurer;

    @Mock
    private DiagnosesStepConfigurer diagnosesStepConfigurer;

    @Mock
    private RspStepConfigurer rspStepConfigurer;

    @Mock
    private DispositionStepConfigurer dispositionStepConfigurer;

    @Mock
    private RspViewDefinition rspViewDefinition;

    @Mock
    private ProcedureExecutor procedureExecutor;

    private JobFlowBuilder jobFlowBuilder;

    private BatchConfiguration batchConfiguration;
    
    private static final String OBS_DATA_FLATTENING_JOB_NAME = "Obs Data";

    @Before
    public void setUp() throws Exception {
        mockStatic(FileUtils.class);
        mockStatic(JobDefinitionValidator.class);
        batchConfiguration = new BatchConfiguration();
        setValuesForMemberFields(batchConfiguration, "formStepConfigurer", formStepConfigurer);
        setValuesForMemberFields(batchConfiguration, "bacteriologyStepConfigurer", bacteriologyStepConfigurer);
        setValuesForMemberFields(batchConfiguration, "jobDefinitionReader", jobDefinitionReader);
        setValuesForMemberFields(batchConfiguration, "simpleJobTemplateFactory", simpleJobTemplateFactory);
        setValuesForMemberFields(batchConfiguration, "jobLauncher", jobLauncher);
        setValuesForMemberFields(batchConfiguration, "jobBuilderFactory", jobBuilderFactory);
        setValuesForMemberFields(batchConfiguration, "metaDataStepConfigurer", metaDataStepConfigurer);
        setValuesForMemberFields(batchConfiguration, "treatmentRegistrationBaseExportStep",
                treatmentRegistrationBaseExportStep);
        setValuesForMemberFields(batchConfiguration, "eavJobTemplateFactory", eavJobTemplateFactory);
        setValuesForMemberFields(batchConfiguration, "martJSONReader", martJSONReader);
        setValuesForMemberFields(batchConfiguration, "viewExecutor", viewExecutor);
        setValuesForMemberFields(batchConfiguration, "orderStepConfigurer", orderStepConfigurer);
        setValuesForMemberFields(batchConfiguration, "diagnosesStepConfigurer", diagnosesStepConfigurer);
        setValuesForMemberFields(batchConfiguration, "rspStepConfigurer", rspStepConfigurer);
        setValuesForMemberFields(batchConfiguration, "rspViewDefinition", rspViewDefinition);
        setValuesForMemberFields(batchConfiguration, "procedureExecutor", procedureExecutor);
        setValuesForMemberFields(batchConfiguration, "dispositionStepConfigurer", dispositionStepConfigurer);

        JobBuilder jobBuilder = mock(JobBuilder.class);
        when(jobBuilderFactory.get(any())).thenReturn(jobBuilder);
        when(jobBuilder.incrementer(any(RunIdIncrementer.class))).thenReturn(jobBuilder);
        when(jobBuilder.preventRestart()).thenReturn(jobBuilder);
        Step treatmentStep = mock(Step.class);
        when(treatmentRegistrationBaseExportStep.getStep()).thenReturn(treatmentStep);

        jobFlowBuilder = mock(JobFlowBuilder.class);
        when(jobBuilder.flow(treatmentStep)).thenReturn(jobFlowBuilder);
        FlowJobBuilder flowJobBuilder = mock(FlowJobBuilder.class);
        when(jobFlowBuilder.end()).thenReturn(flowJobBuilder);
        when(flowJobBuilder.build()).thenReturn(job);
        when(martJSONReader.getViewDefinitions()).thenReturn(new ArrayList<>());
        when(JobDefinitionValidator.validate(anyListOf(JobDefinition.class))).thenReturn(true);
        when(eavJobTemplateFactory.getObject()).thenReturn(eavJobTemplate);
        when(simpleJobTemplateFactory.getObject()).thenReturn(simpleJobTemplate);
        when(jobDefinitionReader.getJobDefinitionByName(REGISTRATION_SECOND_PAGE)).thenReturn(jobDefinition);
    }

    @Test
    public void shouldRunObsJob() throws Exception {
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Collections.singletonList(jobDefinition));
        when(jobDefinition.getType()).thenReturn("obs");
        when(jobDefinition.getName()).thenReturn(OBS_DATA_FLATTENING_JOB_NAME);

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(jobBuilderFactory, times(1)).get(OBS_DATA_FLATTENING_JOB_NAME);
        verify(formStepConfigurer, times(1)).createTables();
        verify(formStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    public void shouldRunObsJobEvenIfThereIsAnyErrorWithPreviousJob() throws Exception {
        JobDefinition jobDefinition1 = mock(JobDefinition.class);
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Arrays.asList(jobDefinition1, jobDefinition));
        when(simpleJobTemplate.buildJob(jobDefinition1))
                .thenThrow(new BadSqlGrammarException("task", "wrong sql", null));

        when(jobDefinition1.getType()).thenReturn("customSql");

        when(jobDefinition.getType()).thenReturn("obs");
        when(jobDefinition.getName()).thenReturn(OBS_DATA_FLATTENING_JOB_NAME);

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(jobDefinition1, times(1)).getType();
        verify(simpleJobTemplateFactory,times(1)).getObject();
        verify(jobBuilderFactory, times(1)).get(OBS_DATA_FLATTENING_JOB_NAME);
        verify(formStepConfigurer, times(1)).createTables();
        verify(formStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
    }


    @Test
    public void shouldRunJobsForValidJobConfiguration() throws Exception {
        List<JobDefinition> jobDefinitions = Arrays.asList(jobDefinition, jobDefinition);

        when(jobDefinition.getType()).thenReturn("customSql");
        when(jobDefinition.getReaderSql()).thenReturn("Some sql");

        when(jobDefinitionReader.getJobDefinitions()).thenReturn(jobDefinitions);
        when(simpleJobTemplate.buildJob(jobDefinition)).thenReturn(job);

        batchConfiguration.run();

        verify(jobDefinition, times(2)).getType();
        verify(job, times(2)).getName();
        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(simpleJobTemplateFactory,times(2)).getObject();
        verify(simpleJobTemplate, times(2)).buildJob(jobDefinition);
        verify(jobLauncher, times(2)).run(any(Job.class), any(JobParameters.class));
        verify(viewExecutor, times(1)).execute(any());
        verifyStatic(times(1));
        JobDefinitionValidator.validate(anyListOf(JobDefinition.class));
    }

    @Test
    public void shouldNotRunJobsForInvalidJobConfiguration() throws Exception {
        List<JobDefinition> jobDefinitions = Arrays.asList(jobDefinition, jobDefinition);

        when(JobDefinitionValidator.validate(anyListOf(JobDefinition.class))).thenReturn(false);
        when(jobDefinition.getTableName()).thenReturn("table");
        when(jobDefinition.getType()).thenReturn("customSql");

        when(jobDefinitionReader.getJobDefinitions()).thenReturn(jobDefinitions);
        when(simpleJobTemplate.buildJob(jobDefinition)).thenReturn(job);

        // As Junit does not support to expect Exception and others verify in same test method, we have to use old way

        try {
            batchConfiguration.run();
        } catch (InvalidJobConfiguration e) {
            verify(jobDefinitionReader, times(1)).getJobDefinitions();
            verify(simpleJobTemplate, times(0)).buildJob(jobDefinition);
            verify(jobLauncher, times(0)).run(any(Job.class), any(JobParameters.class));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void shouldRunEavJob() throws Exception {
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Collections.singletonList(jobDefinition));
        when(jobDefinition.getType()).thenReturn("eav");
        when(eavJobTemplate.buildJob(jobDefinition)).thenReturn(job);

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
        verify(eavJobTemplateFactory,times(1)).getObject();
        verify(eavJobTemplate, times(1)).buildJob(jobDefinition);
        verify(jobDefinition, times(1)).getType();
    }

    @Test
    public void shouldRunBacteriologyJob() throws Exception {
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Collections.singletonList(jobDefinition));
        when(jobDefinition.getName()).thenReturn("Bacteriology Data");
        when(jobDefinition.getType()).thenReturn("bacteriology");

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(jobBuilderFactory, times(1)).get("Bacteriology Data");
        verify(bacteriologyStepConfigurer, times(1)).createTables();
        verify(bacteriologyStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    public void shouldRunMetaDataDictionaryJob() throws Exception {
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Collections.singletonList(jobDefinition));
        when(jobDefinition.getName()).thenReturn("MetaData Dictionary");
        when(jobDefinition.getType()).thenReturn("metadata");

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(jobBuilderFactory, times(1)).get("MetaData Dictionary");
        verify(metaDataStepConfigurer, times(1)).createTables();
        verify(metaDataStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    public void shouldRunOrdersJob() throws Exception {
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Collections.singletonList(jobDefinition));
        when(jobDefinition.getName()).thenReturn("Order Data");
        when(jobDefinition.getType()).thenReturn("orders");

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(jobBuilderFactory, times(1)).get("Order Data");
        verify(orderStepConfigurer, times(1)).createTables();
        verify(orderStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    public void shouldRunDiagnosesJob() throws Exception {
        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Collections.singletonList(jobDefinition));
        when(jobDefinition.getName()).thenReturn("Diagnoses Data");
        when(jobDefinition.getType()).thenReturn("diagnoses");

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(jobBuilderFactory, times(1)).get("Diagnoses Data");
        verify(diagnosesStepConfigurer, times(1)).createTables();
        verify(diagnosesStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    public void shouldRunRspJob() throws Exception {
        String jobName = "Registration Second Page";
        ViewDefinition viewDefinition = mock(ViewDefinition.class);
        List<ViewDefinition> viewDefinitions = new ArrayList<>();

        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Collections.singletonList(jobDefinition));
        when(jobDefinition.getName()).thenReturn(jobName);
        when(jobDefinition.getType()).thenReturn("rsp");
        when(jobDefinitionReader.getJobDefinitionByName(REGISTRATION_SECOND_PAGE)).thenReturn(jobDefinition);
        when(rspViewDefinition.getDefinition()).thenReturn(viewDefinition);
        when(martJSONReader.getViewDefinitions()).thenReturn(viewDefinitions);

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(jobBuilderFactory, times(1)).get(jobName);
        verify(rspStepConfigurer, times(1)).createTables();
        verify(rspStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
        verify(jobDefinitionReader, times(1)).getJobDefinitionByName(REGISTRATION_SECOND_PAGE);
        verify(rspViewDefinition, times(1)).getDefinition();
        verify(martJSONReader, times(1)).getViewDefinitions();
        verify(viewExecutor, times(1)).execute(Collections.singletonList(viewDefinition));
    }

    @Test
    public void shouldRunDispositionJob() throws Exception {
        String jobName = "Disposition Data";

        when(jobDefinitionReader.getJobDefinitions()).thenReturn(Collections.singletonList(jobDefinition));
        when(jobDefinition.getName()).thenReturn(jobName);
        when(jobDefinition.getType()).thenReturn("disposition");

        batchConfiguration.run();

        verify(jobDefinitionReader, times(1)).getJobDefinitions();
        verify(jobBuilderFactory, times(1)).get(jobName);
        verify(dispositionStepConfigurer, times(1)).createTables();
        verify(dispositionStepConfigurer, times(1)).registerSteps(jobFlowBuilder, jobDefinition);
        verify(jobLauncher, times(1)).run(any(Job.class), any(JobParameters.class));
    }

    @Test
    public void shouldExecuteProcedures() throws Exception {
        ProcedureDefinition procedureDefinition = mock(ProcedureDefinition.class);
        List<ProcedureDefinition> procedureDefinitions = Arrays.asList(procedureDefinition);
        when(martJSONReader.getProcedureDefinitions()).thenReturn(procedureDefinitions);

        batchConfiguration.run();

        verify(procedureExecutor,times(1)).execute(procedureDefinitions);
    }
}