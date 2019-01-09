package org.bahmni.mart.form2.uitl;

import com.google.gson.Gson;
import org.bahmni.mart.form2.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
public class Form2MetadataReader {

    private static final Logger logger = LoggerFactory.getLogger(Form2MetadataReader.class);

    public BufferedReader read(String jsonPath) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(jsonPath));
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        return bufferedReader;
    }

    public List<Form2Control> parse(BufferedReader metadataReader){
        Form2JsonMetadata form2JsonMetadata = new Gson().fromJson(metadataReader, Form2JsonMetadata.class);
        List<Form2Control> form2Controls = filterConceptAndProperty(form2JsonMetadata);
        return form2Controls;
    }

    private List<Form2Control> filterConceptAndProperty(Form2JsonMetadata form2JsonMetadata) {
        List <Form2Control> form2ControlList = new ArrayList<>();
        form2JsonMetadata.getControls().forEach(control -> {
           getFormControl(control,form2ControlList);
        });
        return form2ControlList;
    }

    private void getFormControl(Control control, List<Form2Control> form2ControlList) {
        Concept concept = control.getConcept();
        if(concept != null) {
            Form2Control form2Control = new Form2Control();
            form2Control.setConcept(concept);
            ControlProperties properties =control.getProperties();
            if (properties != null) {
                form2Control.setProperties(properties);
            }
            form2ControlList.add(form2Control);
            List<Control> childControls = control.getControls();
            if(childControls != null){
                childControls.forEach(childControl -> {
                    getFormControl(childControl, form2ControlList);
                });
            }
        }
    }
}
