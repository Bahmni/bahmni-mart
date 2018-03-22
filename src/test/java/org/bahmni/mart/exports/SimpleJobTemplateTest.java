package org.bahmni.mart.exports;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.table.listener.TableGeneratorJobListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Job;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
public class SimpleJobTemplateTest {

    @Mock
    private TableGeneratorJobListener listener;

    @Mock
    private Job job;

    @Test
    public void shouldBuildJobDependsOnJobConfiguration() throws Exception {
        SimpleJobTemplate simpleJobTemplate = new SimpleJobTemplate();
        setValuesForMemberFields(simpleJobTemplate, "tableGeneratorJobListener", listener);

        SimpleJobTemplate spy = spy(simpleJobTemplate);

        JobDefinition jobDefinition = new JobDefinition();
        String testJobName = "testJob";
        jobDefinition.setName(testJobName);
        jobDefinition.setChunkSizeToRead(100);
        String readerSql = "select * from table";
        jobDefinition.setReaderSql(readerSql);

        doReturn(job).when((JobTemplate) spy).buildJob(jobDefinition, listener, readerSql);

        spy.buildJob(jobDefinition);
        verify(spy, times(1)).buildJob(jobDefinition, listener, readerSql);
    }
}