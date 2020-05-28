package org.bahmni.mart.form2.translations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class TranslationMetadata {

    private static final String  TRANSLATION_FILES_LOCATION_SQL = "SELECT property_value FROM global_property " +
            "WHERE property = 'bahmni.formTranslations.directory'";

    private final JdbcTemplate openmrsJdbcTemplate;
    private int formVersion;
    private String formName;

    @Autowired
    public TranslationMetadata(JdbcTemplate openmrsJdbcTemplate) {
        this.openmrsJdbcTemplate = openmrsJdbcTemplate;
    }

    public File getTranslationsFileWithFormName(String formName, int formVersion) {

        String fromTranslationsPath = openmrsJdbcTemplate.queryForObject(TRANSLATION_FILES_LOCATION_SQL, String.class);

        return new File(String.format("%s/%s_%s.json", fromTranslationsPath, formName, formVersion));
    }

    public File getNormalizedTranslationsFile(String formName, int formVersion) {

        final String VALID_FILE_NAME_CHAR_REGEX = "[^a-zA-Z0-9_\\-.]";

        String fromTranslationsPath = openmrsJdbcTemplate.queryForObject(TRANSLATION_FILES_LOCATION_SQL, String.class);

        return new File(String.format("%s/%s_%s.json", fromTranslationsPath,
                formName.replaceAll(VALID_FILE_NAME_CHAR_REGEX,"_"),
                formVersion));
    }

    public File getTranslationsFileWithUuid(String formName, int formVersion) {

        String formTranslationsPath = openmrsJdbcTemplate.queryForObject(TRANSLATION_FILES_LOCATION_SQL, String.class);

        String queryForUUID = String.format("SELECT form.uuid FROM form WHERE version = %d AND form.name = \"%s\"",
                formVersion, formName);

        String formUuid = openmrsJdbcTemplate.queryForObject(queryForUUID, String.class);

        return new File(String.format("%s/%s.json", formTranslationsPath, formUuid));
    }

}
