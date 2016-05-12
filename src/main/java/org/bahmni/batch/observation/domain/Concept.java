package org.bahmni.batch.observation.domain;

public class Concept {
	private Integer id;
	private String name;
	private Integer isSet;

	public Concept(){}

	public Concept(Integer id, String name, Integer isSet) {
		this.id = id;
		this.name = name;
		this.isSet = isSet;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
