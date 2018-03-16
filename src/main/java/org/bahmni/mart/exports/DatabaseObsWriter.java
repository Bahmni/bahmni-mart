package org.bahmni.mart.exports;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Obs;
import org.bahmni.mart.helper.FreeMarkerEvaluator;
import org.bahmni.mart.table.FormTableMetadataGenerator;
import org.bahmni.mart.table.domain.TableData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope(value = "prototype")
public class DatabaseObsWriter implements ItemWriter<List<Obs>> {

    private static final Logger log = LoggerFactory.getLogger(DatabaseObsWriter.class);

    @Qualifier("martJdbcTemplate")
    @Autowired
    private JdbcTemplate martJdbcTemplate;


    @Autowired
    public FormTableMetadataGenerator formTableMetadataGenerator;

    @Autowired
    private FreeMarkerEvaluator<ObsRecordExtractorForTable> freeMarkerEvaluatorForTableRecords;

    private BahmniForm form;


    @Override
    public void write(List<? extends List<Obs>> items) throws Exception {
        insertRecords(items);
    }

    public void setForm(BahmniForm form) {
        this.form = form;
    }

    private void insertRecords(List<? extends List<Obs>> items) {
        TableData tableData = formTableMetadataGenerator.getTableData(this.form);
        try {
            ObsRecordExtractorForTable extractor = new ObsRecordExtractorForTable(tableData.getName());
            extractor.execute(items, tableData);
            String sql = freeMarkerEvaluatorForTableRecords.evaluate("insertObs.ftl", extractor);
            martJdbcTemplate.execute(sql);
        } catch (Exception e) {
            log.error(String.format("Cannot insert into %s %s", tableData.getName(), e.getMessage()));
        }
    }
}
