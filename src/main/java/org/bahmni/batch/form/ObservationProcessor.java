package org.bahmni.batch.form;

import org.bahmni.batch.BatchUtils;
import org.bahmni.batch.form.domain.BahmniForm;
import org.bahmni.batch.form.domain.Concept;
import org.bahmni.batch.form.domain.Obs;
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
@Scope(value="prototype")
public class ObservationProcessor implements ItemProcessor<Map<String,Object>, List<Obs>> {

	private String obsDetailSql;

	private String leafObsSql;

	private BahmniForm form;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	@Value("classpath:sql/obsDetail.sql")
	private Resource obsDetailSqlResource;

	@Value("classpath:sql/leafObs.sql")
	private Resource leafObsSqlResource;

	@Autowired
	private FormFieldTransformer formFieldTransformer;

	@Override
	public List<Obs> process(Map<String,Object> obsRow) throws Exception {
        List<Integer> fieldIds = formFieldTransformer.transformFormToFieldIds(form);
		List<Integer> allChildObsIds = new ArrayList<>();

		if (form.getFormName().getIsSet() == 1) {
			retrieveChildObsIds(allChildObsIds, Arrays.asList((Integer)obsRow.get("obs_id")));
		} else {
			allChildObsIds.add((Integer) obsRow.get("obs_id"));
		}
		if(allChildObsIds.size() > 0 && fieldIds.size() > 0) {
            List<Obs> obsRows = fetchAllLeafObs(allChildObsIds, fieldIds);
            setObsIdAndParentObsId(obsRows, (Integer) obsRow.get("obs_id"), (Integer) obsRow.get("parent_obs_id"));
			return obsRows;
		} else {
			return new ArrayList<>();
		}
	}

	private List<Obs> fetchAllLeafObs(List<Integer> allChildObsGroupIds, List<Integer> fieldIds) {
		Map<String,List<Integer>> params = new HashMap<>();
		params.put("childObsIds",allChildObsGroupIds);
		params.put("leafConceptIds", fieldIds);
        return jdbcTemplate.query(leafObsSql, params, new BeanPropertyRowMapper<Obs>(Obs.class) {
            @Override
            public Obs mapRow(ResultSet resultSet, int i) throws SQLException {
                Obs obs = super.mapRow(resultSet, i);
                Concept concept = new Concept(resultSet.getInt("conceptId"), resultSet.getString("conceptName"), 0, "");
                obs.setField(concept);
                return obs;
            }
        });
    }

	protected void retrieveChildObsIds(List<Integer> allChildObsIds, List<Integer> ids){

		Map<String,List<Integer>> params = new HashMap<>();
		params.put("parentObsIds",ids);

		List<Map<String, Object>> results = jdbcTemplate.query(obsDetailSql,params,new ColumnMapRowMapper());
		List<Integer> obsGroupIds = new ArrayList<>();
		for(Map res : results){
			if((boolean)res.get("isSet"))
				obsGroupIds.add((Integer) res.get("obsId"));
			else{
				allChildObsIds.add((Integer) res.get("obsId"));
			}
		}
		if (!obsGroupIds.isEmpty()){
			retrieveChildObsIds(allChildObsIds, obsGroupIds);
		}

	}

	public void setForm(BahmniForm form) {
		this.form = form;
	}

	public void setJdbcTemplate(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setObsDetailSqlResource(Resource obsDetailSqlResource) {
		this.obsDetailSqlResource = obsDetailSqlResource;
	}

	public void setLeafObsSqlResource(Resource leafObsSqlResource) {
		this.leafObsSqlResource = leafObsSqlResource;
	}

	public void setFormFieldTransformer(FormFieldTransformer formFieldTransformer) {
		this.formFieldTransformer = formFieldTransformer;
	}

	@PostConstruct
	public void postConstruct(){
		this.obsDetailSql = BatchUtils.convertResourceOutputToString(obsDetailSqlResource);
		this.leafObsSql = BatchUtils.convertResourceOutputToString(leafObsSqlResource);
	}

	public void setObsIdAndParentObsId(List<Obs> childObs, Integer obsId,Integer parentObsId) {
		for(Obs child: childObs){
			child.setParentId(parentObsId);
			child.setId(obsId);
		}
	}
}
