package org.bahmni.mart.exports;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.config.job.JobDefinitionUtil;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.EAVJobListener;
import org.bahmni.mart.table.model.EAV;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Job;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JobDefinitionUtil.class)
public class EAVJobTemplateTest {
    @Mock
    private EAVJobListener listener;

    @Mock
    private Job job;

    @Mock
    private FreeMarkerEvaluator<EAV> freeMarkerEvaluator;

    @Mock
    private TableData tableData;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(JobDefinitionUtil.class);
    }

    @Test
    public void shouldBuildJobDependsOnJobConfiguration() throws Exception {
        EAVJobTemplate eavJobTemplate = new EAVJobTemplate();
        setValuesForMemberFields(eavJobTemplate, "eavJobListener", listener);
        setValuesForMemberFields(eavJobTemplate, "freeMarkerEvaluator", freeMarkerEvaluator);

        String testJobName = "testJob";
        String readerSql = "select * from table";
        JobDefinition jobDefinition = new JobDefinition();
        jobDefinition.setName(testJobName);
        jobDefinition.setChunkSizeToRead(100);

        EAVJobTemplate spyEAVJobTemplate = spy(eavJobTemplate);

        doReturn(job).when((JobTemplate) spyEAVJobTemplate).buildJob(jobDefinition, listener, readerSql);
        when(listener.getTableDataForMart(testJobName)).thenReturn(tableData);
        when(freeMarkerEvaluator.evaluate(eq("attribute.ftl"), any(EAV.class))).thenReturn(readerSql);
        when(JobDefinitionUtil.getReaderSQLByIgnoringColumns(null, readerSql)).thenReturn(readerSql);

        spyEAVJobTemplate.buildJob(jobDefinition);
        verify(spyEAVJobTemplate, times(1)).buildJob(jobDefinition, listener, readerSql);
        verify(listener, times(1)).getTableDataForMart(testJobName);
        verify(freeMarkerEvaluator, times(1)).evaluate(eq("attribute.ftl"), any(EAV.class));
        verifyStatic(times(1));
        JobDefinitionUtil.getReaderSQLByIgnoringColumns(null, readerSql);
    }
}