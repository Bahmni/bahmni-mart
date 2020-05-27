package org.bahmni.mart.form2.translations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TranslationMetadata {

    private static final String  TRANSLATION_FILES_LOCATION_SQL = "SELECT property_value FROM global_property " +
            "WHERE property = 'bahmni.formTranslations.directory'";

    private final JdbcTemplate openmrsJdbcTemplate;

    @Autowired
    public TranslationMetadata(JdbcTemplate openmrsJdbcTemplate) {
        this.openmrsJdbcTemplate = openmrsJdbcTemplate;
    }

    public String getTranslationsFilePath(String formName, int formVersion) {

        String fromTranslationsPath = openmrsJdbcTemplate.queryForObject(TRANSLATION_FILES_LOCATION_SQL, String.class);

        return String.format("%s/%s_%s.json", fromTranslationsPath, formName, formVersion);
    }

    public String getNormalizedTranslationsFilePath(String formName, int formVersion) {

        final String VALID_FILE_NAME_CHAR_REGEX = "[^a-zA-Z0-9_\\-.]";

        String fromTranslationsPath = openmrsJdbcTemplate.queryForObject(TRANSLATION_FILES_LOCATION_SQL, String.class);

        return String.format("%s/%s_%s.json", fromTranslationsPath,
                formName.replaceAll(VALID_FILE_NAME_CHAR_REGEX,"_"),
                formVersion);
    }

    public String getTranslationsFilePathWithUuid(String formName, int formVersion) {

        String formTranslationsPath = openmrsJdbcTemplate.queryForObject(TRANSLATION_FILES_LOCATION_SQL, String.class);

        String queryForUUID = String.format("SELECT form.uuid FROM form WHERE version = %d AND form.name = \"%s\"",
                formVersion, formName);

        String formUuid = openmrsJdbcTemplate.queryForObject(queryForUUID, String.class);

        return String.format("%s/%s.json", formTranslationsPath, formUuid);
    }
}
