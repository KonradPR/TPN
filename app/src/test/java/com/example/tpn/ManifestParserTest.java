package com.example.tpn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;


import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;



public class ManifestParserTest {

    @Test
    public void testEmptyLabels() throws JSONException {
        JSONObject j = new JSONObject();
        assertFalse(ManifestParser.validateLabels(j));
    }

    @Test
    public void testLabelsWithOneLanguageIncorrect() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("labels",new String[]{"l1","l2"});
        assertFalse(ManifestParser.validateLabels(j));
    }

    @Test
    public void testLabelsWithOneLanguageCorrect() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("labels",new String[]{"l1","l2"});
        j.put("use_latin",false);
        assertTrue(ManifestParser.validateLabels(j));
    }

    @Test
    public void testLabelsWithTwoLanguagesLackingLatinLabels() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("labels",new JSONArray("[\"l1\",\"l2\"]"));
        j.put("use_latin",true);
        assertFalse(ManifestParser.validateLabels(j));
    }

    @Test
    public void testLabelsWithTwoLanguagesNotEqualNumberOFLabels() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("labels",new JSONArray("[\"l1\",\"l2\"]"));
        j.put("use_latin",true);
        j.put("latin_labels",new JSONArray("[\"l1\"]"));
        assertFalse(ManifestParser.validateLabels(j));
    }

    @Test
    public void testLabelsWithTwoLanguages() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("labels",new JSONArray("[\"l1\",\"l2\"]"));
        j.put("use_latin",true);
        j.put("latin_labels",new JSONArray("[\"l1\",\"l2\"]"));
        assertTrue(ManifestParser.validateLabels(j));
    }

    @Test
    public void testEmptyInput() throws JSONException {
        JSONObject j = new JSONObject();
        assertFalse(ManifestParser.validateInput(j));
    }

    @Test
    public void testInputWrongType() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("input_type","NONINT");
        j.put("input_size", new JSONArray("[200,200]"));
        assertFalse(ManifestParser.validateInput(j));
    }

    @Test
    public void testInputWrongSizeArrayLength() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("input_type","UINT8");
        j.put("input_size", new JSONArray("[200,200,300]"));
        assertFalse(ManifestParser.validateInput(j));
    }

    @Test
    public void testInputUint8() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("input_type","UINT8");
        j.put("input_size", new JSONArray("[200,200]"));
        assertTrue(ManifestParser.validateInput(j));
    }

    @Test
    public void testInputFloat32() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("input_type","FLOAT32");
        j.put("input_size", new JSONArray("[200,200]"));
        assertTrue(ManifestParser.validateInput(j));
    }

    @Test
    public void testEmptyOutput() throws JSONException {
        JSONObject j = new JSONObject();
        assertFalse(ManifestParser.validateOutput(j));
    }

    @Test
    public void testOutputWrongType() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("labels",new JSONArray("[\"l1\",\"l2\"]"));
        j.put("output_type","NONINT");
        j.put("output_size", new JSONArray("[200,200]"));
        assertFalse(ManifestParser.validateOutput(j));
    }

    @Test
    public void testOutputWrongArrayLength() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("labels",new JSONArray("[\"l1\",\"l2\"]"));
        j.put("output_type","FLOAT32");
        j.put("output_size", new JSONArray("[1,2,3]"));
        assertFalse(ManifestParser.validateOutput(j));
    }

    @Test
    public void testOutputWrongArrayValues() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("labels",new JSONArray("[\"l1\",\"l2\"]"));
        j.put("output_type","FLOAT32");
        j.put("output_size", new JSONArray("[1,3]"));
        assertFalse(ManifestParser.validateOutput(j));
    }

    @Test
    public void testOutputCorrect() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("labels",new JSONArray("[\"l1\",\"l2\"]"));
        j.put("output_type","FLOAT32");
        j.put("output_size", new JSONArray("[1,2]"));
        assertTrue(ManifestParser.validateOutput(j));
    }

    @Test
    public void testEmptyPhotos() throws JSONException {
        JSONObject j = new JSONObject();
        assertFalse(ManifestParser.validatePhotos(j));
    }

    @Test
    public void testPhotosNoSource() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("use_photos",true);
        assertFalse(ManifestParser.validatePhotos(j));
    }

    @Test
    public void testPhotosAssets() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("use_photos",true);
        j.put("photos_source","assets");
        j.put("output_size",new JSONArray("[1,285]"));
        assertTrue(ManifestParser.validatePhotos(j));
    }

    @Test
    public void testPhotosInternet() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("use_photos",true);
        j.put("photos_source","internet");
        j.put("labels",new JSONArray("[\"l1\",\"l2\"]"));
        j.put("photo_urls",new JSONArray("[\"l1\",\"l2\"]"));
        assertTrue(ManifestParser.validatePhotos(j));
    }

    @Test
    public void testPhotosUseNoPhotos() throws JSONException {
        JSONObject j = new JSONObject();
        j.put("use_photos",false);
        assertTrue(ManifestParser.validatePhotos(j));
    }

}
