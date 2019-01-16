package org.bahmni.mart.table;

import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.table.domain.ForeignKey;
import org.bahmni.mart.table.domain.TableColumn;
import org.bahmni.mart.table.domain.TableData;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component("Form2TableMetadataGenerator")
public class Form2TableMetadataGenerator extends TableMetadataGenerator {

    @Override
    protected List<TableColumn> getForeignKeyColumn(BahmniForm form) {
        if (form.getParent() != null) {
            Concept formParentConcept = form.getParent().getFormName();
            String formParentConceptName = formParentConcept.getName();
            String referenceTableName = getProcessedName(formParentConceptName);
            referenceTableName = SpecialCharacterResolver.getUpdatedTableNameIfExist(referenceTableName);
            String referenceColumn = "form_field_path" ;
            ForeignKey reference = new ForeignKey(referenceColumn, referenceTableName);
            return Arrays.asList(new TableColumn(referenceColumn+ "_"+referenceTableName, "text", false, reference),
                    new TableColumn("encounter_id", "integer", true, new ForeignKey("encounter_id", referenceTableName)));
        }
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
        List<TableColumn> foreignKeyColumn = getForeignKeyColumn(form);
        final boolean hasForeignKeyColumn = foreignKeyColumn != null;
        if (hasForeignKeyColumn)
            columns.addAll(foreignKeyColumn);
        columns.addAll(getPrimaryColumns(hasForeignKeyColumn));
        columns.addAll(getNonKeyColumns(form));
        return columns;
    }

    private List<TableColumn> getPrimaryColumns(boolean hasForeignKeyColumn) {
        TableColumn formFieldPathColumn = new TableColumn("form_field_path",
                "text", true, null);
        if(hasForeignKeyColumn)
            return Arrays.asList(formFieldPathColumn);
        return Arrays.asList(formFieldPathColumn,new TableColumn("encounter_id", "integer", true, null) );
    }

}
