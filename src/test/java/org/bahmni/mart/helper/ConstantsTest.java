package org.bahmni.mart.helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConstantsTest {

    @Test
    public void shouldMapIntToInteger() {
        String psqlDatatype = Constants.getPostgresDataTypeFor("int");

        assertEquals("integer", psqlDatatype);
    }

    @Test
    public void shouldMapInt4ToInteger() {
        String psqlDatatype = Constants.getPostgresDataTypeFor("Int4");

        assertEquals("integer", psqlDatatype);
    }

}