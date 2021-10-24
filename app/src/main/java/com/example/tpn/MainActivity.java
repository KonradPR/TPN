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
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.viewpager.widget.ViewPager;


import com.example.tpn.databinding.ActivityMainBinding;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ListenableFuture;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import android.view.Menu;
import android.view.MenuItem;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import android.Manifest.permission.*;
import android.Manifest.permission_group.*;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import static android.content.res.AssetManager.ACCESS_BUFFER;
import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static androidx.camera.core.CameraX.getContext;
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
    private Executor executor = Executors.newSingleThreadExecutor();

    PreviewView mPreviewView;
    Button cameraCaptureButton;


    public void startCamera(PreviewView pview, Button cameraCaptureButton) {
        mPreviewView = pview;
        this.cameraCaptureButton = cameraCaptureButton;
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetResolution(new Size(214, 214))
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageAnalysis, imageCapture);

        cameraCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull  ImageProxy image) {
                        System.out.println("WWWWWWWWWWWWWWWW");
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        System.out.println("W1");
                        buffer.rewind();
                        System.out.println("W2");
                        byte[] bytes = new byte[ buffer.remaining()];
                        System.out.println("W3");
                        buffer.get(bytes);
                        System.out.println("W4");
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                        System.out.println("W5");
                        currentBitmap = Bitmap.createScaledBitmap(bitmap,224,224,false);
                        System.out.println("W6");
                        Navigation.findNavController(this,R.id.nav_host_fragment_content_main).navigate(R.id.action_to_ResultFragment);
                        super.onCaptureSuccess(image);
                        makePrediction();
                    }

                    @Override
                    public void onError(@NonNull  ImageCaptureException exception) {
                        System.out.println("EEEEEEEEEEEEEEEEEEEEE");
                        super.onError(exception);
                    }
                });
            }
        });
    }

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

    static int[] indexesOfTopElements(float[] orig, int nummax) {
        float[] copy = Arrays.copyOf(orig,orig.length);
        Arrays.sort(copy);
        float[] honey = Arrays.copyOfRange(copy,copy.length - nummax, copy.length);
        int[] result = new int[nummax];
        int resultPos = 0;
        for(int i = 0; i < orig.length; i++) {
            float onTrial = orig[i];
            int index = Arrays.binarySearch(honey,onTrial);
            if(index < 0) continue;
            result[resultPos++] = i;
        }
        return result;
    }


    public void makePrediction(){
        float[][] buf = new float[1][285];
        ByteBuffer buffer = TensorImage.fromBitmap(currentBitmap).getBuffer();
        models.get(indexOfCurrentModel).getInterpreter().run(buffer,buf);
        int[] top = indexesOfTopElements(buf[0],5);
        int[] images = new int[5];
        String[] labels = new String[5];
        for(int i=0;i<5;i++){
            int j = top[i];
            images[i] = getResources().getIdentifier("d"+(j+1), "drawable", getPackageName());
            labels[i] = models.get(indexOfCurrentModel).getLabel(j);
        }
        CustomSwipeAdapter.image_resource = images;
        CustomSwipeAdapter.labels = labels;


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


    private void copyToCacheFile(final String assetPath, final File cacheFile) throws IOException
    {
        final InputStream inputStream = getAssets().open(assetPath, ACCESS_BUFFER);
        try
        {
            final FileOutputStream fileOutputStream = new FileOutputStream(cacheFile, false);
            try
            {
                //using Guava IO lib to copy the streams, but could also do it manually
                ByteStreams.copy(inputStream, fileOutputStream);
            }
            finally
            {
                fileOutputStream.close();
            }
        }
        finally
        {
            inputStream.close();
        }
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
            String assetPath = "manifest.json";
            final File cacheFile = new File(getCacheDir(), assetPath);
            cacheFile.getParentFile().mkdirs();
            copyToCacheFile(assetPath, cacheFile);
            AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(ParcelFileDescriptor.open(cacheFile, MODE_READ_ONLY), 0, -1);//getAssets().openFd("file:///android_asset/manifest.json");
            System.out.println("WEEEEEEEEEE");
            String jsonTxt = IOUtils.toString(assetFileDescriptor.createInputStream(), StandardCharsets.UTF_8);
            models.add(new Model(loadModelFromAssets("converted_model_2.tflite"),"Default", ManifestParser.loadManifestFromString(jsonTxt)));
            models.add(new Model(loadModelFromAssets("model.tflite"),"Fast",ManifestParser.loadManifestFromString(jsonTxt)));
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