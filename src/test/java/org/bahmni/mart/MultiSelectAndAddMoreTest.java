package org.bahmni.mart;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.collections.keyvalue.DefaultMapEntry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.stream.Collector;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;


;

@PrepareForTest({ MultiSelectAndAddMore.class, Stream.class, BatchUtils.class, JsonParser.class, JsonObject.class })
@RunWith(PowerMockRunner.class)
public class MultiSelectAndAddMoreTest {

    @Mock
    private FileReader fileReader;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private JsonElement jsonElement;

    @Mock
    private JsonObject jsonObject;

    @Mock
    private Stream stream;

    @Mock
    private BatchUtils batchUtils;

    @Mock
    private Collector collector;

    @Test
    public void shouldReturnListOfMultiSelectAndAddMore()
            throws Exception {
        MultiSelectAndAddMore multiSelectAndAddMore = new MultiSelectAndAddMore();
        List<String> expected = Arrays
                .asList("FSTG, Specialty determined by MLO",
                        "OR, Operation performed",
                        "Video");
        List<String> ignoreConceptsSet = Arrays.asList("video", "audio");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore, "ignoreConcepts", "video, audio");

        Set<Map.Entry<String, JsonElement>> conceptSetUI = new HashSet();
        JsonObject allObsElement = new JsonObject();
        allObsElement.addProperty("showPanelView", false);
        JsonObject videoElement = new JsonObject();
        videoElement.addProperty("allowAddMore", true);
        JsonObject specialtyElement = new JsonObject();
        specialtyElement.addProperty("multiSelect",true);
        JsonObject operationElement = new JsonObject();
        operationElement.addProperty("autocomplete",true);
        operationElement.addProperty("codedConceptName","OR, Operation performed coded");
        operationElement.addProperty("nonCodedConceptName",
                "OR, Operation performed non coded");
        operationElement.addProperty("allowAddMore",true);

        Map.Entry<String, JsonElement> allObs = new DefaultMapEntry("All Observation Templates", allObsElement);
        Map.Entry<String, JsonElement> video = new DefaultMapEntry("Video", videoElement);
        Map.Entry<String, JsonElement> specialty = new DefaultMapEntry(
                "FSTG, Specialty determined by MLO",
                specialtyElement);
        Map.Entry<String, JsonElement> operation = new DefaultMapEntry("OR, Operation performed", operationElement);

