package org.bahmni.mart.helper;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.config.job.JobDefinitionReader;
import org.bahmni.mart.config.job.model.JobDefinition;
import org.bahmni.mart.exception.InvalidOrderTypeException;
import org.bahmni.mart.exception.NoSamplesFoundException;
import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.service.ConceptService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collections;

import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest(BatchUtils.class)
public class OrderConceptUtilTest {

    private OrderConceptUtil orderConceptUtil;

    private String conceptName = "Lab Samples";

    @Rule
    private ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ConceptService conceptService;

    @Mock
    private Concept concept;

    @Mock
    private NamedParameterJdbcTemplate openMRSJDBCTemplate;

    @Mock
    private JobDefinitionReader jobDefinitionReader;

    @Mock
    private JobDefinition jobDefinition;

    private String locale;

    @Before
    public void setUp() throws Exception {
        orderConceptUtil = new OrderConceptUtil();
        locale = "locale";
        setValuesForMemberFields(orderConceptUtil, "conceptService", conceptService);
        setValuesForMemberFields(orderConceptUtil, "openMRSJDBCTemplate", openMRSJDBCTemplate);
        setValuesForMemberFields(orderConceptUtil, "jobDefinitionReader", jobDefinitionReader);
        mockStatic(BatchUtils.class);
        when(concept.getId()).thenReturn(1);
        when(BatchUtils.convertResourceOutputToString(any(Resource.class))).thenReturn("sql");
        when(jobDefinition.getLocale()).thenReturn(locale);
        when(jobDefinitionReader.getJobDefinitionByName(anyString())).thenReturn(jobDefinition);
    }

    @Test
    public void shouldThrowNoOrderablesFoundExceptionWhenThereAreNoSamplesForAGivenConcept()
            throws InvalidOrderTypeException, NoSamplesFoundException {
        when(conceptService.getChildConcepts(conceptName, locale)).thenReturn(Collections.emptyList());
        expectedException.expect(NoSamplesFoundException.class);
        expectedException.expectMessage("No samples found for the orderable Lab Samples");

        orderConceptUtil.getOrderTypeId(conceptName);

        verify(conceptService, times(1)).getChildConcepts(conceptName, locale);
        verify(jobDefinitionReader).getJobDefinitionByName(anyString());
        verify(jobDefinition).getLocale();
    }

    @Test
    public void shouldThrowInvalidOrderTypeExceptionWhenThereIsNoOrderTypeForAGivenConcept()
            throws InvalidOrderTypeException, NoSamplesFoundException {
        when(conceptService.getChildConcepts(conceptName, locale)).thenReturn(Collections.singletonList(concept));
        when(openMRSJDBCTemplate
                .query(eq("sql"), any(MapSqlParameterSource.class), any(BeanPropertyRowMapper.class)))
                .thenReturn(Collections.emptyList());

        expectedException.expect(InvalidOrderTypeException.class);
        expectedException.expectMessage("Invalid order type Lab Samples");

        orderConceptUtil.getOrderTypeId(conceptName);

        verify(conceptService, times(1)).getChildConcepts(conceptName, locale);
        verify(concept, times(1)).getId();
        verifyStatic();
        BatchUtils.convertResourceOutputToString(any(Resource.class));
        verify(openMRSJDBCTemplate, times(1))
                .query(eq("sql"), any(MapSqlParameterSource.class), any(BeanPropertyRowMapper.class));
        verify(jobDefinitionReader).getJobDefinitionByName(anyString());
        verify(jobDefinition).getLocale();
    }

    @Test
    public void shouldReturnOrderTypeIdForAnOrderable() throws InvalidOrderTypeException, NoSamplesFoundException {

        when(conceptService.getChildConcepts(conceptName, locale)).thenReturn(Collections.singletonList(concept));
        int expectedOrderTypeId = 50;
        when(openMRSJDBCTemplate
                .query(eq("sql"), any(MapSqlParameterSource.class), any(BeanPropertyRowMapper.class)))
                .thenReturn(Collections.singletonList(expectedOrderTypeId));

        int actualOrderTypeId = orderConceptUtil.getOrderTypeId(conceptName);

        verify(conceptService, times(1)).getChildConcepts(conceptName, locale);
        verify(concept, times(1)).getId();
        verifyStatic();
        BatchUtils.convertResourceOutputToString(any(Resource.class));
        verify(openMRSJDBCTemplate, times(1))
                .query(eq("sql"), any(MapSqlParameterSource.class), any(BeanPropertyRowMapper.class));

        Assert.assertEquals(expectedOrderTypeId, actualOrderTypeId);
        verify(jobDefinitionReader).getJobDefinitionByName(anyString());
        verify(jobDefinition).getLocale();
    }

}
