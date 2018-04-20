package org.bahmni.mart.table;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.CodeConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest(BatchUtils.class)
@RunWith(PowerMockRunner.class)
public class CodesProcessorTest {

    @Mock
    private Resource codesSqlResource;

    @Mock
    private DataSource martDataSource;

    @Mock
    private NamedParameterJdbcTemplate martJdbcTemplate;

    private CodesProcessor codesProcessor;

    private List<Map<String, String>> codesList;

    @Before
    public void setUp() throws Exception {
        codesProcessor = new CodesProcessor();
        codesList = new ArrayList<>();
        Map<String, String> code1 = new HashMap<>();
        code1.put("name", "Ward");
        code1.put("type", "bed-management");
        code1.put("code", "ward101");
        Map<String, String> code2 = new HashMap<>();
        code2.put("name", "endtb");
        code2.put("type", "programs");
        code2.put("code", "endtb101");
        codesList.add(code1);
        codesList.add(code2);
        setValuesForMemberFields(codesProcessor, "codesSqlResource", codesSqlResource);
        setValuesForMemberFields(codesProcessor, "martNamedJdbcTemplate", martJdbcTemplate);
    }

    @Test
    public void shouldSetUpCodesData() throws Exception {
        List<CodeConfig> codeConfigs = setUpCodeConfigs();
        Map<String, String> endTbCode = new HashMap<>();
        endTbCode.put("name", "endtb");
        endTbCode.put("type", "programs");
        endTbCode.put("code", "endtb101");
        List<Map<String, String>> codesList1 = new ArrayList<>();
        codesList1.add(endTbCode);

        mockStatic(BatchUtils.class);
        String sql = "some sql";
        when(BatchUtils.convertResourceOutputToString(codesSqlResource)).thenReturn(sql);
        List<Map<String, String>> paramsList = setUpParams();
        when(martJdbcTemplate.query(eq(sql), eq(paramsList.get(0)),
                any(CodesExtractor.class))).thenReturn(codesList1);
        when(martJdbcTemplate.query(eq(sql), eq(paramsList.get(1)),
                any(CodesExtractor.class))).thenReturn(this.codesList);

        Map<String, String> spyColumnsToCode = spy(HashMap.class);
        setValuesForMemberFields(codesProcessor, "columnsToCode", spyColumnsToCode);
        List<Map<String, String>> spyCodesList = spy(ArrayList.class);
        setValuesForMemberFields(codesProcessor, "codes", spyCodesList);

        codesProcessor.setUpCodesData(codeConfigs);

        assertNotNull(spyColumnsToCode);
        assertEquals(2, spyColumnsToCode.size());
        assertTrue(spyColumnsToCode.containsKey("location"));
        assertTrue(spyColumnsToCode.containsKey("program"));
        assertEquals("bed-management", spyColumnsToCode.get("location"));
        assertEquals("programs", spyColumnsToCode.get("program"));
        assertNotNull(spyCodesList);

        assertEquals(3, spyCodesList.size());
        codesList1.addAll(this.codesList);
        assertTrue(Arrays.equals(spyCodesList.toArray(), codesList1.toArray()));
    }

    @Test
    public void shouldProcessGivenItemWithCodes() throws Exception {
        HashMap<String, Object> item = new HashMap<>();
        item.put("location", "Ward");
        item.put("program", "endtb");
        setValuesForMemberFields(codesProcessor, "codes", codesList);
        Map<String, String> columnsToCode = new HashMap<>();
        columnsToCode.put("location", "bed-management");
        columnsToCode.put("program", "programs");
        setValuesForMemberFields(codesProcessor, "columnsToCode", columnsToCode);

        Map<String, Object> processedItem = codesProcessor.process(item);

        assertNotNull(processedItem);
        assertEquals(2, processedItem.size());
        assertEquals("ward101", processedItem.get("location"));
        assertEquals("endtb101", processedItem.get("program"));
    }

