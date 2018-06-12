package org.bahmni.mart.form;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinition;
import org.bahmni.mart.form.domain.BahmniForm;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractObservationProcessor implements ItemProcessor<Map<String, Object>, List<Obs>> {

    private String obsDetailSql;

    private String leafObsSql;

    private String formObsSql;

    protected BahmniForm form;

    private JobDefinition jobDefinition;

    @Value("classpath:sql/obsDetail.sql")
    private Resource obsDetailSqlResource;

    @Value("classpath:sql/leafObs.sql")
    private Resource leafObsSqlResource;

    @Value("classpath:sql/formObs.sql")
    private Resource formObsSqlResource;

    @Autowired
    private FormFieldTransformer formFieldTransformer;

    @Autowired
    @Qualifier("openmrsNamedJdbcTemplate")
    protected NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public abstract List<Obs> process(Map<String, Object> obsRow);

    protected List<Obs> getFormObs(Map<String, Object> obsRow) {

        List<Obs> formObs = formObs((Integer) obsRow.get("obs_id"), (Integer) obsRow.get("parent_obs_id"));
        return formObs.stream().filter(obs -> obs.getField().getIsSet() == 1).collect(Collectors.toList());
    }

    private List<Obs> formObs(Integer obsId, Integer parentObsId) {
        Map<String, Object> params = new HashMap<>();
        params.put("obsId", obsId);
        params.put("parentObsId", parentObsId);
        params.put("conceptReferenceSource", jobDefinition.getConceptReferenceSource());
        return getObs(params, formObsSql);
    }

    protected List<Obs> getObs(Map<String, ?> params, String queryString) {
        return jdbcTemplate.query(queryString, params, new BeanPropertyRowMapper<Obs>(Obs.class) {
            @Override
            public Obs mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
                Obs obs = super.mapRow(resultSet, rowNumber);
                Concept concept = new Concept(resultSet.getInt("conceptId"), resultSet.getString("conceptName"),
                        resultSet.getInt("isSet"), "");
                obs.setParentName(resultSet.getString("parentConceptName"));
                obs.setField(concept);
                return obs;
            }
        });
    }

    protected List<Obs> fetchAllLeafObs(List<Integer> allChildObsGroupIds, Integer parentObsId) {
        List<Integer> leafConcepts = formFieldTransformer.transformFormToFieldIds(form);

        if (allChildObsGroupIds.size() > 0 && leafConcepts.size() > 0) {
            Map<String, Object> params = new HashMap<>();
            params.put("childObsIds", allChildObsGroupIds);
            params.put("leafConceptIds", leafConcepts);
            params.put("parentObsId", parentObsId);
            params.put("conceptReferenceSource", jobDefinition.getConceptReferenceSource());
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

    protected void setObsIdAndParentObsId(List<Obs> childObs, Integer obsId, Integer parentObsId) {
        for (Obs child : childObs) {
            child.setParentId(parentObsId);
            child.setId(obsId);
        }
    }

    public void setJobDefinition(JobDefinition jobDefinition) {
        this.jobDefinition = jobDefinition;
    }
}
