package com.example.tpn;

import android.content.res.AssetFileDescriptor;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ManifestParser {

    static public boolean validateManifest(JSONObject manifest){


        return true;
    }

    static public JSONObject loadManifestFromStorage(String path) throws IOException, JSONException {
        File file = new File(path);
        if(file.exists()){
            InputStream is = new FileInputStream(file);
            String jsonTxt = IOUtils.toString(is, "UTF-8");
            JSONObject manifest = new JSONObject(jsonTxt);
            return manifest;
        }else{
            throw new FileNotFoundException();
        }
    };

    static public JSONObject loadManifestFromAssets(AssetFileDescriptor fileDescriptor) throws IOException, JSONException {
        InputStream is = new FileInputStream(fileDescriptor.getFileDescriptor());
        String jsonTxt = IOUtils.toString(is, "UTF-8");
        JSONObject manifest = new JSONObject(jsonTxt);
        return manifest;
    }
}
