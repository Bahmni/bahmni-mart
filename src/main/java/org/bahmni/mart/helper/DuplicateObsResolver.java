package org.bahmni.mart.helper;

import org.bahmni.mart.form.domain.Obs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DuplicateObsResolver {

    public static List<List<Obs>> getUniqueObsItems(List<? extends List<Obs>> items) {

        if (items == null) {
            return new ArrayList<>();
        }
        List<List<Obs>> uniqueObsItems = new ArrayList<>(items);
        for (List<Obs> item : items) {
            if (!containsUniqueObsWithConceptNames(item)) {
                uniqueObsItems.remove(item);
                separateDuplicateRows(uniqueObsItems, item);
            }
        }
        return uniqueObsItems;
    }

    private static void separateDuplicateRows(List<List<Obs>> items, List<Obs> item) {

        if (containsUniqueObsWithConceptNames(item)) {
            items.add(item);
            return;
        }
        List<Obs> nonDuplicates = getUniqueObs(item);
        items.add(nonDuplicates);
        separateDuplicateRows(items, getDuplicateObs(item, nonDuplicates));
    }

    private static List<Obs> getDuplicateObs(List<Obs> item, List<Obs> nonDuplicates) {
        item.removeAll(nonDuplicates);
        return item;
    }

    private static List<Obs> getUniqueObs(List<Obs> item) {
        Set<String> conceptNames = new HashSet<>();
        List<Obs> nonDuplicates = new ArrayList<>();

        for (Obs observation : item) {
            String name = observation.getField().getName();
            if (!conceptNames.contains(name)) {
                conceptNames.add(name);
                nonDuplicates.add(observation);
            }
        }
        return nonDuplicates;
    }

    private static boolean containsUniqueObsWithConceptNames(List<Obs> list) {
        return list.stream().map(obs -> obs.getField().getName()).allMatch(new HashSet<>()::add);
    }

}
