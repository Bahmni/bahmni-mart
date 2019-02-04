package org.bahmni.mart;

import org.apache.commons.io.IOUtils;
import org.bahmni.mart.exception.BatchResourceException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.BatchUtils.constructSqlWithParameter;
import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;
import static org.bahmni.mart.BatchUtils.getPostgresCompatibleValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest(IOUtils.class)
@RunWith(PowerMockRunner.class)
public class BatchUtilsTest {

    @Rule
    private ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        mockStatic(IOUtils.class);
    }

    @Test
    public void shouldConvertResourceOutputToString() throws Exception {
        ClassPathResource classPathResource = mock(ClassPathResource.class);
        InputStream inputStream = mock(InputStream.class);
        when(classPathResource.getInputStream()).thenReturn(inputStream);
        String expectedString = "stringEquivalentOfClassPathResource";
        when(IOUtils.toString(inputStream)).thenReturn(expectedString);

        assertEquals(expectedString, convertResourceOutputToString(classPathResource));
        verify(classPathResource, times(1)).getInputStream();
    }

    @Test
    public void shouldThrowBatchResourceException() throws Exception {
        ClassPathResource classPathResource = mock(ClassPathResource.class);
        when(classPathResource.getInputStream()).thenThrow(new IOException());

        expectedException.expect(BatchResourceException.class);
        expectedException.expectMessage("Cannot load the provided resource. Unable to continue");
        convertResourceOutputToString(classPathResource);
    }


    @Test
    public void shouldGiveNullIndependentToType() {
        assertNull(getPostgresCompatibleValue(null, "text"));
        assertNull(getPostgresCompatibleValue(null, "date"));
        assertNull(getPostgresCompatibleValue(null, "timestamp"));
        assertNull(getPostgresCompatibleValue(null, "time"));
        assertNull(getPostgresCompatibleValue(null, "numeric"));
    }

    @Test
    public void shouldGivePostgresCompatibleValueForText() {
        assertEquals("'abc''d'", getPostgresCompatibleValue("abc'd", "text"));
    }

    @Test
    public void shouldGivePSQLCompatibleValueForTimeStamp() {
        assertEquals("'2012-12-01T00:00:00z'",
                getPostgresCompatibleValue("2012-12-01T00:00:00z", "timestamp"));
    }

    @Test
    public void shouldGivePSQLCompatibleValueForDate() {
        assertEquals("'2012-12-01'", getPostgresCompatibleValue("2012-12-01", "date"));
    }

    @Test
    public void shouldGivePSQLCompatibleValueForTime() {
        assertEquals("'08:00:00'", getPostgresCompatibleValue("08:00:00", "time"));
    }

    @Test
    public void shouldGiveTheSameValueForAllOtherTypes() {
        assertEquals("222", getPostgresCompatibleValue("222", "numeric"));
    }

    @Test
    public void shouldReplaceParameterWithValue() throws Exception {
        String sql = "Select * from table where column = :parameter";

        assertEquals("Select * from table where column = 'value'",
                constructSqlWithParameter(sql, "parameter", "value"));
    }

    @Test
    public void shouldReplaceParameterWithBooleanValue() {
        String sql = "SELECT * FROM table WHERE column IS :parameter";

        assertEquals("SELECT * FROM table WHERE column IS true",
                constructSqlWithParameter(sql, "parameter", true));
    }

    @Test
    public void shouldReplaceParameterWithListOfStringValues() {
        String sql = "SELECT * FROM table WHERE column IN (:parameter)";

        List<String> values = Arrays.asList("value1", "value2");
        assertEquals("SELECT * FROM table WHERE column IN ('value1', 'value2')",
                constructSqlWithParameter(sql, "parameter", values));
    }
}
