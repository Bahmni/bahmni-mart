package org.bahmni.mart.form.service;

import org.bahmni.mart.BatchUtils;
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

import java.util.Arrays;
import java.util.List;

import static org.bahmni.mart.BatchUtils.convertResourceOutputToString;
import static org.bahmni.mart.CommonTestHelper.setValuesForMemberFields;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;


@PrepareForTest({BatchUtils.class, ObsService.class})
@RunWith(PowerMockRunner.class)
public class ObsServiceTest {

    private ObsService obsService;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private MapSqlParameterSource mapSqlParameterSource;

    @Before
    public void setUp() throws Exception {
        mockStatic(BatchUtils.class);
        obsService = new ObsService();
        ClassPathResource conceptDetailsResource = mock(ClassPathResource.class);
        ClassPathResource conceptListResource = mock(ClassPathResource.class);
        ClassPathResource freeTextConceptSqlResource = mock(ClassPathResource.class);

        when(convertResourceOutputToString(conceptDetailsResource)).thenReturn("conceptDetailsSQL");
        when(convertResourceOutputToString(conceptListResource)).thenReturn("conceptListSQL");
        when(convertResourceOutputToString(freeTextConceptSqlResource)).thenReturn("freeTextConceptSql");
        whenNew(MapSqlParameterSource.class).withNoArguments().thenReturn(mapSqlParameterSource);

        setValuesForMemberFields(obsService, "jdbcTemplate", namedParameterJdbcTemplate);
        setValuesForMemberFields(obsService, "conceptDetailsSqlResource", conceptDetailsResource);
        setValuesForMemberFields(obsService, "conceptListSqlResource", conceptListResource);
        setValuesForMemberFields(obsService, "freeTextConceptSqlResource", freeTextConceptSqlResource);

        obsService.postConstruct();
    }

    @Test
    public void shouldGetConceptsByNames() {
        List<String> conceptNamesList = Arrays.asList("Video", "Image", "Radiology Documents");

        obsService.getConceptsByNames(conceptNamesList);

        verify(mapSqlParameterSource, times(1)).addValue("conceptNames", conceptNamesList);
        verify(namedParameterJdbcTemplate, times(1))
                .query(eq("conceptDetailsSQL"), eq(mapSqlParameterSource), any(BeanPropertyRowMapper.class));
    }

    @Test
    public void shouldGetChildConcepts() {
        String parentVideoConcept = "Patient Videos";

        obsService.getChildConcepts(parentVideoConcept);

        verify(mapSqlParameterSource, times(1)).addValue("parentConceptName", parentVideoConcept);
        verify(namedParameterJdbcTemplate, times(1))
                .query(eq("conceptListSQL"), eq(mapSqlParameterSource), any(BeanPropertyRowMapper.class));
    }

    @Test
    public void shouldGetAllFreeTextConcepts() {
        obsService.getFreeTextConcepts();

        verify(namedParameterJdbcTemplate, times(1))
                .query(eq("freeTextConceptSql"), eq(mapSqlParameterSource), any(BeanPropertyRowMapper.class));

    }
}