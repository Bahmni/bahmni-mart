package org.bahmni.batch.form.service;

import org.bahmni.batch.BatchUtils;
import org.bahmni.batch.form.domain.Concept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ObsService {

    @Value("classpath:sql/conceptDetails.sql")
    private Resource conceptDetailsSqlResource;

    @Value("classpath:sql/conceptList.sql")
    private Resource conceptListSqlResource;

    private String conceptDetailsSql;

    private String conceptListSql;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Concept> getConceptsByNames(String commaSeparatedConceptNames) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("conceptNames", BatchUtils.convertConceptNamesToSet(commaSeparatedConceptNames));

        return jdbcTemplate.query(conceptDetailsSql, parameters, new BeanPropertyRowMapper<>(Concept.class));
    }

    public List<Concept> getChildConcepts(String parentConceptName) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("parentConceptName", parentConceptName);

        return jdbcTemplate.query(conceptListSql, parameters, new BeanPropertyRowMapper<>(Concept.class));

    }

    @PostConstruct
    public void postConstruct() {
        this.conceptDetailsSql = BatchUtils.convertResourceOutputToString(conceptDetailsSqlResource);
        this.conceptListSql = BatchUtils.convertResourceOutputToString(conceptListSqlResource);
    }


}
