package org.bahmni.mart.config.view;

import org.bahmni.mart.helper.RegConfigHelper;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.TableMetadataGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.bahmni.mart.table.FormTableMetadataGenerator.addPrefixToName;
import static org.bahmni.mart.table.FormTableMetadataGenerator.getProcessedName;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FormTableMetadataGenerator.class)
public class RegViewDefinitionTest {

    @Mock
    private RegConfigHelper regConfigHelper;

    @Mock
    private NamedParameterJdbcTemplate martNamedJdbcTemplate;

    private RegViewDefinition regViewDefinition;

    @Before
    public void setUp() throws Exception {
        regViewDefinition = new RegViewDefinition();
        setValuesForMemberFields(regViewDefinition, "regConfigHelper", regConfigHelper);
        setValuesForMemberFields(regViewDefinition, "martNamedJdbcTemplate", martNamedJdbcTemplate);
    }

    @Test
    public void shouldReturnViewDefinitionWithSql() {
        String sql = "SELECT COALESCE(reg_nutritional_values.patient_id,reg_fee_information.patient_id) AS patient_id" +
                ", COALESCE(reg_nutritional_values.encounter_id,reg_fee_information.encounter_id) AS encounter_id," +
                " COALESCE(reg_nutritional_values.visit_id,reg_fee_information.visit_id) AS visit_id," +
                " COALESCE(reg_nutritional_values.location_id,reg_fee_information.location_id) AS location_id," +
                " COALESCE(reg_nutritional_values.location_name,reg_fee_information.location_name) AS location_name," +
                " LEAST(reg_nutritional_values.obs_datetime,reg_fee_information.obs_datetime) AS obs_datetime," +
                " LEAST(reg_nutritional_values.date_created,reg_fee_information.date_created) AS date_created," +
                " GREATEST(reg_nutritional_values.date_modified,reg_fee_information.date_modified) AS date_modified," +
                " COALESCE(reg_nutritional_values.program_id,reg_fee_information.program_id) AS program_id," +
                " COALESCE(reg_nutritional_values.program_name,reg_fee_information.program_name) AS program_name," +
                " COALESCE(reg_nutritional_values.patient_program_id,reg_fee_information.patient_program_id) " +
                "AS patient_program_id," +
                "   FROM reg_nutritional_values FULL OUTER JOIN reg_fee_information " +
                "ON reg_fee_information.encounter_id = reg_nutritional_values.encounter_id";
        String viewName = "registration_second_page_view";

        Map<String, Object> nutritionalValue = new HashMap<>();
        nutritionalValue.put("table_name", "reg_nutritional_values");
        nutritionalValue.put("column_name", "id_reg_nutritional_values");

        Map<String, Object> feeInfo = new HashMap<>();
        feeInfo.put("table_name", "reg_fee_information");
        feeInfo.put("column_name", "id_reg_fee_information");

        List<Map<String, Object>> metaData = Arrays.asList(nutritionalValue, feeInfo);

        mockStatic(FormTableMetadataGenerator.class);
        mockStatic(TableMetadataGenerator.class);
        when(regConfigHelper.getRegConcepts()).thenReturn(Arrays.asList("Nutritional Values", "Fee Information"));
        when(addPrefixToName("Nutritional Values", "Reg")).thenReturn("Reg Nutritional Values");
        when(addPrefixToName("Fee Information", "Reg")).thenReturn("Reg Fee Information");
        when(getProcessedName("Reg Nutritional Values")).thenReturn("reg_nutritional_values");
        when(getProcessedName("Reg Fee Information")).thenReturn("reg_fee_information");
        when(martNamedJdbcTemplate.queryForList(anyString(), any(Map.class))).thenReturn(metaData);

        ViewDefinition definition = regViewDefinition.getDefinition();

        verify(regConfigHelper, times(1)).getRegConcepts();
        verifyStatic(times(1));
        addPrefixToName("Nutritional Values", "Reg");
        verifyStatic(times(1));
        addPrefixToName("Fee Information", "Reg");
        verifyStatic(times(1));
        getProcessedName("Reg Nutritional Values");
        verifyStatic(times(1));
        getProcessedName("Reg Fee Information");
        verify(martNamedJdbcTemplate, times(1)).queryForList(anyString(), any(Map.class));

        assertEquals(viewName, definition.getName());
        assertEquals(sql, definition.getSql());
    }

    @Test
    public void shouldReturnEmptyViewDefinitionSql() {
        when(regConfigHelper.getRegConcepts()).thenReturn(Collections.emptyList());

        ViewDefinition definition = regViewDefinition.getDefinition();

        verify(regConfigHelper, times(1)).getRegConcepts();
        assertEquals("registration_second_page_view", definition.getName());
        assertEquals("", definition.getSql());
    }
}
