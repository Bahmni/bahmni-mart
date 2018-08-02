package org.bahmni.mart.exports.model;

import java.util.Objects;

public class EventInfo {
    private String eventRecordId;
    private String category;
    private Integer maxEventRecordId;

    public EventInfo(String eventRecordId, String category, Integer maxEventRecordId) {
        this.eventRecordId = eventRecordId;
        this.category = category;
        this.maxEventRecordId = maxEventRecordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventInfo that = (EventInfo) o;
        return Objects.equals(eventRecordId, that.eventRecordId) &&
                Objects.equals(category, that.category) &&
                Objects.equals(maxEventRecordId, that.maxEventRecordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventRecordId, category, maxEventRecordId);
    }

    public String getCategory() {
        return category;
    }

    public String getEventRecordId() {
        return eventRecordId;
    }

    public Integer getMaxEventRecordId() {
        return maxEventRecordId;
    }
}
