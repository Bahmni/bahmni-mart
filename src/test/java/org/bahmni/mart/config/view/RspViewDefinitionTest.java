package org.bahmni.mart.config.view;

import org.bahmni.mart.helper.RspConfigHelper;
import org.bahmni.mart.table.FormTableMetadataGenerator;
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
public class RspViewDefinitionTest {

    @Mock
    private RspConfigHelper rspConfigHelper;

    @Mock
    private NamedParameterJdbcTemplate martNamedJdbcTemplate;

    private RspViewDefinition rspViewDefinition;

    @Before
    public void setUp() throws Exception {
        rspViewDefinition = new RspViewDefinition();
        setValuesForMemberFields(rspViewDefinition, "rspConfigHelper", rspConfigHelper);
        setValuesForMemberFields(rspViewDefinition, "martNamedJdbcTemplate", martNamedJdbcTemplate);
    }

    @Test
    public void shouldReturnViewDefinitionWithSql() {
        String sql = "SELECT COALESCE(rsp_nutritional_values.patient_id,rsp_fee_information.patient_id) AS" +
                " patient_id, COALESCE(rsp_nutritional_values.encounter_id,rsp_fee_information.encounter_id) AS" +
                " encounter_id,  FROM rsp_nutritional_values FULL OUTER JOIN rsp_fee_information " +
                "ON rsp_fee_information.encounter_id = rsp_nutritional_values.encounter_id";
        String viewName = "registration_second_page_view";

        Map<String, Object> nutritionalValue = new HashMap<>();
        nutritionalValue.put("table_name", "rsp_nutritional_values");
        nutritionalValue.put("column_name", "id_rsp_nutritional_values");

        Map<String, Object> feeInfo = new HashMap<>();
        feeInfo.put("table_name", "rsp_fee_information");
        feeInfo.put("column_name", "id_rsp_fee_information");

        List<Map<String, Object>> metaData = Arrays.asList(nutritionalValue, feeInfo);

        mockStatic(FormTableMetadataGenerator.class);
        when(rspConfigHelper.getRspConcepts()).thenReturn(Arrays.asList("Nutritional Values", "Fee Information"));
        when(addPrefixToName("Nutritional Values", "Rsp")).thenReturn("Rsp Nutritional Values");
        when(addPrefixToName("Fee Information", "Rsp")).thenReturn("Rsp Fee Information");
        when(getProcessedName("Rsp Nutritional Values")).thenReturn("rsp_nutritional_values");
        when(getProcessedName("Rsp Fee Information")).thenReturn("rsp_fee_information");
        when(martNamedJdbcTemplate.queryForList(anyString(), any(Map.class))).thenReturn(metaData);

        ViewDefinition definition = rspViewDefinition.getDefinition();

        verify(rspConfigHelper, times(1)).getRspConcepts();
        verifyStatic(times(1));
        addPrefixToName("Nutritional Values", "Rsp");
        verifyStatic(times(1));
        addPrefixToName("Fee Information", "Rsp");
        verifyStatic(times(1));
        getProcessedName("Rsp Nutritional Values");
        verifyStatic(times(1));
        getProcessedName("Rsp Fee Information");
        verify(martNamedJdbcTemplate, times(1)).queryForList(anyString(), any(Map.class));

        assertEquals(viewName, definition.getName());
        assertEquals(sql, definition.getSql());
    }

    @Test
    public void shouldReturnEmptyViewDefinitionSql() {
        when(rspConfigHelper.getRspConcepts()).thenReturn(Collections.emptyList());

        ViewDefinition definition = rspViewDefinition.getDefinition();

        verify(rspConfigHelper, times(1)).getRspConcepts();
        assertEquals("registration_second_page_view", definition.getName());
        assertEquals("", definition.getSql());
    }
}