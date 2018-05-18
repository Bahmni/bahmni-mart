package org.bahmni.mart.form.service;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.form.domain.Concept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ConceptService {

    @Value("classpath:sql/conceptDetails.sql")
    private Resource conceptDetailsSqlResource;

    @Value("classpath:sql/conceptList.sql")
    private Resource conceptListSqlResource;

    @Value("classpath:sql/freeTextConceptList.sql")
    private Resource freeTextConceptSqlResource;

    private String conceptDetailsSql;

    private String conceptListSql;

    private String freeTextConceptSql;

    @Autowired
    @Qualifier("openmrsNamedJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Concept> getConceptsByNames(List<String> conceptNames) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("conceptNames", conceptNames);
        return getConcepts(conceptDetailsSql, parameters);
    }

    public List<Concept> getChildConcepts(String parentConceptName) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("parentConceptName", parentConceptName);
        return getConcepts(conceptListSql, parameters);
    }

    @PostConstruct
    public void postConstruct() {
        this.conceptDetailsSql = BatchUtils.convertResourceOutputToString(conceptDetailsSqlResource);
        this.conceptListSql = BatchUtils.convertResourceOutputToString(conceptListSqlResource);
        this.freeTextConceptSql = BatchUtils.convertResourceOutputToString(freeTextConceptSqlResource);
    }

    public List<Concept> getFreeTextConcepts() {
        return getConcepts(freeTextConceptSql, new MapSqlParameterSource());
    }

    private List<Concept> getConcepts(String sql, MapSqlParameterSource parameters) {
        return jdbcTemplate.query(sql, parameters, new BeanPropertyRowMapper<>(Concept.class));
    }

    /**
     * Immediate parent concept of childConcept. If the rootConcept has more occurrences of
     * childConcept(enclosed in a concept set) then it returns the parent which found first.
     * It returns null if rootConcept doesn't have childConcept or rootConcept and childConcept concepts are same
     * @param rootConcept root concept of the child concept
     * @param childConcept concept for which immediate parent needs to be found
     * @return @{@link Concept}, null
     */
    public Concept getImmediateParentOfChildFromRootConcept(Concept rootConcept, Concept childConcept) {
        if (rootConcept == childConcept) {
            return null;
        }
        List<Concept> childConcepts = getChildConcepts(rootConcept.getName());
        for (Concept concept : childConcepts) {
            if (concept.getName().equals(childConcept.getName()))
                return rootConcept;
            Concept immediateParentOfChildFromRoot = getImmediateParentOfChildFromRootConcept(concept, childConcept);
            if (immediateParentOfChildFromRoot != null)
                return immediateParentOfChildFromRoot;
        }
        return null;
    }

}
