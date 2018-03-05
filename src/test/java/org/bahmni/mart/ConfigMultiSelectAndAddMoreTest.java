package org.bahmni.mart;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

@PrepareForTest({ConfigMultiSelectAndAddMore.class})
@RunWith(PowerMockRunner.class)
public class ConfigMultiSelectAndAddMoreTest {

    @Test
    public void shouldReturnListOfMultiSelectAndAddMore()
            throws FileNotFoundException, NoSuchFieldException, IllegalAccessException {
        ConfigMultiSelectAndAddMore configMultiSelectAndAddMore = new ConfigMultiSelectAndAddMore();
        List<String> expected = Arrays
                .asList("FSTG, Specialty determined by MLO",
                        "OR, Operation performed",
                        "Video");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        List<String> multiSelectAndAddMore = configMultiSelectAndAddMore.getMultiSelectAndAddMore();

        Assert.assertEquals(3, multiSelectAndAddMore.size());
        Assert.assertThat(multiSelectAndAddMore, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void shouldReturnListOfMultiSelectAndAddMoreDiscardingIgnoreConcepts()
            throws FileNotFoundException, NoSuchFieldException, IllegalAccessException {
        ConfigMultiSelectAndAddMore configMultiSelectAndAddMore = new ConfigMultiSelectAndAddMore();
        List<String> expected = Arrays.asList("FSTG, Specialty determined by MLO");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "ignoreConcepts",
                "\"Video\", \"OR, Operation performed\",\"Radiology Documents\"");
        List<String> multiSelectAndAddMore = configMultiSelectAndAddMore.getMultiSelectAndAddMore();

        Assert.assertEquals(1, multiSelectAndAddMore.size());
        Assert.assertThat(multiSelectAndAddMore, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void shouldReturnEmptyListWhenFileIsNotPresentInTheGivenPath()
            throws NoSuchFieldException, IllegalAccessException {
        ConfigMultiSelectAndAddMore configMultiSelectAndAddMore = new ConfigMultiSelectAndAddMore();
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/NoSuchFile.json");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/NoSuchFile.json");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "ignoreConcepts",
                "\"Video\",\"Radiology Documents\"");
        List<String> multiSelectAndAddMore = configMultiSelectAndAddMore.getMultiSelectAndAddMore();
        Assert.assertEquals(0, multiSelectAndAddMore.size());
    }

    @Test
    public void shouldReturnUniqueListOfMultiSelectAndAddMore()
            throws NoSuchFieldException, IllegalAccessException {
        ConfigMultiSelectAndAddMore configMultiSelectAndAddMore = new ConfigMultiSelectAndAddMore();
        List<String> expected = Arrays.asList("FSTG, Specialty determined by MLO", "OR, Operation performed");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "ignoreConcepts",
                "\"Video\",\"Radiology Documents\"");
        List<String> multiSelectAndAddMore = configMultiSelectAndAddMore.getMultiSelectAndAddMore();
        Assert.assertEquals(2, multiSelectAndAddMore.size());
        Assert.assertThat(multiSelectAndAddMore, containsInAnyOrder(expected.toArray()));
    }
}