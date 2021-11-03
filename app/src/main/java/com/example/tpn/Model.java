package com.example.tpn;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.nio.MappedByteBuffer;

public class Model {
    private Interpreter interpreter;
    private String modelName;
    private JSONObject manifest;

    public Model(MappedByteBuffer mappedByteBuffer, String modelName, JSONObject manifest) {
        this.modelName = modelName;
        interpreter = new Interpreter(mappedByteBuffer);
        this.manifest = manifest;
    }

    public String getModelName() {
        return modelName;
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public String getLabel(int i) throws ManifestException {
        try {
            return manifest.getJSONArray("labels").getString(i);
        } catch (JSONException e) {
            throw new ManifestException("There was an error getting a specific label from the model's manifest");
        }
    }

    public String getLatinLabel(int i) throws ManifestException {
        try {
            return manifest.getJSONArray("latin_labels").getString(i);
        } catch (JSONException e) {
            throw new ManifestException("There was an error getting a specific label from the model's manifest");
        }
    }

    public int getFirstDim() throws ManifestException {
        try {
            return manifest.getJSONArray("input_size").getInt(0);
        } catch (JSONException e) {
            throw new ManifestException("There was an error getting input dimension from manifest");
        }
    }

    public int getSecondDim() throws ManifestException {
        try {
            return manifest.getJSONArray("input_size").getInt(1);
        } catch (JSONException e) {
            throw new ManifestException("There was an error getting input dimension from manifest");
        }

    }

    public boolean usePhotos() throws ManifestException {
        try {
            return manifest.getBoolean("use_photos");
        } catch (JSONException e) {
            throw new ManifestException("There was an error getting photo information from manifest");
        }
    }

    public String photosSource() throws ManifestException {
        try {
            return manifest.getString("photos_source");
        } catch (JSONException ex) {
            throw new ManifestException("There was an error getting photo information from manifest");
        }
    }

    public String inputType() throws ManifestException {
        try {
            System.out.println(manifest.getString("input_type"));
            return manifest.getString("input_type");
        } catch (JSONException ex) {
            throw new ManifestException("There was an error getting input type from manifest");
        }
    }


    public int outputSize() throws ManifestException {
        try {
            return manifest.getJSONArray("output_size").getInt(1);
        } catch (JSONException ex) {
            throw new ManifestException("There was an error getting output size from manifest");
        }
    }

    public boolean useLatin() throws ManifestException {
        try {
            return manifest.getBoolean("use_latin");
        } catch (JSONException ex) {
            throw new ManifestException("There was an error establishing need to use latin labels");
        }
    }

    public String getPhotoUrl(int i) throws ManifestException {
        try {
            return manifest.getJSONArray("photo_urls").getString(i);
        } catch (JSONException ex) {
            throw new ManifestException("There was an error getting photo url for given index");
        }
    }
}
