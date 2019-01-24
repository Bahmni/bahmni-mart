package org.bahmni.mart.job;

import org.bahmni.mart.config.job.model.JobDefinition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.batch.core.Job;

import java.util.Map;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobContextTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ObsJobStrategy obsJobStrategy;

    @Mock
    private Map<String, JobStrategy> jobStrategies;

    @Mock
    private JobDefinition jobDefinition;

    @Mock
    private Job job;

    private JobContext jobContext;

    private String jobType = "obs";

    @Before
    public void setUp() throws Exception {

        jobContext = new JobContext();

        setValuesForMemberFields(jobContext, "jobStrategies", jobStrategies);

        when(jobDefinition.getType()).thenReturn(jobType);
        when(obsJobStrategy.getJob(jobDefinition)).thenReturn(job);
    }

    @Test
    public void shouldGetJobFromCorrespondingJobTypeStrategy() {

        when(jobStrategies.get(jobType)).thenReturn(obsJobStrategy);

        Job actualJob = jobContext.getJob(jobDefinition);

        assertEquals(job, actualJob);

        verify(jobDefinition, times(1)).getType();
        verify(jobStrategies, times(1)).get(jobType);
        verify(obsJobStrategy, times(1)).getJob(jobDefinition);
    }

    @Test
    public void shouldLogWarningForInvalidJobType() throws Exception {

        when(jobStrategies.get(jobType)).thenReturn(null);
        when(jobDefinition.getName()).thenReturn("Obs Data");
        Logger log = mock(Logger.class);
        setValueForFinalStaticField(JobContext.class, "log", log);

        jobContext.getJob(jobDefinition);
        verify(log, times(1)).warn("'obs' type is invalid for the job 'Obs Data'");

        verify(jobDefinition, times(1)).getType();
        verify(obsJobStrategy, times(0)).getJob(jobDefinition);

    }
}