    @Test
    public void shouldGiveTheActualValueIfColumnIsNotListedInColumnsToCode() throws Exception {
        HashMap<String, Object> item = new HashMap<>();
        item.put("location", "Ward");
        item.put("program", "endtb");
        setValuesForMemberFields(codesProcessor, "codes", codesList);
        Map<String, String> columnsToCode = new HashMap<>();
        setValuesForMemberFields(codesProcessor, "columnsToCode", columnsToCode);

        Map<String, Object> processedItem = codesProcessor.process(item);

        assertNotNull(processedItem);
        assertEquals(2, processedItem.size());
        assertEquals("Ward", processedItem.get("location"));
        assertEquals("endtb", processedItem.get("program"));
    }

    @Test
    public void shouldGiveTheActualValueIfThereIsNoCodeForIt() throws Exception {
        HashMap<String, Object> item = new HashMap<>();
        item.put("location", "Ward");
        item.put("program", "Refractive Surgery");
        setValuesForMemberFields(codesProcessor, "codes", codesList);
        Map<String, String> columnsToCode = new HashMap<>();
        columnsToCode.put("location", "bed-management");
        columnsToCode.put("program", "programs");
        setValuesForMemberFields(codesProcessor, "columnsToCode", columnsToCode);

        Map<String, Object> processedItem = codesProcessor.process(item);

        assertNotNull(processedItem);
        assertEquals(2, processedItem.size());
        assertEquals("ward101", processedItem.get("location"));
        assertEquals("Refractive Surgery", processedItem.get("program"));
    }

    @Test
    public void shouldGiveEmptyStringsWhenValueIsNull() throws Exception {
        HashMap<String, Object> item = new HashMap<>();
        item.put("location", null);
        item.put("program", null);
        setValuesForMemberFields(codesProcessor, "codes", codesList);
        Map<String, String> columnsToCode = new HashMap<>();
        columnsToCode.put("location", "bed-management");
        columnsToCode.put("program", "programs");
        setValuesForMemberFields(codesProcessor, "columnsToCode", columnsToCode);

        Map<String, Object> processedItem = codesProcessor.process(item);

        assertNotNull(processedItem);
        assertEquals(2, processedItem.size());
        assertEquals("", processedItem.get("location"));
        assertEquals("", processedItem.get("program"));
    }

    @Test
    public void shouldGiveActualValueWhenTypeDoesNotMatch() throws Exception {
        HashMap<String, Object> item = new HashMap<>();
        item.put("location", "ward");
        item.put("program", "endtb");
        setValuesForMemberFields(codesProcessor, "codes", codesList);
        Map<String, String> columnsToCode = new HashMap<>();
        columnsToCode.put("location", "ot");
        columnsToCode.put("program", "programs");
        setValuesForMemberFields(codesProcessor, "columnsToCode", columnsToCode);

        Map<String, Object> processedItem = codesProcessor.process(item);

        assertNotNull(processedItem);
        assertEquals(2, processedItem.size());
        assertEquals("ward", processedItem.get("location"));
        assertEquals("endtb101", processedItem.get("program"));
    }

    private List<CodeConfig> setUpCodeConfigs() {
        CodeConfig codeConfig1 = Mockito.mock(CodeConfig.class);
        CodeConfig codeConfig2 = Mockito.mock(CodeConfig.class);
        when(codeConfig1.getSource()).thenReturn("Bahmni-internal");
        when(codeConfig1.getColumnsToCode()).thenReturn(Arrays.asList("location"));
        when(codeConfig1.getType()).thenReturn("bed-management");
        when(codeConfig2.getSource()).thenReturn("Bahmni-internal");
        when(codeConfig2.getColumnsToCode()).thenReturn(Arrays.asList("program"));
        when(codeConfig2.getType()).thenReturn("programs");
        return Arrays.asList(codeConfig1, codeConfig2);
    }

    private List<Map<String, String>> setUpParams() {
        Map<String, String> params1 = new HashMap<>();
        Map<String, String> params2 = new HashMap<>();
        params1.put("type", "bed-management");
        params1.put("source", "Bahmni-internal");
        params2.put("type", "programs");
        params2.put("source", "Bahmni-internal");
        return Arrays.asList(params1, params2);
    }
}