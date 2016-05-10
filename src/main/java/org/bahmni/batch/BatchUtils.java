package org.bahmni.batch;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

public class BatchUtils {

	public static String convertResourceOutputToString(Resource resource){
		try(InputStream is = resource.getInputStream()) {
			return IOUtils.toString(is);
		}
		catch (IOException e) {
			throw new RuntimeException("Cannot load the provided resource. Unable to continue");
		}
	}

}
