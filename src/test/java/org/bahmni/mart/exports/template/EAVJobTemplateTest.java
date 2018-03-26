package org.bahmni.mart.exports.template;

import org.bahmni.mart.config.job.EavAttributes;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.domain.TableData;
import org.bahmni.mart.table.listener.EAVJobListener;
import org.bahmni.mart.table.model.EAV;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
        jobDefinition.setEavAttributes(eavAttributes);

        EAVJobTemplate spyEAVJobTemplate = spy(eavJobTemplate);

        doReturn(job).when((JobTemplate) spyEAVJobTemplate).buildJob(jobDefinition, listener, readerSql);
        when(listener.getTableDataForMart(testJobName)).thenReturn(tableData);
        when(freeMarkerEvaluator.evaluate(eq("attribute.ftl"), any(EAV.class))).thenReturn(readerSql);
        when(eavAttributes.getAttributeTypeTableName()).thenReturn("person_attribute_type");

        spyEAVJobTemplate.buildJob(jobDefinition);
        verify(spyEAVJobTemplate, times(1)).buildJob(jobDefinition, listener, readerSql);
        verify(listener, times(1)).getTableDataForMart(testJobName);
        verify(freeMarkerEvaluator, times(1)).evaluate(eq("attribute.ftl"), any(EAV.class));
        verify(eavAttributes, times(1)).getAttributeTypeTableName();
    }
}