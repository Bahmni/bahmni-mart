package org.bahmni.mart.job;

import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.exports.template.SimpleJobTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.batch.core.Job;
import org.springframework.beans.factory.ObjectFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomSqlJobStrategyTest {

    @Mock
    private ObjectFactory<SimpleJobTemplate> simpleJobTemplateObjectFactory;

    @Mock
    private SimpleJobTemplate simpleJobTemplate;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private Job expectedJob;

    @Test
    public void shouldReturnAJob() {

        when(simpleJobTemplateObjectFactory.getObject()).thenReturn(simpleJobTemplate);
        when(simpleJobTemplate.buildJob(jobDefinition)).thenReturn(expectedJob);

        CustomSqlJobStrategy customSqlJobStrategy = new CustomSqlJobStrategy(simpleJobTemplateObjectFactory);

        Job actualJob = customSqlJobStrategy.getJob(jobDefinition);

        assertEquals(expectedJob, actualJob);

        verify(simpleJobTemplateObjectFactory, times(1)).getObject();
        verify(simpleJobTemplate, times(1)).buildJob(jobDefinition);

    }
}