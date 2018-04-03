package org.bahmni.mart.config.job;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
public class JobDefinitionTest {

    @Mock
    private EavAttributes eavAttributes;

    private JobDefinition jobDefinition;

    @Before
    public void setUp() {
        jobDefinition = new JobDefinition();
    }

    @Test
    public void shouldGiveTrueIfDefinitionIsEmpty() {
        assertTrue(new JobDefinition().isEmpty());
    }

    @Test
    public void shouldGiveFalseIfDefinitionIsNotEmpty() {
        jobDefinition.setName("name");
        jobDefinition.setReaderSql("reader sql");
        jobDefinition.setTableName("tableName");
        jobDefinition.setType("type");
        jobDefinition.setChunkSizeToRead(10);
        jobDefinition.setEavAttributes(eavAttributes);
        jobDefinition.setSeparateTables(Collections.singletonList("concept"));
        jobDefinition.setConceptReferenceSource("conceptReferenceSource");
        jobDefinition.setColumnsToIgnore(Collections.singletonList("ignore"));

        assertFalse(jobDefinition.isEmpty());
    }

    @Test
    public void shouldGiveFalseIfNameIsPresent() {
        jobDefinition.setName("name");

        assertFalse(jobDefinition.isEmpty());
    }

    @Test
    public void shouldGiveFalseIfTypeIsPresent() {
        jobDefinition.setType("type");

        assertFalse(jobDefinition.isEmpty());
    }

    @Test
    public void shouldGiveFalseIfReaderSqlIsPresent() {
        jobDefinition.setReaderSql("reader sql");

        assertFalse(jobDefinition.isEmpty());
    }

    @Test
    public void shouldGiveFalseIfChunkSizeToReadIsNot0() {
        jobDefinition.setChunkSizeToRead(10);

        assertFalse(jobDefinition.isEmpty());
    }

    @Test
    public void shouldGiveTrueEvenIfChunkSizeIs0() {
        jobDefinition.setChunkSizeToRead(0);

        assertTrue(jobDefinition.isEmpty());
    }

    @Test
    public void shouldGiveFalseIfSeparateTablesConfigIsPresent() {
        jobDefinition.setSeparateTables(Collections.singletonList("concept"));

        assertFalse(jobDefinition.isEmpty());
    }

    @Test
    public void shouldGiveFalseIfConceptReferenceSourceIsPresent() {
        jobDefinition.setConceptReferenceSource("conceptReferenceSource");

        assertFalse(jobDefinition.isEmpty());
    }

    @Test
    public void shouldGiveFalseIfColumnsToIgnoreConfigIsPresent() {
        jobDefinition.setColumnsToIgnore(Collections.singletonList("ignore"));

        assertFalse(jobDefinition.isEmpty());
    }

    @Test
    public void shouldGiveFalseIfEavAttributesIsPresent() {
        jobDefinition.setEavAttributes(eavAttributes);

        assertFalse(jobDefinition.isEmpty());
    }


}