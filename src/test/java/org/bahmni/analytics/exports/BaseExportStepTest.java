package org.bahmni.analytics.exports;

import org.bahmni.analytics.BatchUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

import static org.mockito.Matchers.any;


@PrepareForTest(BatchUtils.class)
@RunWith(PowerMockRunner.class)
public class BaseExportStepTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private StepBuilderFactory stepBuilderFactory;

    @Mock
    private Resource sqlResource;

    @Mock
    private Resource outputFolder;

    @Mock
    SimpleStepBuilder<String, String> chunk;

    private BaseExportStep baseExportStep;
    String headers = "patient_id, obs_id";
    String exportName = "exportName";


    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(BatchUtils.class);
        baseExportStep = new BaseExportStep(stepBuilderFactory, dataSource,
                sqlResource, outputFolder, exportName, headers);
    }

    @Test
    public void shouldConvertSqlResourceToEquivalentSqlString() throws Exception {
        String sql = "";
        Mockito.when(BatchUtils.convertResourceOutputToString(sqlResource)).thenReturn(sql);

        baseExportStep.postConstruct();

        PowerMockito.verifyStatic(Mockito.times(1));
        BatchUtils.convertResourceOutputToString(sqlResource);
    }

    @Test
    public void shouldReturnTheHeadersForExport() throws Exception {
        String actualStepHeaders = baseExportStep.getHeaders();

        Assert.assertEquals(headers, actualStepHeaders);
    }

    @Test
    public void shouldGetTheBatchStepForBaseExport() throws Exception {
        StepBuilder stepBuilder = Mockito.mock(StepBuilder.class);
        Mockito.when(stepBuilderFactory.get(exportName)).thenReturn(stepBuilder);
        SimpleStepBuilder simpleStepBuilder = Mockito.mock(SimpleStepBuilder.class);
        Mockito.when(stepBuilder.chunk(50)).thenReturn(simpleStepBuilder);
        Mockito.when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        Mockito.when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);
        TaskletStep expectedBaseExportStep = Mockito.mock(TaskletStep.class);
        Mockito.when(simpleStepBuilder.build()).thenReturn(expectedBaseExportStep);

        Step baseExportStepStep = baseExportStep.getStep();

        Assert.assertNotNull(baseExportStepStep);
        Assert.assertEquals(expectedBaseExportStep, baseExportStepStep);
    }
}