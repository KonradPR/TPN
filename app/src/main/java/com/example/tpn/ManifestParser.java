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
        try {
            if (!manifest.has("labels")) {
                return false;
            }

            if (!manifest.has("latin_labels")) {
                return false;
            }

            if (manifest.getJSONArray("labels").length()!=manifest.getJSONArray("latin_labels").length()){
                return false;
            }

            if(!manifest.has("input_type")){
                return false;
            }

            if(!((manifest.getString("input_type").equals("UINT8"))||(manifest.getString("input_type").equals("FLOAT32")))){
                return false;
            }

            if(!manifest.has("input_size")){
                return false;
            }

            if(!(manifest.getJSONArray("input_size").length() ==2)){
                return false;
            }

            if(!manifest.has("output_size")){
                return false;
            }

            if(!(manifest.getJSONArray("output_size").length()==2)){
                return false;
            }

            if(manifest.getJSONArray("labels").length() != manifest.getJSONArray("output_size").getInt(1)){
                return false;
            }

            if(!manifest.has("output_type")){
                return false;
            }

            if(!(manifest.getString("output_type").equals("FLOAT32"))|| (manifest.getString("output_type").equals("UINT8"))){
                return false;
            }

            if(!manifest.has("use_photos")){
                return false;
            }

            if(manifest.getBoolean("use_photos")&&(!manifest.has("photos_source"))){
                return false;
            }

            if(manifest.getBoolean("use_photos")&&(manifest.getString("photos_source").equals("assets"))){
                if(manifest.getJSONArray("output_size").getInt(1) !=285){

                    return false;
                }
            }

            if(manifest.getBoolean("use_photos")&&(!manifest.getString("photos_source").equals("assets"))){
                 if(manifest.getString("photos_source").equals("internet")){
                    if(!(manifest.getJSONArray("photo_urls").length() == manifest.getJSONArray("labels").length())){
                        return false;
                    }
                }else{
                    return false;
                }
            }


        }catch (JSONException e){
            return false;
        }
        return true;
    }

    static public JSONObject loadManifestFromStorage(String path) throws ManifestException {
        try {
            File file = new File(path);
            InputStream is = new FileInputStream(file);
            String jsonTxt = IOUtils.toString(is, "UTF-8");
            JSONObject manifest = new JSONObject(jsonTxt);
            if(!ManifestParser.validateManifest(manifest)) throw new ManifestException("The manifest You are trying to load doesn't follow required format!");
            return manifest;
        }catch (Exception ex){
            throw new ManifestException("There was an error loading model's manifest");
        }
    }

    static public JSONObject loadManifestFromString(String jsonTxt) throws  ManifestException {
        try {
            JSONObject manifest = new JSONObject(jsonTxt);
            if(!ManifestParser.validateManifest(manifest))throw new ManifestException("The manifest You are trying to load doesn't follow required format!");
            return manifest;
        } catch (JSONException ex){
            throw new ManifestException("There was an error loading model's manifest");
        }
    }

}
