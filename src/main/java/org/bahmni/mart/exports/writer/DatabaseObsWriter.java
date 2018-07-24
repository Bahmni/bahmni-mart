package org.bahmni.mart.exports.writer;

import org.bahmni.mart.exports.ObsRecordExtractorForTable;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.bahmni.mart.helper.DuplicateObsResolver.getUniqueObsItems;

@Component
@Scope(value = "prototype")
public class DatabaseObsWriter extends BaseWriter implements ItemWriter<List<Obs>> {

    @Autowired
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Autowired
    private FreeMarkerEvaluator<ObsRecordExtractorForTable> freeMarkerEvaluatorForTableRecords;

    private BahmniForm form;

    private boolean isAddMoreMultiSelectEnabled = true;

    @Override
    public void write(List<? extends List<Obs>> items) {
        TableData tableData = formTableMetadataGenerator.getTableData(form);
        insertRecords(items, tableData);
    }

    public void setForm(BahmniForm form) {
        this.form = form;
    }

    private void insertRecords(List<? extends List<Obs>> items, TableData tableData) {
        ObsRecordExtractorForTable extractor = getObsRecordExtractor(items, tableData);
        String sql = freeMarkerEvaluatorForTableRecords.evaluate("insertObs.ftl", extractor);
        martJdbcTemplate.execute(sql);
    }

    private ObsRecordExtractorForTable getObsRecordExtractor(List<? extends List<Obs>> items, TableData tableData) {
        ObsRecordExtractorForTable extractor = new ObsRecordExtractorForTable(tableData.getName());
        extractor.setAddMoreMultiSelectEnabledForSeparateTables(isAddMoreMultiSelectEnabled);
        if (isAddMoreMultiSelectEnabled) {
            extractor.execute(items, tableData);
            return extractor;
        }
        List<List<Obs>> uniqueObsItems = getUniqueObsItems(items);
        extractor.execute(uniqueObsItems, tableData);
        return extractor;
    }

    public void setAddMoreMultiSelectEnabled(boolean addMoreMultiSelectEnabled) {
        this.isAddMoreMultiSelectEnabled = addMoreMultiSelectEnabled;
    }
}
