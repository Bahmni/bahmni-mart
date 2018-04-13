package org.bahmni.mart.helper;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.exception.InvalidOrderTypeException;
import org.bahmni.mart.exception.NoSamplesFoundException;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ObsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderConceptUtil {

    @Autowired
    private ObsService obsService;

    @Value("classpath:sql/orderTypes.sql")
    private Resource resource;

    @Autowired
    private NamedParameterJdbcTemplate openMRSJDBCTemplate;

    public int getOrderTypeId(String conceptName) throws NoSamplesFoundException, InvalidOrderTypeException {
        List<Integer> sampleConceptIds = obsService.getChildConcepts(conceptName).stream()
                .map(Concept::getId).collect(Collectors.toList());
        if (sampleConceptIds.isEmpty()) {
            throw new NoSamplesFoundException(String.format("No samples found for the orderable %s", conceptName));
        }
        return executeOrderTypesSql(sampleConceptIds, conceptName);
    }

    private int executeOrderTypesSql(List<Integer> sampleConceptIds, String conceptName)
            throws InvalidOrderTypeException {
        String sql = BatchUtils.convertResourceOutputToString(resource);
        MapSqlParameterSource conceptIds = new MapSqlParameterSource();
        conceptIds.addValue("sampleConceptIds", sampleConceptIds);

        List<Integer> orderTypeIds = openMRSJDBCTemplate
                .query(sql, conceptIds, new SingleColumnRowMapper<>(Integer.class));

        if (orderTypeIds.isEmpty()) {
            throw new InvalidOrderTypeException(String.format("Invalid order type %s", conceptName));
        }

        return orderTypeIds.get(0);
    }
}
