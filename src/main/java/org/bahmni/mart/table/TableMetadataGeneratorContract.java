package org.bahmni.mart.table;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
public interface TableMetadataGeneratorContract {
    List<TableData> getTableDataList();
    void addMetadataForForm(BahmniForm form);
    TableData getTableData(BahmniForm form);
}
