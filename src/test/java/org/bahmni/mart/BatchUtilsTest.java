package org.bahmni.mart;

import org.apache.commons.io.IOUtils;
import org.bahmni.mart.exception.BatchResourceException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@PrepareForTest(IOUtils.class)
@RunWith(PowerMockRunner.class)
public class BatchUtilsTest {

    @Rule
    ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(IOUtils.class);
    }

    @Test
    public void ensureThatTheCommaSeparatedConceptNamesAreConvertedToSet() {
        List<String> conceptNames = BatchUtils.convertConceptNamesToSet("\"a,b\",\"c\",\"d\"");

        assertEquals(3, conceptNames.size());
        assertTrue(conceptNames.contains("c"));
        assertTrue(conceptNames.contains("d"));
        assertTrue(conceptNames.contains("a,b"));
    }


    @Test
    public void ensureThatSetIsNotNullWhenConceptNamesIsEmpty() {
        List<String> conceptNames = BatchUtils.convertConceptNamesToSet("");
        assertNotNull(conceptNames);
        assertEquals(0, conceptNames.size());
    }

    @Test
    public void ensureThatSetIsNotNullWhenConceptNamesIsNull() {
        List<String> conceptNames = BatchUtils.convertConceptNamesToSet(null);
        assertNotNull(conceptNames);
        assertEquals(0, conceptNames.size());
    }

    @Test
    public void shouldConvertResourceOutputToString() throws Exception {
        ClassPathResource classPathResource = Mockito.mock(ClassPathResource.class);
        InputStream inputStream = Mockito.mock(InputStream.class);
        Mockito.when(classPathResource.getInputStream()).thenReturn(inputStream);
        String expectedString = "stringEquivalentOfClassPathResource";
        Mockito.when(IOUtils.toString(inputStream)).thenReturn(expectedString);

        String actualString = BatchUtils.convertResourceOutputToString(classPathResource);

        assertEquals(expectedString, actualString);
        Mockito.verify(classPathResource, Mockito.times(1)).getInputStream();
    }

    @Test
    public void shouldThrowBatchResourceException() throws Exception {
        expectedException.expect(BatchResourceException.class);
        expectedException.expectMessage("Cannot load the provided resource. Unable to continue");

        ClassPathResource classPathResource = Mockito.mock(ClassPathResource.class);
        Mockito.when(classPathResource.getInputStream()).thenThrow(new IOException());

        BatchUtils.convertResourceOutputToString(classPathResource);
    }


    @Test
    public void shouldGiveNullIndependentToType() {
        assertNull(BatchUtils.getPostgresCompatibleValue(null, "text"));
        assertNull(BatchUtils.getPostgresCompatibleValue(null, "date"));
        assertNull(BatchUtils.getPostgresCompatibleValue(null, "timestamp"));
        assertNull(BatchUtils.getPostgresCompatibleValue(null, "time"));
    }

    @Test
    public void shouldGivePostgresCompatibleValueForText() {
        assertEquals("'abc''d'", BatchUtils.getPostgresCompatibleValue("abc'd", "text"));
    }

    @Test
    public void shouldGivePSQLCompatibleValueForTimeStamp() {
        assertEquals("'2012-12-01T00:00:00z'",
                BatchUtils.getPostgresCompatibleValue("2012-12-01T00:00:00z", "timestamp"));
    }

    @Test
    public void shouldGivePSQLCompatibleValueForDate() {
        assertEquals("'2012-12-01'", BatchUtils.getPostgresCompatibleValue("2012-12-01", "date"));
    }

    @Test
    public void shouldGivePSQLCompatibleValueForTime() {
        assertEquals("'08:00:00'", BatchUtils.getPostgresCompatibleValue("08:00:00", "time"));
    }

    @Test
    public void shouldGiveTheSameValueForAllOtherTypes() {
        assertEquals("222", BatchUtils.getPostgresCompatibleValue("222", "numeric"));
    }

    @Test
    public void shouldReplaceParameterWithValue() throws Exception {
        String sql = "Select * from table where column = :parameter";

        sql = BatchUtils.constructSqlWithParameter(sql,"parameter","value");

        assertEquals("Select * from table where column = 'value'",sql);
    }
}
