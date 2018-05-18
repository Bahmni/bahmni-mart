package org.bahmni.mart.form.service;

import org.bahmni.mart.BatchUtils;
import org.bahmni.mart.form.domain.Concept;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@PrepareForTest({BatchUtils.class, ConceptService.class})
@RunWith(PowerMockRunner.class)
public class ConceptServiceTest {

    private ConceptService conceptService;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private MapSqlParameterSource mapSqlParameterSource;

    @Before
    public void setUp() throws Exception {
        mockStatic(BatchUtils.class);
        conceptService = new ConceptService();
        ClassPathResource conceptDetailsResource = mock(ClassPathResource.class);
        ClassPathResource conceptListResource = mock(ClassPathResource.class);
        ClassPathResource freeTextConceptSqlResource = mock(ClassPathResource.class);

        when(convertResourceOutputToString(conceptDetailsResource)).thenReturn("conceptDetailsSQL");
        when(convertResourceOutputToString(conceptListResource)).thenReturn("conceptListSQL");
        when(convertResourceOutputToString(freeTextConceptSqlResource)).thenReturn("freeTextConceptSql");
        whenNew(MapSqlParameterSource.class).withNoArguments().thenReturn(mapSqlParameterSource);

        setValuesForMemberFields(conceptService, "jdbcTemplate", namedParameterJdbcTemplate);
        setValuesForMemberFields(conceptService, "conceptDetailsSqlResource", conceptDetailsResource);
        setValuesForMemberFields(conceptService, "conceptListSqlResource", conceptListResource);
        setValuesForMemberFields(conceptService, "freeTextConceptSqlResource", freeTextConceptSqlResource);

        conceptService.postConstruct();
    }

    @Test
    public void shouldGetConceptsByNames() {
        List<String> conceptNamesList = Arrays.asList("Video", "Image", "Radiology Documents");

        conceptService.getConceptsByNames(conceptNamesList);

        verify(mapSqlParameterSource, times(1)).addValue("conceptNames", conceptNamesList);
        verify(namedParameterJdbcTemplate, times(1))
                .query(eq("conceptDetailsSQL"), eq(mapSqlParameterSource), any(BeanPropertyRowMapper.class));
    }

    @Test
    public void shouldGetChildConcepts() {
        String parentVideoConcept = "Patient Videos";

        conceptService.getChildConcepts(parentVideoConcept);

        verify(mapSqlParameterSource, times(1)).addValue("parentConceptName", parentVideoConcept);
        verify(namedParameterJdbcTemplate, times(1))
                .query(eq("conceptListSQL"), eq(mapSqlParameterSource), any(BeanPropertyRowMapper.class));
    }

    @Test
    public void shouldGetAllFreeTextConcepts() {
        conceptService.getFreeTextConcepts();

        verify(namedParameterJdbcTemplate, times(1))
                .query(eq("freeTextConceptSql"), eq(mapSqlParameterSource), any(BeanPropertyRowMapper.class));

    }

    @Test
    public void shouldReturnNullAsImmediateParentIfParentAndChildConceptsAreSame() {
        Concept rootConcept = mock(Concept.class);

        Concept immediateParentOfChildFromRootConcept = conceptService
                .getImmediateParentOfChildFromRootConcept(rootConcept, rootConcept);

        assertNull(immediateParentOfChildFromRootConcept);

    }

    @Test
    public void shouldReturnRootConceptIfItIsImmediateParentOfChildConcept() {

        Concept rootConcept = mock(Concept.class);
        when(rootConcept.getName()).thenReturn("root concept name");

        Concept childConcept = mock(Concept.class);
        when(childConcept.getName()).thenReturn("child concept name");

        when(namedParameterJdbcTemplate.query(anyString(), any(SqlParameterSource.class),
                any(BeanPropertyRowMapper.class))).thenReturn(Arrays.asList(childConcept));

        Concept immediateParentOfChildFromRootConcept = conceptService
                .getImmediateParentOfChildFromRootConcept(rootConcept, childConcept);

        Assert.assertEquals(rootConcept, immediateParentOfChildFromRootConcept);
    }

    @Test
    public void shouldReturnParentConceptIfItIsImmediateParentOfChildConcept() {

        Concept rootConcept = mock(Concept.class);
        when(rootConcept.getName()).thenReturn("root concept name");

        Concept parentConcept = mock(Concept.class);
        when(parentConcept.getName()).thenReturn("parent concept name");

        Concept childConcept = mock(Concept.class);
        when(childConcept.getName()).thenReturn("child concept name");

        Concept targetChildConcept = mock(Concept.class);
        when(targetChildConcept.getName()).thenReturn("target child concept name");

        when(namedParameterJdbcTemplate.query(anyString(), any(SqlParameterSource.class),
                any(BeanPropertyRowMapper.class))).thenReturn(Arrays.asList(parentConcept))
                .thenReturn(Arrays.asList(childConcept, targetChildConcept))
                .thenReturn(Collections.EMPTY_LIST);

        Concept immediateParentOfChildFromRootConcept = conceptService
                .getImmediateParentOfChildFromRootConcept(rootConcept, childConcept);

        Assert.assertEquals(parentConcept, immediateParentOfChildFromRootConcept);
    }
}