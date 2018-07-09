package org.bahmni.mart.exports;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.table.TableRecordWriter;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.io.ByteArrayResource;

import javax.sql.DataSource;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class MetaDataExportStepTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private StepBuilderFactory stepBuilderFactory;

    @Mock
    private ObjectFactory<TableRecordWriter> recordWriterObjectFactory;

    @Mock
    private JobDefinition jobDefinition;

    private MetaDataExportStep metaDataExportStep;

    @Before
    public void setUp() throws Exception {
        metaDataExportStep = new MetaDataExportStep();
        PowerMockito.mockStatic(BatchUtils.class);
        setValuesForMemberFields(metaDataExportStep, "dataSource", dataSource);
        setValuesForMemberFields(metaDataExportStep, "stepBuilderFactory", stepBuilderFactory);
        setValuesForMemberFields(metaDataExportStep, "recordWriterObjectFactory", recordWriterObjectFactory);
        setValuesForMemberFields(metaDataExportStep, "jobDefinition", jobDefinition);
        setValuesForMemberFields(metaDataExportStep, "metaDataSqlResource",
                new ByteArrayResource("some sql".getBytes()));
    }

    @Test
    public void shouldGetTheBatchStepForMetaData() throws Exception {
        TableData table = mock(TableData.class);
        StepBuilder stepBuilder = mock(StepBuilder.class);
        SimpleStepBuilder simpleStepBuilder = mock(SimpleStepBuilder.class);
        TaskletStep expectedBaseExportStep = mock(TaskletStep.class);
        metaDataExportStep.setTableData(table);
        when(stepBuilderFactory.get("Meta Data Export Step")).thenReturn(stepBuilder);
        when(stepBuilder.chunk(100)).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.reader(any())).thenReturn(simpleStepBuilder);
        when(recordWriterObjectFactory.getObject()).thenReturn(new TableRecordWriter());
        when(simpleStepBuilder.processor(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.writer(any())).thenReturn(simpleStepBuilder);
        when(simpleStepBuilder.build()).thenReturn(expectedBaseExportStep);

        Step metaDataExportStepStep = metaDataExportStep.getStep();

        Assert.assertNotNull(metaDataExportStepStep);
        Assert.assertEquals(expectedBaseExportStep, metaDataExportStepStep);
        verify(jobDefinition,times(1)).getConceptReferenceSource();
        verify(recordWriterObjectFactory, times(1)).getObject();
    }
}