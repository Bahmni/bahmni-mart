package org.bahmni.mart.exports;

import com.google.gson.Gson;
import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.table.TableMetadataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class AsIsTableMetadataGenerator implements TableMetadataGenerator {

    private List<TableData> tables = new ArrayList<TableData>();
    @Value("classpath:metadata.json")
    private Resource metadataJson;

    @Override
    public List<TableData> getTables() {
        if (tables.isEmpty()) {
            readTableDataFromJson();
        }
        return tables;
    }

    private void readTableDataFromJson() {
        TableData[] tablesData = new Gson().fromJson(
                BatchUtils.convertResourceOutputToString(metadataJson),TableData[].class);
        if (tablesData != null) {
            tables = Arrays.asList(tablesData);
        }
    }

}
