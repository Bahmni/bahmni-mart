package org.bahmni.mart.form;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope(value = "prototype")
public class ObservationProcessor implements ItemProcessor<Map<String, Object>, List<Obs>> {

    private String obsDetailSql;

    private String leafObsSql;

    private String formObsSql;

    private BahmniForm form;

    @Value("classpath:sql/obsDetail.sql")
    private Resource obsDetailSqlResource;

    @Value("classpath:sql/leafObs.sql")
    private Resource leafObsSqlResource;

    @Value("classpath:sql/formObs.sql")
    private Resource formObsSqlResource;

    @Autowired
    private FormFieldTransformer formFieldTransformer;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private JobDefinitionReader jobDefinitionReader;

    @Override
    public List<Obs> process(Map<String, Object> obsRow) throws Exception {
        List<Integer> allChildObsIds = new ArrayList<>();

        if (form.getFormName().getIsSet() == 1) {
            retrieveChildObsIds(allChildObsIds, Arrays.asList((Integer) obsRow.get("obs_id")));
        } else {
            allChildObsIds.add((Integer) obsRow.get("obs_id"));
        }

        List<Obs> obsRows = fetchAllLeafObs(allChildObsIds);
        obsRows.addAll(formObs((Integer) obsRow.get("obs_id")));
        setObsIdAndParentObsId(obsRows, (Integer) obsRow.get("obs_id"), (Integer) obsRow.get("parent_obs_id"));

        return obsRows;
    }

    private List<Obs> formObs(Integer obsId) {
        Map<String, Object> params = new HashMap<>();
        params.put("obsId", obsId);
        params.put("conceptReferenceSource", jobDefinitionReader.getConceptReferenceSource());
        return getObs(params, formObsSql);
    }

    private List<Obs> getObs(Map<String, ?> params, String queryString) {
        return jdbcTemplate.query(queryString, params, new BeanPropertyRowMapper<Obs>(Obs.class) {
            @Override
            public Obs mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
                Obs obs = super.mapRow(resultSet, rowNumber);
                Concept concept = new Concept(resultSet.getInt("conceptId"), resultSet.getString("conceptName"), 0, "");
                obs.setParentName(resultSet.getString("parentConceptName"));
                obs.setField(concept);
                return obs;
            }
        });
    }

    private List<Obs> fetchAllLeafObs(List<Integer> allChildObsGroupIds) {
        List<Integer> leafConcepts = formFieldTransformer.transformFormToFieldIds(form);

        if (allChildObsGroupIds.size() > 0 && leafConcepts.size() > 0) {
            Map<String, Object> params = new HashMap<>();
            params.put("childObsIds", allChildObsGroupIds);
            params.put("leafConceptIds", leafConcepts);
            params.put("conceptReferenceSource", jobDefinitionReader.getConceptReferenceSource());
            return getObs(params, leafObsSql);
        }
        return new ArrayList<>();
    }

    protected void retrieveChildObsIds(List<Integer> allChildObsIds, List<Integer> ids) {
        Map<String, List<Integer>> params = new HashMap<>();
        params.put("parentObsIds", ids);

        List<Map<String, Object>> results = jdbcTemplate.query(obsDetailSql, params, new ColumnMapRowMapper());
        List<Integer> obsGroupIds = new ArrayList<>();
        for (Map res : results) {
            if ((boolean) res.get("isSet")) {
                obsGroupIds.add((Integer) res.get("obsId"));
            } else {
                allChildObsIds.add((Integer) res.get("obsId"));
            }
        }
        if (!obsGroupIds.isEmpty())
            retrieveChildObsIds(allChildObsIds, obsGroupIds);
    }

    public void setForm(BahmniForm form) {
        this.form = form;
    }

    @PostConstruct
    public void postConstruct() {
        this.obsDetailSql = BatchUtils.convertResourceOutputToString(obsDetailSqlResource);
        this.leafObsSql = BatchUtils.convertResourceOutputToString(leafObsSqlResource);
        this.formObsSql = BatchUtils.convertResourceOutputToString(formObsSqlResource);
    }

    private void setObsIdAndParentObsId(List<Obs> childObs, Integer obsId, Integer parentObsId) {
        for (Obs child : childObs) {
            child.setParentId(parentObsId);
            child.setId(obsId);
        }
    }
}
