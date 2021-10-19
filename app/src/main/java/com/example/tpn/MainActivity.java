package com.example.tpn;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;


import com.example.tpn.databinding.ActivityMainBinding;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import android.Manifest.permission.*;
import android.Manifest.permission_group.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
//import android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private  int PERMISSIONS_REQUEST_CODE = 0;
    private  int FILE_PICKER_REQUEST_CODE = 1;
    private  int FILE_PICKER_REQUEST_CODE_JPG = 2;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public String modelName;
    ArrayList<Model> models = new ArrayList<>();
    private int indexOfCurrentModel = 0;
    private Bitmap currentBitmap = null;
    private ArrayAdapter<String> adapter;
    private ListView list;
    private ArrayList<String> arrayList;

    public void pick(){
        new MaterialFilePicker()
                .withActivity(this)
                .withCloseMenu(true)
                .withFilter(Pattern.compile(".*\\.(tflite)$"))
                .withFilterDirectories(false)
                .withTitle("Choose File to Import")
                .withRequestCode(FILE_PICKER_REQUEST_CODE)
                .start();
    }


    public void getPhotoFromGallery(){
        new MaterialFilePicker()
                .withActivity(this)
                .withCloseMenu(true)
                .withFilter(Pattern.compile(".*\\.(jpg)$"))
                .withFilterDirectories(false)
                .withTitle("Choose File to Import")
                .withRequestCode(FILE_PICKER_REQUEST_CODE_JPG)
                .start();
    }


    public void makePrediction(){
        TensorBuffer probabilityBuffer = TensorBuffer.createFixedSize(new int[]{1, 1001}, DataType.FLOAT32);
        float[][] buf = new float[1][285];
        ByteBuffer buffer = TensorImage.fromBitmap(currentBitmap).getBuffer();
        models.get(indexOfCurrentModel).getInterpreter().run(buffer,buf);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
           String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            try {
                models.add(new Model(loadLocalModelFile(path), modelName, ManifestParser.loadManifestFromStorage("placeholder") ));
                System.out.println(models.size());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            System.out.println(path);
        }else if(requestCode == FILE_PICKER_REQUEST_CODE_JPG && resultCode == Activity.RESULT_OK){
            String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(path),224,224,false);
            currentBitmap = bitmap;
            Navigation.findNavController(this,R.id.nav_host_fragment_content_main).navigate(R.id.action_to_ResultFragment);
            makePrediction();
        }
    }

    public void setUpSelection(View view){

        arrayList = new ArrayList<String>();
        for(Model model: models){
            arrayList.add(model.getModelName());
        }

        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);
        ((ListView)view).setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        try {
            models.add(new Model(loadModelFromAssets("converted_model_2.tflite"),"Default", ManifestParser.loadManifestFromAssets(getAssets().openFd("manifest.json"))));
            models.add(new Model(loadModelFromAssets("model.tflite"),"Fast",ManifestParser.loadManifestFromAssets(getAssets().openFd("manifest.json"))));
        } catch (Exception e) {
            System.out.println("HERE");
            e.printStackTrace();
        }

    }

    private MappedByteBuffer loadModelFromAssets(String fileName) throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(fileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private MappedByteBuffer loadLocalModelFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = null;

            fis = new FileInputStream(file);
            return fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void setCurrentBitmap(Bitmap currentBitmap) {
        this.currentBitmap = currentBitmap;
    }

}