package org.bahmni.mart.exports;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.helper.IncrementalUpdater;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.bahmni.mart.helper.DuplicateObsResolver.getUniqueObsItems;

@Component
@Scope(value = "prototype")
public class DatabaseObsWriter implements ItemWriter<List<Obs>> {

    @Autowired
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Qualifier("martJdbcTemplate")
    @Autowired
    private JdbcTemplate martJdbcTemplate;

    @Autowired
    private FreeMarkerEvaluator<ObsRecordExtractorForTable> freeMarkerEvaluatorForTableRecords;

    @Autowired
    private IncrementalUpdater incrementalUpdater;

    private BahmniForm form;

    private boolean isAddMoreMultiSelectEnabled = true;


    @Override
    public void write(List<? extends List<Obs>> items) throws Exception {
        TableData tableData = formTableMetadataGenerator.getTableData(form);
        if (!incrementalUpdater.isMetaDataChanged(form.getFormName().getName())) {
            deleteVoidedRecords(items, tableData);
        }
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

    private void deleteVoidedRecords(List<? extends List<Obs>> items, TableData tableData) {
        Set<String> encounterIds = new HashSet<>();
        items.forEach(item -> item.forEach(obs -> encounterIds.add(obs.getEncounterId())));
        incrementalUpdater.deleteVoidedRecords(encounterIds, tableData.getName(), "encounter_id");
    }

    public void setAddMoreMultiSelectEnabled(boolean addMoreMultiSelectEnabled) {
        this.isAddMoreMultiSelectEnabled = addMoreMultiSelectEnabled;
    }
}
