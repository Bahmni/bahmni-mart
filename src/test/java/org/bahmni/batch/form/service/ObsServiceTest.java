package org.bahmni.batch.form.service;

import org.bahmni.batch.BatchUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;


@PrepareForTest({BatchUtils.class, ObsService.class})
@RunWith(PowerMockRunner.class)
public class ObsServiceTest {

    private ObsService obsService;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(BatchUtils.class);
        obsService = new ObsService();
        ClassPathResource conceptDetailsResource = Mockito.mock(ClassPathResource.class);
        ClassPathResource conceptListResource = Mockito.mock(ClassPathResource.class);
        PowerMockito.when(BatchUtils.convertResourceOutputToString(conceptDetailsResource)).thenReturn("conceptDetailsSQL");
        PowerMockito.when(BatchUtils.convertResourceOutputToString(conceptListResource)).thenReturn("conceptListSQL");
        setValuesForMemberFields(obsService, "jdbcTemplate", namedParameterJdbcTemplate);
        setValuesForMemberFields(obsService, "conceptDetailsSqlResource", conceptDetailsResource);
        setValuesForMemberFields(obsService, "conceptListSqlResource", conceptListResource);

        obsService.postConstruct();
    }

    @Test
    public void shouldGetConceptsByNames() throws Exception {
        String commaSeparatedConceptNames = "Video, Image, Radiology Documents";
        List<String> conceptNamesList = Arrays.asList("Video", "Image", "Radiology Documents");
        MapSqlParameterSource mapSqlParameterSource = Mockito.mock(MapSqlParameterSource.class);
        PowerMockito.whenNew(MapSqlParameterSource.class).withNoArguments().thenReturn(mapSqlParameterSource);
        PowerMockito.when(BatchUtils.convertConceptNamesToSet(commaSeparatedConceptNames)).thenReturn(conceptNamesList);

        obsService.getConceptsByNames(commaSeparatedConceptNames);

        Mockito.verify(mapSqlParameterSource, Mockito.times(1)).addValue("conceptNames", conceptNamesList);
        Mockito.verify(namedParameterJdbcTemplate, Mockito.times(1)).query(eq("conceptDetailsSQL"), eq(mapSqlParameterSource), any(BeanPropertyRowMapper.class));
    }

    @Test
    public void shouldGetChildConcepts() throws Exception {
        String parentVideoConcept = "Patient Videos";
        MapSqlParameterSource mapSqlParameterSource = Mockito.mock(MapSqlParameterSource.class);
        PowerMockito.whenNew(MapSqlParameterSource.class).withNoArguments().thenReturn(mapSqlParameterSource);

        obsService.getChildConcepts(parentVideoConcept);

        Mockito.verify(mapSqlParameterSource, Mockito.times(1)).addValue("parentConceptName", parentVideoConcept);
        Mockito.verify(namedParameterJdbcTemplate, Mockito.times(1)).query(eq("conceptListSQL"), eq(mapSqlParameterSource), any(BeanPropertyRowMapper.class));
    }

    private void setValuesForMemberFields(Object observationService, String fieldName, Object valueForMemberField) throws NoSuchFieldException, IllegalAccessException {
        Field f1 = observationService.getClass().getDeclaredField(fieldName);
        f1.setAccessible(true);
        f1.set(observationService, valueForMemberField);
    }
}