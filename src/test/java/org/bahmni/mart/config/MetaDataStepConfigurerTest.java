package org.bahmni.mart.config;

import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.exports.MetaDataExportStep;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.Arrays;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetaDataStepConfigurerTest {

    @Mock
    private TableGeneratorStep tableGeneratorStep;

    @Spy
    private TableData tableData;

    @Mock
    private MetaDataExportStep metaDataExportStep;

    @Mock
    private JdbcTemplate openmrsJDBCTemplate;

    @Mock
    private ObjectFactory<MetaDataExportStep> metaDataExportStepObjectFactory;

    @Mock
    private JobDefinitionReader jobDefinitionReader;


    private MetaDataStepConfigurer metaDataStepConfigurer;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        metaDataStepConfigurer = new MetaDataStepConfigurer();
        setValuesForMemberFields(metaDataStepConfigurer, "tableGeneratorStep", tableGeneratorStep);
        setValuesForMemberFields(metaDataStepConfigurer, "tableData", tableData);
        setValuesForMemberFields(metaDataStepConfigurer, "metaDataExportStepObjectFactory",
                metaDataExportStepObjectFactory);
        setValuesForMemberFields(metaDataStepConfigurer, "openmrsJDBCTemplate", openmrsJDBCTemplate);
        setValuesForMemberFields(metaDataStepConfigurer, "jobDefinitionReader", jobDefinitionReader);
        setValuesForMemberFields(metaDataStepConfigurer, "metaDataSqlResource",
                new ByteArrayResource("Some sql".getBytes()));
    }

    @Test
    public void shouldCallCreateTables() {

        metaDataStepConfigurer.createTables();

        verify(tableGeneratorStep, times(1)).createTables(Arrays.asList(tableData));
    }

    @Test
    public void shouldAddStepToCompleteDataExport() throws Exception {
        when(metaDataExportStepObjectFactory.getObject()).thenReturn(metaDataExportStep);
        Step step = mock(Step.class);
        when(metaDataExportStep.getStep()).thenReturn(step);
        FlowBuilder completeDataExport = mock(FlowBuilder.class);
        tableData.addColumn(new TableColumn("column", "text", true, null));
        when(openmrsJDBCTemplate.query(anyString(), any(ResultSetExtractor.class))).thenReturn(tableData);

        metaDataStepConfigurer.registerSteps(completeDataExport);

        assertEquals("meta_data_dictionary", tableData.getName());
        verify(jobDefinitionReader,times(1)).getConceptReferenceSource();
        verify(metaDataExportStepObjectFactory,times(1)).getObject();
        verify(metaDataExportStep, times(1)).setTableData(tableData);
        verify(metaDataExportStep, times(1)).getStep();
        verify(completeDataExport, times(1)).next(step);
    }
}