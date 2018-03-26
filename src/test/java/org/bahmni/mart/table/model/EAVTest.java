package org.bahmni.mart.table.model;

import org.bahmni.mart.config.job.EAVJobData;
import org.bahmni.mart.table.domain.TableData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class EAVTest {

    @Mock
    private TableData tableData;

    @Mock
    private EAVJobData eavJobData;

    @Test
    public void shouldReturnDataTypeColumnNameForPersonAttributeTypeTable() {
        when(eavJobData.getAttributeTypeTableName()).thenReturn("person_attribute_type");
        assertEquals("format", new EAV(tableData, eavJobData).getTypeColumnName());
    }

    @Test
    public void shouldReturnDataTypeColumnNameForVisitAttributeTypeTable() {
        when(eavJobData.getAttributeTypeTableName()).thenReturn("visit_attribute_type");
        assertEquals("datatype", new EAV(tableData, eavJobData).getTypeColumnName());
    }

    @Test
    public void shouldReturnDataTypeColumnNameForProviderAttributeTypeTable() {
        when(eavJobData.getAttributeTypeTableName()).thenReturn("provider_attribute_type");
        assertEquals("datatype", new EAV(tableData, eavJobData).getTypeColumnName());
    }
}