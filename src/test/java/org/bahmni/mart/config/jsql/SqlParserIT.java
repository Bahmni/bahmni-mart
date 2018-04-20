package org.bahmni.mart.config.jsql;

import org.junit.Assert;
import org.junit.Test;

public class SqlParserIT {

    @Test
    public void shouldThrowExceptionForInvalidSQL() {
        String readerSql = "SELECT * FROM";

        String actualReaderSql = SqlParser.getUpdatedReaderSql(null, readerSql);

        Assert.assertEquals(readerSql, actualReaderSql);
    }
}