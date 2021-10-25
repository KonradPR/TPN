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

    public String getModelName(){
        return modelName;
    }

    public Interpreter getInterpreter(){
        return interpreter;
    }

    public String getLabel(int i) {
        try {
            return manifest.getJSONArray("labels").getString(i);
        }catch (JSONException e){
            return "";
        }
    }

    public String getLatinLabel(int i){
        try {
            return manifest.getJSONArray("latin_labels").getString(i);
        }catch (JSONException e){
            return "";
        }
    }


}
