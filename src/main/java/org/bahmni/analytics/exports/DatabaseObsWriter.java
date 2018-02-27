package org.bahmni.analytics.exports;

import org.bahmni.analytics.form.domain.BahmniForm;
import org.bahmni.analytics.form.domain.Obs;
import org.bahmni.analytics.helper.FreeMarkerEvaluator;
import org.bahmni.analytics.table.FormTableMetadataGenerator;
import org.bahmni.analytics.table.domain.TableData;
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

    @Qualifier("postgresJdbcTemplate")
    @Autowired
    private JdbcTemplate postgresJdbcTemplate;


    @Autowired
    public FormTableMetadataGenerator formTableMetadataGenerator;

    @Autowired
    private FreeMarkerEvaluator<ObsRecordExtractorForTable> freeMarkerEvaluatorForTableRecords;

    BahmniForm form;


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
            postgresJdbcTemplate.execute(sql);
        } catch (Exception e) {
            log.error(String.format("Cannot insert into %s %s", tableData.getName(), e.getMessage()));
        }
    }
}
