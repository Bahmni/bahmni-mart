package org.bahmni.batch;

import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class BatchUtilsTest {

	@Test
	public void ensureThatTheCommaSeparatedConceptNamesAreConvertedToSet(){
		List<String> conceptNames = BatchUtils.convertConceptNamesToSet("\"a,b\",\"c\",\"d\"");

		assertEquals(3,conceptNames.size());
		assertTrue(conceptNames.contains("c"));
		assertTrue(conceptNames.contains("d"));
		assertTrue(conceptNames.contains("a,b"));
	}


	@Test
	public void ensureThatSetIsNotNullWhenConceptNamesIsEmpty(){
		List<String> conceptNames = BatchUtils.convertConceptNamesToSet("");
		assertNotNull(conceptNames);
		assertEquals(0,conceptNames.size());
	}

	@Test
	public void ensureThatSetIsNotNullWhenConceptNamesIsNull(){
		List<String> conceptNames = BatchUtils.convertConceptNamesToSet(null);
		assertNotNull(conceptNames);
		assertEquals(0,conceptNames.size());
	}
}
