package org.bahmni.mart.exports;

import org.bahmni.mart.BatchUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;


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

    private BaseExportStep baseExportStep;
    private String headers = "patient_id, obs_id";
    private String exportName = "exportName";


    @Before
    public void setUp() throws Exception {
        mockStatic(BatchUtils.class);
        baseExportStep = new BaseExportStep(stepBuilderFactory, dataSource,
                sqlResource, outputFolder, exportName, headers);
    }

    @Test
    public void shouldConvertSqlResourceToEquivalentSqlString() throws Exception {
        String sql = "";
        when(BatchUtils.convertResourceOutputToString(sqlResource)).thenReturn(sql);

        baseExportStep.postConstruct();

        verifyStatic(times(1));
        BatchUtils.convertResourceOutputToString(sqlResource);
    }

    @Test
    public void shouldReturnTheHeadersForExport() throws Exception {
        assertEquals(headers, baseExportStep.getHeaders());
    }

    @Test
    public void shouldGetTheBatchStepForBaseExport() throws Exception {
        StepBuilder stepBuilder = Mockito.mock(StepBuilder.class);
        when(stepBuilderFactory.get(exportName)).thenReturn(stepBuilder);
        SimpleStepBuilder simpleStepBuilder = Mockito.mock(SimpleStepBuilder.class);
        when(stepBuilder.chunk(50)).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);
        TaskletStep expectedBaseExportStep = Mockito.mock(TaskletStep.class);
        when(simpleStepBuilder.build()).thenReturn(expectedBaseExportStep);

        Step baseExportStepStep = baseExportStep.getStep();

        assertNotNull(baseExportStepStep);
        assertEquals(expectedBaseExportStep, baseExportStepStep);
    }
}