package org.bahmni.mart.table;

import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CodesExtractorTest {

    private CodesExtractor codesExtractor;

    @Before
    public void setUp() throws Exception {
        codesExtractor = new CodesExtractor();
    }

    @Test
    public void shouldExtractDataFromResultSet() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnName(1)).thenReturn("id");
        when(resultSetMetaData.getColumnName(2)).thenReturn("name");
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getString("id")).thenReturn("1");
        when(resultSet.getString("name")).thenReturn("some name");

        List<Map<String, String>> actualData = codesExtractor.extractData(resultSet);

        assertNotNull(actualData);
        verify(resultSet, times(1)).getMetaData();
        verify(resultSet, times(2)).next();
        verify(resultSet, times(2)).getString(any());
        verify(resultSetMetaData, times(1)).getColumnCount();
        verify(resultSetMetaData, times(1)).getColumnName(1);
        verify(resultSetMetaData, times(1)).getColumnName(2);
        assertEquals(1, actualData.size());
        assertEquals("1", actualData.get(0).get("id"));
        assertEquals("some name", actualData.get(0).get("name"));
    }
}