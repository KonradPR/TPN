package com.example.tpn;

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
    }

    public String getModelName(){
        return modelName;
    }

    public Interpreter getInterpreter(){
        return interpreter;
    }


}
