package org.bahmni.batch;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BatchUtils {

	public static String convertResourceOutputToString(Resource resource){
		try(InputStream is = resource.getInputStream()) {
			return IOUtils.toString(is);
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot load the provided resource. Unable to continue");
		}
	}

	public static List<String> convertConceptNamesToSet(String conceptNames){
		List<String> conceptNamesSet = new ArrayList<>();
		if(conceptNames ==null ||conceptNames.isEmpty())
			return conceptNamesSet;

		String[] tokens = conceptNames.split("\"(\\s*),(\\s*)\"");
		for(String token: tokens){
			conceptNamesSet.add(token.replaceAll("\"",""));
		}

		return conceptNamesSet;
	}

}
