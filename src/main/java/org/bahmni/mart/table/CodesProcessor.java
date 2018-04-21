package org.bahmni.mart.table;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.CodeConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CodesProcessor implements PreProcessor {

    @Value("classpath:sql/customCodes.sql")
    private Resource codesSqlResource;

    @Autowired
    @Qualifier("martNamedJdbcTemplate")
    NamedParameterJdbcTemplate martNamedJdbcTemplate;

    private List<Map<String, String>> codes;

    private Map<String, String> columnsToCode;

    @Autowired
    public CodesProcessor() {
        codes = new ArrayList<>();
        columnsToCode = new HashMap<>();
    }

    @Override
    public Map<String, Object> process(Map<String, Object> item) {
        return item.entrySet().stream().collect(
                Collectors.toMap(Map.Entry::getKey, p -> getProcessedValue(p.getKey(), p.getValue())));
    }

    public void setUpCodesData(List<CodeConfig> codeConfigs) {
        codeConfigs.forEach(codeConfig -> {
            setColumnsToCode(codeConfig.getColumnsToCode(), codeConfig.getType());
            setCodes(fetchCodes(codeConfig));
        });
    }

    private String getProcessedValue(String key, Object value) {
        if (value == null)
            return "";

        return columnsToCode.keySet().contains(key) ? getCode(value, columnsToCode.get(key)) : value.toString();
    }

    private List<Map<String, String>> fetchCodes(CodeConfig codeConfig) {
        Map<String, String> params = new HashMap<>();
        params.put("source", codeConfig.getSource());
        params.put("type", codeConfig.getType());
        String sql = BatchUtils.convertResourceOutputToString(codesSqlResource);
        return martNamedJdbcTemplate.query(sql, params, new CodesExtractor());
    }

    private String getCode(Object value, String type) {
        for (Map<String, String> rowMap : codes) {
            if (value.equals(rowMap.get("name")) && type.equals(rowMap.get("type"))) {
                return rowMap.get("code");
            }
        }

        return value.toString();
    }

    private void setCodes(List<Map<String, String>> codes) {
        this.codes.addAll(codes);
    }

    private void setColumnsToCode(List<String> columnsToCode, String type) {
        columnsToCode.forEach(column -> this.columnsToCode.put(column, type));
    }
}
