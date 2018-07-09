package org.bahmni.mart.job;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.template.EAVJobTemplate;
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
public class EAVJobStrategyTest {

    @Mock
    private ObjectFactory<EAVJobTemplate> eavJobTemplateFactory;

    @Mock
    private EAVJobTemplate eavJobTemplate;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private Job expectedJob;

    @Test
    public void shouldGetAJob() {

        when(eavJobTemplateFactory.getObject()).thenReturn(eavJobTemplate);
        when(eavJobTemplate.buildJob(jobDefinition)).thenReturn(expectedJob);

        EAVJobStrategy eavJobStrategy = new EAVJobStrategy(eavJobTemplateFactory);

        Job actualJob = eavJobStrategy.getJob(jobDefinition);

        assertEquals(expectedJob, actualJob);

        verify(eavJobTemplateFactory, times(1)).getObject();
        verify(eavJobTemplate, times(1)).buildJob(jobDefinition);
    }
}