package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.model.CodeConfig;
import org.bahmni.mart.config.job.model.EavAttributes;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionValidator;
import org.bahmni.mart.exports.updatestrategy.EavIncrementalUpdateStrategy;
import org.bahmni.mart.exports.updatestrategy.IncrementalStrategyContext;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.CodesProcessor;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.EAVJobListener;
import org.bahmni.mart.table.model.EAV;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Job;

import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.CommonTestHelper.setValuesForSuperClassMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest(JobDefinitionValidator.class)
@RunWith(PowerMockRunner.class)
public class EAVJobTemplateTest {
    @Mock
    private EAVJobListener listener;

    @Mock
    private Job job;

    @Mock
    private FreeMarkerEvaluator<EAV> freeMarkerEvaluator;

    @Mock
    private EavAttributes eavAttributes;

    @Mock
    private TableData tableData;

    @Mock
    private CodesProcessor codesProcessor;

    @Mock
    private IncrementalStrategyContext incrementalStrategyContext;

    @Mock
    private EavIncrementalUpdateStrategy eavIncrementalUpdateStrategy;

    @Mock
    private JobDefinition jobDefinition;

    private EAVJobTemplate spyEAVJobTemplate;
    private String jobName;
    private String readerSql;
    private List<CodeConfig> codeConfigs;
    private static final String JOB_TYPE = "jobType";

    @Before
    public void setUp() throws Exception {
        EAVJobTemplate eavJobTemplate = new EAVJobTemplate();
        setValuesForSuperClassMemberFields(eavJobTemplate, "incrementalStrategyContext", incrementalStrategyContext);
        setValuesForMemberFields(eavJobTemplate, "codesProcessor", codesProcessor);
        setValuesForMemberFields(eavJobTemplate, "eavJobListener", listener);
        setValuesForMemberFields(eavJobTemplate, "freeMarkerEvaluator", freeMarkerEvaluator);

        spyEAVJobTemplate = spy(eavJobTemplate);

        jobName = "testJob";
        readerSql = "select * from table";
        CodeConfig codeConfig = mock(CodeConfig.class);
        codeConfigs = Arrays.asList(codeConfig);
        when(jobDefinition.getCodeConfigs()).thenReturn(codeConfigs);
        when(jobDefinition.getEavAttributes()).thenReturn(eavAttributes);
        when(jobDefinition.getName()).thenReturn(jobName);
        when(jobDefinition.getType()).thenReturn(JOB_TYPE);
        when(incrementalStrategyContext.getStrategy(JOB_TYPE)).thenReturn(eavIncrementalUpdateStrategy);
        mockStatic(JobDefinitionValidator.class);

        doReturn(this.job).when((JobTemplate) spyEAVJobTemplate).buildJob(jobDefinition, listener, readerSql);
        when(listener.getTableDataForMart(jobName)).thenReturn(tableData);
        when(freeMarkerEvaluator.evaluate(eq("attribute.ftl"), any(EAV.class))).thenReturn(readerSql);
        when(eavAttributes.getAttributeTypeTableName()).thenReturn("person_attribute_type");
    }

    @Test
    public void shouldBuildJobDependsOnJobConfiguration() throws Exception {
        spyEAVJobTemplate.buildJob(jobDefinition);

        verify(spyEAVJobTemplate, times(1)).buildJob(jobDefinition, listener, readerSql);
        verify(listener, times(1)).getTableDataForMart(jobName);
        verify(freeMarkerEvaluator, times(1)).evaluate(eq("attribute.ftl"), any(EAV.class));
        verify(eavAttributes, times(2)).getAttributeTypeTableName();
        verify(incrementalStrategyContext).getStrategy(JOB_TYPE);
        verify(eavIncrementalUpdateStrategy).setListener(listener);
        verify(jobDefinition).getType();
    }

    @Test
    public void shouldSetPreProcessorGivenValidCodeConfigs() throws Exception {
        when(JobDefinitionValidator.isValid(codeConfigs)).thenReturn(true);

        spyEAVJobTemplate.buildJob(jobDefinition);

        verifyStatic(times(1));
        JobDefinitionValidator.isValid(codeConfigs);
        verify(codesProcessor, times(1)).setCodeConfigs(codeConfigs);
        verify(listener, times(1)).setCodesProcessor(codesProcessor);
        verify(spyEAVJobTemplate, times(1)).setPreProcessor(codesProcessor);
        verify(incrementalStrategyContext).getStrategy(JOB_TYPE);
        verify(eavIncrementalUpdateStrategy).setListener(listener);
        verify(jobDefinition).getType();
    }

    @Test
    public void shouldNotSetPreProcessorGivenInvalidCodeConfigs() throws Exception {
        when(JobDefinitionValidator.isValid(any())).thenReturn(false);

        spyEAVJobTemplate.buildJob(jobDefinition);

        verifyStatic(times(1));
        JobDefinitionValidator.isValid(codeConfigs);
        verify(spyEAVJobTemplate, never()).setPreProcessor(codesProcessor);
        verify(listener, never()).setCodesProcessor(codesProcessor);
        verify(codesProcessor, never()).setUpCodesData();
        verify(incrementalStrategyContext).getStrategy(JOB_TYPE);
        verify(eavIncrementalUpdateStrategy).setListener(listener);
        verify(jobDefinition).getType();
    }
}