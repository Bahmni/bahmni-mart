package org.bahmni.mart.form2.service;

import org.bahmni.mart.BatchUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BatchUtils.class)
public class FormServiceTest {

    FormService formService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    private String sql;

    @Before
    public void setUp() throws Exception {
        mockStatic(BatchUtils.class);
        sql = "form list sql";
        when(convertResourceOutputToString(any(Resource.class))).thenReturn(sql);
        formService = new FormService();
        setValuesForMemberFields(formService, "openmrsDbTemplate", jdbcTemplate);
    }

    @Test
    public void shouldReturnMapWithKeysAsFormNamesAndValuesAsLatestVersion() {

        String formNameAndVersionSql = "SELECT name , MAX(version) as version FROM form GROUP BY name";
        Map<String, Object> formRow = new LinkedHashMap<>();
        formRow.put("name", "Vitals");
        formRow.put("version", "3");
        List<Map<String, Object>> formRows = new ArrayList<>();
        formRows.add(formRow);
        when(jdbcTemplate.queryForList(formNameAndVersionSql)).thenReturn(formRows);

        Map<String, Integer> formNameAndVersionMap = formService.getFormNamesWithLatestVersionNumber();

        assertEquals(3, formNameAndVersionMap.get("Vitals").intValue());
    }

    @Test
    public void shouldReturnEmptyMapWhenNoFormsAvailable() {

        when(jdbcTemplate.queryForList(sql)).thenReturn(new ArrayList<>());
        Map<String, Integer> formNameAndVersionMap = formService.getFormNamesWithLatestVersionNumber();

        assertEquals(0, formNameAndVersionMap.size());
    }
}