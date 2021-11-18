package com.example.tpn;


import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;


import java.nio.MappedByteBuffer;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ModelTest {

    MappedByteBuffer model = null;

    @Test
    public void testGetModelName() throws JSONException {
        JSONObject j = new JSONObject();
        Model m1 = new Model(model,"m1", new JSONObject());
        assertEquals("","m1", m1.getModelName());
    }

    @Test(expected = ManifestException.class)
    public void testGetLabelsError() throws JSONException, ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject());
        m1.getLabel(0);
    }

    @Test
    public void testGetLabels() throws JSONException, ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject("{labels:[\"l1\",\"l2\"]}"));
        assertEquals(m1.getLabel(0),"l1");
        assertEquals(m1.getLabel(1),"l2");
    }

    @Test(expected = ManifestException.class)
    public void testGetLatinLabelsError() throws JSONException, ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject());
        m1.getLatinLabel(0);
    }

    @Test
    public void testGetLatinLabels() throws JSONException, ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject("{latin_labels:[\"l1\",\"l2\"]}"));
        assertEquals(m1.getLatinLabel(0),"l1");
        assertEquals(m1.getLatinLabel(1),"l2");
    }

    @Test
    public void testGetFirstDim() throws JSONException, ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject("{input_size:[224,224]}"));
        assertEquals(m1.getFirstDim(),224);
    }

    @Test(expected = ManifestException.class)
    public void testGetFirstDimError() throws JSONException, ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject());
        m1.getFirstDim();
    }

    @Test
    public void testGetSecondDim() throws JSONException, ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject("{input_size:[224,224]}"));
        assertEquals(m1.getSecondDim(),224);
    }

    @Test(expected = ManifestException.class)
    public void testGetSecondDimError() throws JSONException, ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject());
        m1.getSecondDim();
    }

    @Test(expected = ManifestException.class)
    public void testUsePhotosError() throws ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject());
        m1.usePhotos();
    }

    @Test
    public void testUsePhotos() throws ManifestException, JSONException {
        Model m1 = new Model(model,"m1", new JSONObject("{use_photos:true}"));
        assertTrue(m1.usePhotos());
    }

    @Test(expected = ManifestException.class)
    public void testUseLatinError() throws ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject());
        m1.useLatin();
    }

    @Test
    public void testUseLatin() throws ManifestException, JSONException {
        Model m1 = new Model(model,"m1", new JSONObject("{use_latin:true}"));
        assertTrue(m1.useLatin());
    }

    @Test(expected = ManifestException.class)
    public void testPhotosSourceError() throws ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject());
        m1.photosSource();
    }

    @Test
    public void testPhotosSource() throws ManifestException, JSONException {
        Model m1 = new Model(model,"m1", new JSONObject("{photos_source:\"assets\"}"));
        assertEquals("assets",m1.photosSource());
    }

    @Test(expected = ManifestException.class)
    public void testInputTypeError() throws ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject());
        m1.inputType();
    }

    @Test
    public void testInputType() throws ManifestException, JSONException {
        Model m1 = new Model(model,"m1", new JSONObject("{input_type:\"UINT8\"}"));
        assertEquals("UINT8",m1.inputType());
    }

    @Test(expected = ManifestException.class)
    public void testOutputTypeError() throws ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject());
        m1.outputType();
    }

    @Test
    public void testOutputType() throws ManifestException, JSONException {
        Model m1 = new Model(model,"m1", new JSONObject("{output_type:\"UINT8\"}"));
        assertEquals("UINT8",m1.outputType());
    }

    @Test(expected = ManifestException.class)
    public void testOutputSizeError() throws ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject());
        m1.outputSize();
    }

    @Test
    public void testOutputSize() throws ManifestException, JSONException {
        Model m1 = new Model(model,"m1", new JSONObject("{output_size:[1,400]}"));
        assertEquals(400,m1.outputSize());
    }

    @Test(expected = ManifestException.class)
    public void testPhotoUrlsError() throws ManifestException {
        Model m1 = new Model(model,"m1", new JSONObject());
        m1.getPhotoUrl(0);
    }

    @Test
    public void testPhotoUrls() throws ManifestException, JSONException {
        Model m1 = new Model(model,"m1", new JSONObject("{photo_urls:[\"u1\",\"u2\"]}"));
        assertEquals("u1",m1.getPhotoUrl(0));
        assertEquals("u2",m1.getPhotoUrl(1));
    }
}
