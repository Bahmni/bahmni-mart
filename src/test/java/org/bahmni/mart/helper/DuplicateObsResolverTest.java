package org.bahmni.mart.helper;

import org.bahmni.mart.form.domain.Concept;
import org.bahmni.mart.form.domain.Obs;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DuplicateObsResolverTest {

    Obs obs = new Obs();
    Obs obs1 = new Obs();
    Obs obs2 = new Obs();

    @Before
    public void setUp() {

        Concept concept = new Concept();
        concept.setName("concept");
        obs.setField(concept);

        Concept concept1 = new Concept();
        concept1.setName("concept1");
        obs1.setField(concept1);

        Concept concept2 = new Concept();
        concept2.setName("concept1");
        obs2.setField(concept2);
    }

    @Test
    public void shouldReturnObsWithDuplicateConceptNamesAsSeparateList() {

        List<Obs> obsList = new ArrayList<>();
        obsList.add(obs);
        obsList.add(obs1);
        obsList.add(obs2);

        List<List<Obs>> items = Collections.singletonList(obsList);

        List<List<Obs>> uniqueObsItems = DuplicateObsResolver.getUniqueObsItems(items);

        assertEquals(2, uniqueObsItems.size());
        assertEquals(2, uniqueObsItems.get(0).size());
        assertThat(Arrays.asList(obs, obs1), containsInAnyOrder(uniqueObsItems.get(0).toArray()));

        assertEquals(1, uniqueObsItems.get(1).size());
        assertThat(Collections.singletonList(obs2), containsInAnyOrder(uniqueObsItems.get(1).toArray()));

    }

    @Test
    public void shouldReturnSameObsAsListWhenThereAreNoObsWithDuplicateConceptNames() {
        List<Obs> obsList = new ArrayList<>();
        obsList.add(obs);
        obsList.add(obs1);

        List<List<Obs>> items = Collections.singletonList(obsList);

        List<List<Obs>> uniqueObsItems = DuplicateObsResolver.getUniqueObsItems(items);

        assertEquals(1, uniqueObsItems.size());
        assertEquals(2, uniqueObsItems.get(0).size());
        assertThat(Arrays.asList(obs, obs1), containsInAnyOrder(uniqueObsItems.get(0).toArray()));
    }

    @Test
    public void shouldReturnAListWithAnItemAsAnEmptyListWhenThereAreNoObsPassed() {

        List<List<Obs>> items = Collections.singletonList(new ArrayList<>());

        List<List<Obs>> uniqueObsItems = DuplicateObsResolver.getUniqueObsItems(items);

        assertEquals(1, uniqueObsItems.size());
        assertEquals(0, uniqueObsItems.get(0).size());

    }

    @Test
    public void shouldReturnAnEmptyListWhenNullIsPassed() {

        List<List<Obs>> uniqueObsItems = DuplicateObsResolver.getUniqueObsItems(null);

        assertEquals(0, uniqueObsItems.size());

    }
}