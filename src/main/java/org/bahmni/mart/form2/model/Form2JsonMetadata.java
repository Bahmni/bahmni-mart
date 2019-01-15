package org.bahmni.mart.form2.model;

import java.util.List;

public class Form2JsonMetadata {
    List<Control> controls;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    public List<Control> getControls() {
        return controls;
    }

    public void setControls(List<Control> controls) {
        this.controls = controls;
    }


}

