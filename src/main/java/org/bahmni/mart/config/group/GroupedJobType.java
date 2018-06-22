package org.bahmni.mart.config.group;

import java.util.Arrays;

/**
 * The value of GroupedJobType is used in grouped job type in bahmni-mart.json.
 * Eg: {
 *       "name": "Bed Management",
 *       "type": "bedManagement",
 *       "chunkSizeToRead": "500"
 *     }
 *
 *  When a new grouped job type is added(in bahmni-mart.json) add that type into this enum
 *  and put all grouped jobs in a file with the name as < type >.json in 'resources/groupedJobs' directory.
 *  Eg: bedManagement.json
 */
public enum GroupedJobType {
    PROGRAMS("programs"), PATIENTS("patients"), APPOINTMENTS("appointments"), LOCATION("location"),
    OPERATION_THEATER("operationTheater"), BED_MANAGEMENT("bedManagement"), PERSON("person"), PROVIDER("provider");

    private final String typeValue;

    GroupedJobType(String groupedJobType) {
        this.typeValue = groupedJobType;
    }

    public String getTypeValue() {
        return typeValue;
    }

    public static boolean contains(String groupedJobType) {
        return Arrays.stream(GroupedJobType.values())
                .anyMatch(jobType -> jobType.getTypeValue().toLowerCase().equals(groupedJobType.toLowerCase()));
    }
}
