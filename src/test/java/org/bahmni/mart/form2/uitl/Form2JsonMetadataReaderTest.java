package org.bahmni.mart.form2.uitl;

import org.bahmni.mart.form2.model.Form2Control;
import org.bahmni.mart.form2.model.Form2JsonMetadata;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

public class Form2JsonMetadataReaderTest {

    @Test
    public void shouldReturnMetadataWithEmptyConceptsAndPropertiesWhenFormDoesNotContainAnyObs() {
        String test = "{\"name\":\"EmptyForm\",\"uuid\":\"13aa7134-e44d-410f-84af-a67db9ec77d3\",\"defaultLocale\":" +
                "\"en\",\"controls\":[{\"type\":\"section\",\"label\":{\"translationKey\":\"SECTION_1\",\"type\":\"" +
                "label\",\"value\":\"Section\",\"id\":\"1\"},\"properties\":{\"addMore\":true,\"location\":{\"column" +
                "\":0,\"row\":0}},\"id\":\"1\",\"unsupportedProperties\":[],\"controls\":[]}],\"events\":{}," +
                "\"translationsUrl\":\"/openmrs/ws/rest/v1/bahmniie/form/translations\"}";
        Reader inputString = new StringReader(test);
        BufferedReader bufferedReader = new BufferedReader(inputString);
        List<Form2Control> form2JsonMetadata = new Form2MetadataReader()
                .parse(bufferedReader);


        System.out.print("");
    }
}
