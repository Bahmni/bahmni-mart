package org.bahmni.mart.table;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.table.domain.TableColumn;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;

@Component("Form2TableMetadataGenerator")
public class Form2TableMetadataGenerator extends TableMetadataGenerator {

    @Override
    protected List<TableColumn> getForeignKeyColumn(BahmniForm form) {
        return null;
    }

    @Override
    protected List<TableColumn> getColumns(BahmniForm form) {
        List<TableColumn> columns = new ArrayList<>();
        columns.add(new TableColumn("patient_id", "integer", false, null));
        columns.add(new TableColumn("obs_datetime", "timestamp", false, null));
        columns.add(new TableColumn("date_created", "timestamp", false, null));
        columns.add(new TableColumn("date_modified", "timestamp", false, null));
        columns.add(new TableColumn("location_id", "integer", false, null));
        columns.add(new TableColumn("location_name", "text", false, null));
        columns.add(new TableColumn("program_id", "integer", false, null));
        columns.add(new TableColumn("program_name", "text", false, null));
        if (!isNull(form.getParent())) {
            final TableColumn referenceColumn = getParentReferenceColumn();
            columns.add(referenceColumn);
        }
        columns.addAll(getPrimaryColumns());
        columns.addAll(getNonKeyColumns(form));
        return columns;
    }

    private List<TableColumn> getPrimaryColumns() {
        TableColumn formFieldPathColumn = new TableColumn("form_field_path",
                "text", true, null);
        TableColumn encounterIdColumn = new TableColumn("encounter_id", "integer",
                true, null);
        return Arrays.asList(formFieldPathColumn, encounterIdColumn);
    }

    private TableColumn getParentReferenceColumn() {
        return new TableColumn("reference_form_field_path", "text", false, null);
    }
}
