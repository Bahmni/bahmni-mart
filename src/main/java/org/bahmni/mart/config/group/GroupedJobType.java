package org.bahmni.mart.config.group;

import java.util.Arrays;

public enum GroupedJobType {
    PROGRAMS;

    public static boolean contains(String groupedJobType) {
        return Arrays.stream(GroupedJobType.values())
                .anyMatch(jobType -> jobType.toString().toLowerCase().equals(groupedJobType.toLowerCase()));
    }
}
