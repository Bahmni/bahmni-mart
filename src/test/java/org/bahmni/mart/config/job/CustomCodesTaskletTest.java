package org.bahmni.mart.config.job;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.postgresql.copy.CopyManager;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Scanner;

import static org.bahmni.mart.CommonTestHelper.setValueForFinalStaticField;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CustomCodesTasklet.class})
public class CustomCodesTaskletTest {

    @Mock
    private JdbcTemplate martJdbcTemplate;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private CopyManager copyManager;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private FileInputStream fileInputStream;

    @Mock
    private Logger logger;

    @Mock
    private Scanner scanner;

    private String readerFilePath;
    private CustomCodesTasklet customCodesTasklet;

    @Before
    public void setUp() throws Exception {
        customCodesTasklet = new CustomCodesTasklet();
        readerFilePath = "path of reader file";
        String headers = "name,source,type,code";
        setValuesForMemberFields(customCodesTasklet, "martJdbcTemplate", martJdbcTemplate);
        setValuesForMemberFields(customCodesTasklet, "readerFilePath", readerFilePath);
        setValuesForMemberFields(customCodesTasklet, "copyManager", copyManager);
        setValueForFinalStaticField(CustomCodesTasklet.class, "logger", logger);

        whenNew(FileInputStream.class).withArguments(readerFilePath).thenReturn(fileInputStream);
        whenNew(Scanner.class).withArguments(fileInputStream).thenReturn(scanner);
        when(scanner.nextLine()).thenReturn(headers);
    }

    @Test
    public void shouldCreateCustomCodesTableAndExportDataFromGivenPath() throws Exception {
        String tableCreationSql = "DROP TABLE IF EXISTS custom_codes; " +
                "CREATE TABLE custom_codes ( name TEXT NOT NULL, source TEXT NOT NULL ,type TEXT ,code TEXT," +
                "UNIQUE (name, type ,source));";

        customCodesTasklet.execute(stepContribution, chunkContext);

        verify(martJdbcTemplate, times(1)).execute(tableCreationSql);
        verify(copyManager, times(1)).copyIn(eq(
                "COPY custom_codes (name,source,type,code) FROM STDIN DELIMITER ',' CSV HEADER"),
                any(FileInputStream.class));
    }

    @Test
    public void shouldLogErrorGivenImproperFilePath() throws Exception {
        doThrow(DataAccessResourceFailureException.class).when(copyManager).copyIn(anyString(), eq(fileInputStream));

        customCodesTasklet.execute(stepContribution, chunkContext);

        verify(logger, times(1)).error(anyString(), any(DataAccessResourceFailureException.class));
    }

    @Test
    public void shouldLogErrorGivenImproperDataInCSV() throws Exception {
        doThrow(DataIntegrityViolationException.class).when(copyManager).copyIn(anyString(), eq(fileInputStream));

        customCodesTasklet.execute(stepContribution, chunkContext);

        verify(logger, times(1)).error(anyString(), any(DataIntegrityViolationException.class));
    }

    @Test
    public void shouldLogErrorGivenImproperImportSql() throws Exception {
        doThrow(SQLException.class).when(copyManager).copyIn(anyString(), eq(fileInputStream));

        customCodesTasklet.execute(stepContribution, chunkContext);

        verify(logger, times(1)).error(anyString(), any(SQLException.class));
    }

    @Test
    public void shouldLogErrorForWrongCSVFilePath() throws Exception {
        whenNew(FileInputStream.class).withArguments(readerFilePath).thenThrow(new IOException());

        customCodesTasklet.execute(stepContribution, chunkContext);

        verify(logger, times(1)).error(anyString(), any(IOException.class));
        verify(martJdbcTemplate, times(1)).execute(anyString());
        verify(copyManager, times(0)).copyIn(any(), any(FileInputStream.class));
    }

    @Test
    public void shouldNotCopyWhenHeadersAreNotValid() throws Exception {
        String headers = "invalid,headers";
        when(scanner.nextLine()).thenReturn(headers);

        customCodesTasklet.execute(stepContribution, chunkContext);

        verify(copyManager, never()).copyIn(anyString(), any(InputStream.class));
    }
}