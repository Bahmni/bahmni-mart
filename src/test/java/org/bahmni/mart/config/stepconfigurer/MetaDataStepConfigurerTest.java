package org.bahmni.mart.config.stepconfigurer;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exports.MetaDataExportStep;
import org.bahmni.mart.table.TableGeneratorStep;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.Arrays;

import static org.bahmni.mart.BatchUtils.constructSqlWithParameter;
import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest(BatchUtils.class)
@RunWith(PowerMockRunner.class)
public class MetaDataStepConfigurerTest {

    @Mock
    private TableGeneratorStep tableGeneratorStep;

    @Spy
    private TableData tableData = new TableData();

    @Mock
    private MetaDataExportStep metaDataExportStep;

    @Mock
    private JdbcTemplate openmrsJDBCTemplate;

    @Mock
    private ObjectFactory<MetaDataExportStep> metaDataExportStepObjectFactory;

    @Mock
    private Resource metaDataSqlResource;


    private MetaDataStepConfigurer metaDataStepConfigurer;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        metaDataStepConfigurer = new MetaDataStepConfigurer();
        setValuesForMemberFields(metaDataStepConfigurer, "tableGeneratorStep", tableGeneratorStep);
        setValuesForMemberFields(metaDataStepConfigurer, "tableData", tableData);
        setValuesForMemberFields(metaDataStepConfigurer, "metaDataExportStepObjectFactory",
                metaDataExportStepObjectFactory);
        setValuesForMemberFields(metaDataStepConfigurer, "openmrsJDBCTemplate", openmrsJDBCTemplate);
        setValuesForMemberFields(metaDataStepConfigurer, "metaDataSqlResource",
                new ByteArrayResource("Some sql".getBytes()));
        setValuesForMemberFields(metaDataStepConfigurer, "metaDataSqlResource", metaDataSqlResource);
    }

    @Test
    public void shouldCallCreateTables() {

        metaDataStepConfigurer.createTables();

        verify(tableGeneratorStep, times(1)).createTables(Arrays.asList(tableData));
    }

    @Test
    public void shouldGenerateTableDataForGivenJobDefinition() {
        mockStatic(BatchUtils.class);
        String sql = "some sql";
        JobDefinition jobDefinition = mock(JobDefinition.class);
        String conceptReferenceSource = "source";
        when(jobDefinition.getConceptReferenceSource()).thenReturn(conceptReferenceSource);
        when(convertResourceOutputToString(metaDataSqlResource)).thenReturn(sql);
        when(constructSqlWithParameter(sql, "conceptReferenceSource", conceptReferenceSource)).thenReturn(sql);
        when(openmrsJDBCTemplate.query(eq(sql + "LIMIT 1"), any(ResultSetExtractor.class))).thenReturn(tableData);

        metaDataStepConfigurer.generateTableData(jobDefinition);

        verify(jobDefinition).getConceptReferenceSource();
        verifyStatic();
        convertResourceOutputToString(metaDataSqlResource);
        verifyStatic();
        constructSqlWithParameter(sql, "conceptReferenceSource", conceptReferenceSource);
        verify(openmrsJDBCTemplate).query(eq(sql + "LIMIT 1"), any(ResultSetExtractor.class));
        assertEquals("meta_data_dictionary", tableData.getName());
    }

    @Test
    public void shouldAddStepToCompleteDataExport() throws Exception {
        when(metaDataExportStepObjectFactory.getObject()).thenReturn(metaDataExportStep);
        Step step = mock(Step.class);
        when(metaDataExportStep.getStep()).thenReturn(step);
        FlowBuilder completeDataExport = mock(FlowBuilder.class);
        tableData.addColumn(new TableColumn("column", "text", true, null));
        JobDefinition jobDefinition = mock(JobDefinition.class);
        tableData.setName("meta_data_dictionary");

        metaDataStepConfigurer.registerSteps(completeDataExport, jobDefinition);

        assertEquals("meta_data_dictionary", tableData.getName());
        verify(metaDataExportStepObjectFactory, times(1)).getObject();
        verify(metaDataExportStep, times(1)).setTableData(tableData);
        verify(metaDataExportStep, times(1)).getStep();
        verify(metaDataExportStep, times(1)).setJobDefinition(jobDefinition);
        verify(completeDataExport, times(1)).next(step);
    }
}