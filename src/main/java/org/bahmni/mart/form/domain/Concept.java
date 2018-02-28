package org.bahmni.mart.form.domain;

public class Concept {
    private Integer id;
    private String name;
    private String dataType;
    private Integer isSet;
    private String title;
    private Concept parent;

    public Concept() {
    }

    public Concept(Integer id, String name, Integer isSet) {
        this.id = id;
        this.name = name;
        this.isSet = isSet;
    }

    public Concept(Integer id, String name, Integer isSet, String title) {
        this.id = id;
        this.name = name;
        this.isSet = isSet;
        this.title = title;
    }

    public Concept(Integer id, String name, Integer isSet, String title, Concept parent) {
        this.id = id;
        this.name = name;
        this.isSet = isSet;
        this.title = title;
        this.parent = parent;
    }

    public Concept getParent() {
        return parent;
    }

    public void setParent(Concept parent) {
        this.parent = parent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setIsSet(Integer isSet) {
        this.isSet = isSet;
    }

    public Integer getIsSet() {
        return isSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Concept concept = (Concept) o;

        if (id != null ? !id.equals(concept.id) : concept.id != null)
            return false;
        if (name != null ? !name.equals(concept.name) : concept.name != null)
            return false;
        return isSet != null ? isSet.equals(concept.isSet) : concept.isSet == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (isSet != null ? isSet.hashCode() : 0);
        return result;
    }
}
