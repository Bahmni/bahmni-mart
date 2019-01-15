package org.bahmni.mart.form2.uitl;

import com.google.gson.Gson;
import org.bahmni.mart.form2.model.Form2JsonMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

@Component
public class Form2MetadataReader {

    private static final Logger logger = LoggerFactory.getLogger(Form2MetadataReader.class);

    public Form2JsonMetadata read(String jsonPath) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(jsonPath));
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return parse(bufferedReader);
    }

    private Form2JsonMetadata parse(BufferedReader metadataReader){
       try {
           Form2JsonMetadata form2JsonMetadata = new Gson().fromJson(metadataReader, Form2JsonMetadata.class);
           return form2JsonMetadata;
       }catch(Exception e){
           return null;
       }
    }
}