        conceptSetUI.add(allObs);
        conceptSetUI.add(specialty);
        conceptSetUI.add(video);
        conceptSetUI.add(operation);

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonElement);
        when(jsonElement.getAsJsonObject()).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("config")).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("conceptSetUI")).thenReturn(jsonObject);
        when(jsonObject.entrySet()).thenReturn(conceptSetUI);
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet("video, audio")).thenReturn(ignoreConceptsSet);

        List<String> multiSelectAndAddMoreConceptNames = multiSelectAndAddMore.getConceptNames();

        Assert.assertEquals(3, multiSelectAndAddMoreConceptNames.size());
        Assert.assertThat(multiSelectAndAddMoreConceptNames, containsInAnyOrder(expected.toArray()));
    }


    @Test
    public void shouldReturnListOfAddMores()
            throws Exception {
        MultiSelectAndAddMore multiSelectAndAddMore = new MultiSelectAndAddMore();
        List<String> expected = Arrays
                .asList("FSTG, Specialty determined by MLO");
        List<String> ignoreConceptsSet = Arrays.asList("video", "audio");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore, "ignoreConcepts", "video, audio");

        Set<Map.Entry<String, JsonElement>> conceptSetUI = new HashSet();
        JsonObject specialtyElement = new JsonObject();
        specialtyElement.addProperty("allowAddMore",true);
        specialtyElement.addProperty("multiSelect",false);

        Map.Entry<String, JsonElement> specialty = new DefaultMapEntry(
                "FSTG, Specialty determined by MLO",
                specialtyElement);

        conceptSetUI.add(specialty);

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonElement);
        when(jsonElement.getAsJsonObject()).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("config")).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("conceptSetUI")).thenReturn(jsonObject);
        when(jsonObject.entrySet()).thenReturn(conceptSetUI);
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet("video, audio")).thenReturn(ignoreConceptsSet);

        List<String> addMoreConceptNames = multiSelectAndAddMore.getConceptNames();

        Assert.assertEquals(1, addMoreConceptNames.size());
        Assert.assertThat(addMoreConceptNames, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void shouldReturnListOfMultiSelects()
            throws Exception {
        MultiSelectAndAddMore multiSelectAndAddMore = new MultiSelectAndAddMore();
        List<String> expected = Arrays
                .asList("FSTG, Specialty determined by MLO");
        List<String> ignoreConceptsSet = Arrays.asList("video", "audio");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore, "ignoreConcepts", "video, audio");

        Set<Map.Entry<String, JsonElement>> conceptSetUI = new HashSet();
        JsonObject specialtyElement = new JsonObject();
        specialtyElement.addProperty("multiSelect",true);
        specialtyElement.addProperty("allowAddMore",false);

        Map.Entry<String, JsonElement> specialty = new DefaultMapEntry(
                "FSTG, Specialty determined by MLO",
                specialtyElement);

        conceptSetUI.add(specialty);

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonElement);
        when(jsonElement.getAsJsonObject()).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("config")).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("conceptSetUI")).thenReturn(jsonObject);
        when(jsonObject.entrySet()).thenReturn(conceptSetUI);
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet("video, audio")).thenReturn(ignoreConceptsSet);

        List<String> multiSelectConceptNames = multiSelectAndAddMore.getConceptNames();

        Assert.assertEquals(1, multiSelectConceptNames.size());
        Assert.assertThat(multiSelectConceptNames, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void shouldReturnEmptyWhenAddMoreIsTrueAndItISInIgnoreConcept()
            throws Exception {
        MultiSelectAndAddMore multiSelectAndAddMore = new MultiSelectAndAddMore();
        List<String> expected = Arrays
                .asList("FSTG, Specialty determined by MLO");
        List<String> ignoreConceptsSet = Arrays.asList("FSTG, Specialty determined by MLO");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore, "ignoreConcepts", "video, audio");

        Set<Map.Entry<String, JsonElement>> conceptSetUI = new HashSet();
        JsonObject specialtyElement = new JsonObject();
        specialtyElement.addProperty("multiSelect",false);
        specialtyElement.addProperty("allowAddMore",true);

        Map.Entry<String, JsonElement> specialty = new DefaultMapEntry(
                "FSTG, Specialty determined by MLO",
                specialtyElement);

        conceptSetUI.add(specialty);

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonElement);
        when(jsonElement.getAsJsonObject()).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("config")).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("conceptSetUI")).thenReturn(jsonObject);
        when(jsonObject.entrySet()).thenReturn(conceptSetUI);
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet("video, audio")).thenReturn(ignoreConceptsSet);

        List<String> multiSelectConceptNames = multiSelectAndAddMore.getConceptNames();

        Assert.assertEquals(0, multiSelectConceptNames.size());
    }

    @Test
    public void shouldReturnEmptyWhenAddMoreAndMultiSelectIsTrueAndItISInIgnoreConcept()
            throws Exception {
        MultiSelectAndAddMore multiSelectAndAddMore = new MultiSelectAndAddMore();
        List<String> expected = Arrays
                .asList("FSTG, Specialty determined by MLO");
        List<String> ignoreConceptsSet = Arrays.asList("FSTG, Specialty determined by MLO");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore, "ignoreConcepts", "video, audio");

        Set<Map.Entry<String, JsonElement>> conceptSetUI = new HashSet();
        JsonObject specialtyElement = new JsonObject();
        specialtyElement.addProperty("multiSelect",true);
        specialtyElement.addProperty("allowAddMore",true);

        Map.Entry<String, JsonElement> specialty = new DefaultMapEntry(
                "FSTG, Specialty determined by MLO",
                specialtyElement);

        conceptSetUI.add(specialty);

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonElement);
        when(jsonElement.getAsJsonObject()).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("config")).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("conceptSetUI")).thenReturn(jsonObject);
        when(jsonObject.entrySet()).thenReturn(conceptSetUI);
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet("video, audio")).thenReturn(ignoreConceptsSet);

        List<String> multiSelectConceptNames = multiSelectAndAddMore.getConceptNames();

        Assert.assertEquals(0, multiSelectConceptNames.size());
    }

    @Test
    public void shouldReturnEmptyWhenNotAddMoreAndMultiSelectIsTrueAndItISInIgnoreConcept()
            throws Exception {
        MultiSelectAndAddMore multiSelectAndAddMore = new MultiSelectAndAddMore();
        List<String> expected = Arrays
                .asList("FSTG, Specialty determined by MLO");
        List<String> ignoreConceptsSet = Arrays.asList("FSTG, Specialty determined by MLO");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore, "ignoreConcepts", "video, audio");

        Set<Map.Entry<String, JsonElement>> conceptSetUI = new HashSet();
        JsonObject specialtyElement = new JsonObject();
        specialtyElement.addProperty("multiSelect",true);
        specialtyElement.addProperty("allowAddMore",false);

        Map.Entry<String, JsonElement> specialty = new DefaultMapEntry(
                "FSTG, Specialty determined by MLO",
                specialtyElement);

        conceptSetUI.add(specialty);

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonElement);
        when(jsonElement.getAsJsonObject()).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("config")).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("conceptSetUI")).thenReturn(jsonObject);
        when(jsonObject.entrySet()).thenReturn(conceptSetUI);
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet("video, audio")).thenReturn(ignoreConceptsSet);

        List<String> multiSelectConceptNames = multiSelectAndAddMore.getConceptNames();

        Assert.assertEquals(0, multiSelectConceptNames.size());
    }

    @Test
    public void shouldReturnEmptyWhenNotAddMoreAndNotMultiSelectIsTrueAndItISInIgnoreConcept()
            throws Exception {
        MultiSelectAndAddMore multiSelectAndAddMore = new MultiSelectAndAddMore();
        List<String> expected = Arrays
                .asList("FSTG, Specialty determined by MLO");
        List<String> ignoreConceptsSet = Arrays.asList("video");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore, "ignoreConcepts", "video, audio");

        Set<Map.Entry<String, JsonElement>> conceptSetUI = new HashSet();
        JsonObject specialtyElement = new JsonObject();
        specialtyElement.addProperty("multiSelect",false);
        specialtyElement.addProperty("allowAddMore",false);

        Map.Entry<String, JsonElement> specialty = new DefaultMapEntry(
                "FSTG, Specialty determined by MLO",
                specialtyElement);

        conceptSetUI.add(specialty);

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonElement);
        when(jsonElement.getAsJsonObject()).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("config")).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("conceptSetUI")).thenReturn(jsonObject);
        when(jsonObject.entrySet()).thenReturn(conceptSetUI);
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet("video, audio")).thenReturn(ignoreConceptsSet);

        List<String> multiSelectConceptNames = multiSelectAndAddMore.getConceptNames();

        Assert.assertEquals(0, multiSelectConceptNames.size());
    }

    @Test
    public void shouldReturnListWhenAddMoreAndMultiSelectIsTrueAndNotInInIgnoreConcept()
            throws Exception {
        MultiSelectAndAddMore multiSelectAndAddMore = new MultiSelectAndAddMore();
        List<String> expected = Arrays
                .asList("FSTG, Specialty determined by MLO");
        List<String> ignoreConceptsSet = Arrays.asList("video");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore, "ignoreConcepts", "video, audio");

        Set<Map.Entry<String, JsonElement>> conceptSetUI = new HashSet();
        JsonObject specialtyElement = new JsonObject();
        specialtyElement.addProperty("multiSelect",true);
        specialtyElement.addProperty("allowAddMore",true);

        Map.Entry<String, JsonElement> specialty = new DefaultMapEntry(
                "FSTG, Specialty determined by MLO",
                specialtyElement);

        conceptSetUI.add(specialty);

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonElement);
        when(jsonElement.getAsJsonObject()).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("config")).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("conceptSetUI")).thenReturn(jsonObject);
        when(jsonObject.entrySet()).thenReturn(conceptSetUI);
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet("video, audio")).thenReturn(ignoreConceptsSet);

        List<String> multiSelectConceptNames = multiSelectAndAddMore.getConceptNames();

        Assert.assertEquals(1, multiSelectConceptNames.size());
    }

    @Test
    public void shouldReturnListWhenNotAddMoreAndNotMultiSelectIsTrueAndNotInInIgnoreConcept()
            throws Exception {
        MultiSelectAndAddMore multiSelectAndAddMore = new MultiSelectAndAddMore();
        List<String> expected = Arrays
                .asList("FSTG, Specialty determined by MLO");
        List<String> ignoreConceptsSet = Arrays.asList("FSTG, Specialty determined by MLO");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore, "ignoreConcepts", "video, audio");

        Set<Map.Entry<String, JsonElement>> conceptSetUI = new HashSet();
        JsonObject specialtyElement = new JsonObject();
        specialtyElement.addProperty("multiSelect",false);
        specialtyElement.addProperty("allowAddMore",false);

        Map.Entry<String, JsonElement> specialty = new DefaultMapEntry(
                "FSTG, Specialty determined by MLO",
                specialtyElement);

        conceptSetUI.add(specialty);

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonElement);
        when(jsonElement.getAsJsonObject()).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("config")).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("conceptSetUI")).thenReturn(jsonObject);
        when(jsonObject.entrySet()).thenReturn(conceptSetUI);
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet("video, audio")).thenReturn(ignoreConceptsSet);

        List<String> multiSelectConceptNames = multiSelectAndAddMore.getConceptNames();

        Assert.assertEquals(0, multiSelectConceptNames.size());
    }

    @Test
    public void shouldReturnEmptyListWhenFileContainsNothing()
            throws Exception {
        MultiSelectAndAddMore multiSelectAndAddMore = new MultiSelectAndAddMore();
        List<String> expected = Arrays
                .asList("FSTG, Specialty determined by MLO");
        List<String> ignoreConceptsSet = Arrays.asList("FSTG, Specialty determined by MLO");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore, "ignoreConcepts", "video, audio");

        Set<Map.Entry<String, JsonElement>> conceptSetUI = new HashSet();

        whenNew(FileReader.class).withArguments("conf/app.json").thenReturn(fileReader);
        whenNew(FileReader.class).withArguments("conf/random/app.json").thenReturn(fileReader);
        whenNew(JsonParser.class).withNoArguments().thenReturn(jsonParser);
        when(jsonParser.parse(fileReader)).thenReturn(jsonElement);
        when(jsonElement.getAsJsonObject()).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("config")).thenReturn(jsonObject);
        when(jsonObject.getAsJsonObject("conceptSetUI")).thenReturn(jsonObject);
        when(jsonObject.entrySet()).thenReturn(conceptSetUI);
        mockStatic(BatchUtils.class);
        when(BatchUtils.convertConceptNamesToSet("video, audio")).thenReturn(ignoreConceptsSet);

        List<String> multiSelectConceptNames = multiSelectAndAddMore.getConceptNames();

        Assert.assertEquals(0, multiSelectConceptNames.size());
    }



    @Test
    public void shouldReturnListOfMultiSelectAndAddMoreDiscardingIgnoreConcepts()
            throws FileNotFoundException, NoSuchFieldException, IllegalAccessException {
        MultiSelectAndAddMore multiSelectAndAddMore = new MultiSelectAndAddMore();
        List<String> expected = Arrays.asList("FSTG, Specialty determined by MLO");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/random/app.json");
        CommonTestHelper.setValuesForMemberFields(multiSelectAndAddMore,
                "ignoreConcepts",
                "\"Video\", \"OR, Operation performed\",\"Radiology Documents\"");
        List<String> multiSelectAndAddMoreConceptNames = multiSelectAndAddMore.getConceptNames();

        Assert.assertEquals(1, multiSelectAndAddMoreConceptNames.size());
        Assert.assertThat(multiSelectAndAddMoreConceptNames, containsInAnyOrder(expected.toArray()));
    }

    @Test
    public void shouldReturnEmptyListWhenFileIsNotPresentInTheGivenPath()
            throws NoSuchFieldException, IllegalAccessException {
        MultiSelectAndAddMore configMultiSelectAndAddMore = new MultiSelectAndAddMore();
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "defaultMultiSelectAndAddMore",
                "conf/NoSuchFile.json");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "implementationMultiSelectAndAddMore",
                "conf/NoSuchFile.json");
        CommonTestHelper.setValuesForMemberFields(configMultiSelectAndAddMore,
                "ignoreConcepts",
                "\"Video\",\"Radiology Documents\"");
        List<String> multiSelectAndAddMore = configMultiSelectAndAddMore.getConceptNames();
        Assert.assertEquals(0, multiSelectAndAddMore.size());
    }

    @Test
    public void shouldReturnUniqueListOfMultiSelectAndAddMore()
            throws NoSuchFieldException, IllegalAccessException {
        MultiSelectAndAddMore configMultiSelectAndAddMore = new MultiSelectAndAddMore();
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
        List<String> multiSelectAndAddMore = configMultiSelectAndAddMore.getConceptNames();
        Assert.assertEquals(2, multiSelectAndAddMore.size());
        Assert.assertThat(multiSelectAndAddMore, containsInAnyOrder(expected.toArray()));
    }
}