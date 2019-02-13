package org.bahmni.mart.exports.writer;

import org.bahmni.mart.exports.ObsRecordExtractorForTable;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.Form2TableMetadataGenerator;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.bahmni.mart.helper.DuplicateObsResolver.getUniqueObsItems;

@Component
@Scope(value = "prototype")
public class DatabaseObsWriter extends BaseWriter implements ItemWriter<List<Obs>> {

    private static final String FORM2_OBS_JOB_TYPE = "form2obs";

    @Autowired
    private FormTableMetadataGenerator formTableMetadataGenerator;

    @Autowired
    private Form2TableMetadataGenerator form2TableMetadataGenerator;

    @Autowired
    private FreeMarkerEvaluator<ObsRecordExtractorForTable> freeMarkerEvaluatorForTableRecords;

    private BahmniForm form;

    private boolean isAddMoreMultiSelectEnabled = true;

    @Override
    public void write(List<? extends List<Obs>> items) {
        if (FORM2_OBS_JOB_TYPE.equals(jobDefinition.getType())) {
            insertForm2Records(items);
        } else {
            insertForm1Records(items);
        }
    }

    private void insertForm1Records(List<? extends List<Obs>> items) {
        TableData tableData = formTableMetadataGenerator.getTableData(form);
        insertRecords(items, tableData);
    }

    private void insertForm2Records(List<? extends List<Obs>> items) {
        TableData tableData = form2TableMetadataGenerator.getTableData(form);
        List<? extends List<Obs>> groupedItems = form.isMultiSelect() ? items : groupForm2Obs(items);
        insertRecords(groupedItems, tableData);
    }

    private List<? extends List<Obs>> groupForm2Obs(List<? extends List<Obs>> items) {
        List<Obs> obsList = items.stream()
                .map(item -> item.get(0)).collect(Collectors.toList());
        Function<Obs, List<Object>> compositeKey = obs ->
                Arrays.asList(obs.getEncounterId(), obs.getFormFieldPath());
        Map<Object, List<Obs>> groupedObs = obsList.stream()
                .collect(Collectors.groupingBy(compositeKey, Collectors.toList()));
        List<List<Obs>> groupedItems = new ArrayList<>();
        groupedObs.forEach((key, value) -> groupedItems.add(value));
        return groupedItems;
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
