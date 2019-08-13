package org.bahmni.mart.form2;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope(value = "prototype")
public class Form2ObservationProcessor implements ItemProcessor<Map<String, Object>, List<Obs>> {

    private static final String SLASH = "/";
    private static final String DOT = ".";
    private BahmniForm form;

    @Value("classpath:sql/form2Obs.sql")
    private Resource formObsSqlResource;

    @Autowired
    @Qualifier("openmrsNamedJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private String form2ObsSql;
    private JobDefinition jobDefinition;

    @Override
    public List<Obs> process(Map<String, Object> encounterIdMap) {
        String encounterId = String.valueOf(encounterIdMap.get("encounter_id"));
        return getFormObs(encounterId);
    }

    private List<Obs> getFormObs(String encounterId) {
        List<String> conceptNames = new ArrayList<>(form.getFieldNameAndFullySpecifiedNameMap().keySet());
        Map<String, Object> params = new HashMap<>();
        String rootFormName = form.getRootForm() != null ? form.getRootForm().getFormName().getName()
                : form.getFormName().getName();
        params.put("formName", rootFormName);
        params.put("encounterId", encounterId);
        params.put("conceptNames", conceptNames);
        params.put("conceptReferenceSource", jobDefinition.getConceptReferenceSource());
        return getObs(params, form2ObsSql);
    }

    private List<Obs> getObs(Map<String, Object> params, String queryString) {
        return jdbcTemplate.query(queryString, params, new BeanPropertyRowMapper<Obs>(Obs.class) {
            @Override
            public Obs mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
                Obs obs = super.mapRow(resultSet, rowNumber);
                Concept concept = new Concept();
                concept.setId(resultSet.getInt("conceptId"));
                concept.setName(form.getFieldNameAndFullySpecifiedNameMap()
                        .get(resultSet.getString("conceptName")));
                obs.setField(concept);
                setFormFieldPath(obs,obs.getFormFieldPath());
                return obs;
            }
        });
    }

    private void setFormFieldPath(Obs obs, String formFieldPath) {
        int formDepthToParent = form.getDepthToParent();
        obs.setFormFieldPath(getProcessedFormFieldPath(formFieldPath, formDepthToParent));
        if (formDepthToParent != 0) {
            int parentFormDepthToParent = form.getParent().getDepthToParent();
            obs.setReferenceFormFieldPath(getProcessedFormFieldPath(formFieldPath, parentFormDepthToParent));
        }
    }

    private String getProcessedFormFieldPath(String formFieldPath, int depthToParent) {
        if (depthToParent == 0) {
            return getFormNameFrom(formFieldPath);
        }
        int slashIndex = getSlashIndex(formFieldPath, depthToParent);
        return StringUtils.substring(formFieldPath, 0, slashIndex);
    }

    private String getFormNameFrom(String formFieldPath) {
        return StringUtils.substringBefore(formFieldPath, DOT);
    }

    private int getSlashIndex(String formFieldPath, int depthToParent) {
        int slashCount = 0;
        int slashIndex = -1;
        while (slashCount <= depthToParent) {
            slashIndex = formFieldPath.indexOf(SLASH, slashIndex + 1);
            slashCount++;
        }
        return slashIndex == -1 ? formFieldPath.length() : slashIndex;
    }

    public void setForm(BahmniForm form) {
        this.form = form;
    }

    public void setJobDefinition(JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
    }

    @PostConstruct
    public void postConstruct() {
        this.form2ObsSql = BatchUtils.convertResourceOutputToString(formObsSqlResource);
    }
}
