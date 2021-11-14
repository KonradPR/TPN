package com.example.tpn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.tpn.databinding.ActivityMainBinding;
import com.google.common.io.ByteStreams;
import com.google.common.util.concurrent.ListenableFuture;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import android.view.Menu;
import android.view.MenuItem;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import static android.content.res.AssetManager.ACCESS_BUFFER;
import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;


public class MainActivity extends AppCompatActivity {
    private final int FILE_PICKER_REQUEST_CODE_JSON = 0;
    private final int FILE_PICKER_REQUEST_CODE_TFLITE = 1;
    private final int FILE_PICKER_REQUEST_CODE_JPG = 2;
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public String modelName;
    private ArrayList<Model> models = new ArrayList<>();
    private int indexOfCurrentModel = 0;
    private Bitmap currentBitmap = null;
    private ArrayAdapter<String> modelNamesAdapter;
    private ArrayList<String> modelNamesList;
    private Executor cameraExecutor = Executors.newSingleThreadExecutor();
    private JSONObject defaultManifest = null;
    private JSONObject manifestToLoad = null;
    private boolean useDefaultManifest = true;
    private View loadingView = null;

    PreviewView cameraPreviewView;
    Button cameraCaptureButton;


    public void startCamera(PreviewView pview, Button cameraCaptureButton) {

        cameraPreviewView = pview;
        this.cameraCaptureButton = cameraCaptureButton;
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    cameraProvider.unbindAll();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));

    }


    public void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

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
        preview.setSurfaceProvider(cameraPreviewView.createSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);

        cameraCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCapture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        buffer.rewind();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                        try {
                            currentBitmap = Bitmap.createScaledBitmap(bitmap, models.get(indexOfCurrentModel).getFirstDim(), models.get(indexOfCurrentModel).getSecondDim(), false);
                            toResult();
                        }catch (ManifestException ex){
                            ErrorFragment.setCurrentException(ex);
                            toError();
                        }
                        super.onCaptureSuccess(image);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        ErrorFragment.setCurrentException(exception);
                        toError();
                        super.onError(exception);
                    }
                });
            }
        });
    }

    public void loadModelFile() {
        new MaterialFilePicker()
                .withActivity(this)
                .withCloseMenu(true)
                .withFilter(Pattern.compile(".*\\.(tflite)$"))
                .withFilterDirectories(false)
                .withTitle("Choose File to Import")
                .withRequestCode(FILE_PICKER_REQUEST_CODE_TFLITE)
                .start();
    }

    public void loadManifestFile(View view) {
        loadingView = view;
        new MaterialFilePicker()
                .withActivity(this)
                .withCloseMenu(true)
                .withFilter(Pattern.compile(".*\\.(json)$"))
                .withFilterDirectories(false)
                .withTitle("Choose File to Import")
                .withRequestCode(FILE_PICKER_REQUEST_CODE_JSON)
                .start();
    }


    public void getPhotoFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, FILE_PICKER_REQUEST_CODE_JPG);
    }

    static private int[] getIndexesOfTopElements(float[] orig, int nummax) {
        float[] copy = Arrays.copyOf(orig, orig.length);
        Arrays.sort(copy);
        float[] honey = Arrays.copyOfRange(copy, copy.length - nummax, copy.length);
        int[] result = new int[nummax];
        int resultPos = 0;
        for (int i = 0; i < orig.length; i++) {
            float onTrial = orig[i];
            int index = Arrays.binarySearch(honey, onTrial);
            if (index < 0) continue;
            result[resultPos++] = i;
        }
        for (int i = 0; i < nummax - 1; i++)
            for (int j = 0; j < nummax - i - 1; j++) {
                if (orig[result[j]] < orig[result[j + 1]]) {
                    int temp = result[j];
                    result[j] = result[j + 1];
                    result[j + 1] = temp;
                }
            }
        return result;
    }


    public void toResult() {
        MainActivity act = this;

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Navigation.findNavController(act, R.id.nav_host_fragment_content_main).navigate(R.id.action_to_ResultFragment);
            }
        });
    }

    public void toError() {
        MainActivity act = this;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Navigation.findNavController(act, R.id.nav_host_fragment_content_main).navigate(R.id.action_to_ErrorFragment);
            }
        });
    }


    private Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void makePrediction() {
        try {
            Model model = models.get(indexOfCurrentModel);
            float[][] buf = new float[1][model.outputSize()];
            currentBitmap = Bitmap.createScaledBitmap(currentBitmap, model.getFirstDim(), model.getSecondDim(), false);
            if (model.inputType().equals("FLOAT32")) {
                TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                tensorImage.load(currentBitmap);
                model.getInterpreter().run(tensorImage.getBuffer(), buf);
            } else {
                ByteBuffer buffer = TensorImage.fromBitmap(currentBitmap).getBuffer();
                model.getInterpreter().run(buffer, buf);
            }

            int[] top = getIndexesOfTopElements(buf[0], 5);
            int[] images = new int[5];
            Drawable[] drawables = new Drawable[5];
            String[] labels = new String[5];
            Future<Drawable>[] futures = new Future[5];
            ExecutorService executor = Executors.newFixedThreadPool(5);
            for (int i = 0; i < 5; i++) {
                int j = top[i];
                if (model.usePhotos() && model.photosSource().equals("assets")) {
                    drawables[i] = getResources().getDrawable(getResources().getIdentifier("d" + (j + 1), "drawable", getPackageName()));
                } else if (model.usePhotos() && model.photosSource().equals("internet")) {
                    futures[i] = executor.submit(new Callable<Drawable>() {
                        @Override
                        public Drawable call() throws Exception {
                            Bitmap bitmap = getBitmapFromURL(model.getPhotoUrl(j));
                            Drawable d = new BitmapDrawable(getResources(), bitmap);
                            return d;
                        }
                    });
                } else {
                    drawables[i] = getResources().getDrawable(R.drawable.placeholder);
                    images[i] = R.drawable.placeholder;
                }

                if (model.useLatin()) {
                    labels[i] = StringUtils.capitalize(model.getLabel(j)) + "\n" +
                            StringUtils.capitalize(model.getLatinLabel(j))
                            + "\n" + "Probability: " + String.format("%.2f", (buf[0][j] * 100)) + "%";
                } else {
                    labels[i] = StringUtils.capitalize(model.getLabel(j))
                            + "\n" + "Probability: " + String.format("%.2f", (buf[0][j] * 100)) + "%";
                }

            }

            if (model.usePhotos() && model.photosSource().equals("internet")) {
                for (int i = 0; i < 5; i++) {
                    try {
                        Drawable d = futures[i].get(3, TimeUnit.SECONDS);
                        drawables[i] = d;
                    } catch (Exception e) {
                        e.printStackTrace();
                        drawables[i] = getResources().getDrawable(R.drawable.placeholder);
                    }
                }
            }
            CustomSwipeAdapter.labels = labels;
            CustomSwipeAdapter.drawables = drawables;
        } catch (ManifestException exception) {
            ErrorFragment.setCurrentException(exception);
            toError();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST_CODE_TFLITE && resultCode == Activity.RESULT_OK) {
            try {
                String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                if (manifestToLoad == null) {
                    models.add(new Model(loadLocalModelFile(path), modelName, defaultManifest));
                } else {
                    models.add(new Model(loadLocalModelFile(path), modelName, manifestToLoad));
                    manifestToLoad = null;
                }
            } catch (IOException e) {
                ErrorFragment.setCurrentException(new FileException("There was an error opening model file"));
                toError();
            }

        } else if (requestCode == FILE_PICKER_REQUEST_CODE_JPG && resultCode == Activity.RESULT_OK) {
            try {
                Uri selectedImage = data.getData();
                Bitmap bitmap = null;
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                currentBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false);
                toResult();
            } catch (IOException ex) {
                ErrorFragment.setCurrentException(new FileException("There was an error loading file from gallery"));
                toError();
            }

        } else if (requestCode == FILE_PICKER_REQUEST_CODE_JSON && resultCode == Activity.RESULT_OK) {
            String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            try {
                manifestToLoad = ManifestParser.loadManifestFromStorage(path);
                ((Button) ((ViewGroup) loadingView.getParent()).getChildAt(2)).setEnabled(true);
            } catch (ManifestException ex) {
                manifestToLoad = null;
                ErrorFragment.setCurrentException(ex);
                toError();
            }
        }
    }

    public void setUpSelection(View view) {

        modelNamesList = new ArrayList<String>();
        for (Model model : models) {
            modelNamesList.add(model.getModelName());
        }

        modelNamesAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.spiner_item, modelNamesList);
        ((ListView) view).setAdapter(modelNamesAdapter);
    }


    private void copyToCacheFile(final String assetPath, final File cacheFile) throws FileException {
        try {
            final InputStream inputStream = getAssets().open(assetPath, ACCESS_BUFFER);
            try {
                final FileOutputStream fileOutputStream = new FileOutputStream(cacheFile, false);
                try {
                    //using Guava IO lib to copy the streams, but could also do it manually
                    ByteStreams.copy(inputStream, fileOutputStream);
                } finally {
                    fileOutputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (Exception ex) {
            throw new FileException("There was an error copying file to cache");
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
            //String assetPath1 = "mtest.json";
            //final File cacheFile1 = new File(getCacheDir(), assetPath1);
            //cacheFile1.getParentFile().mkdirs();
            //copyToCacheFile(assetPath1, cacheFile1);
            //AssetFileDescriptor assetFileDescriptor1 = new AssetFileDescriptor(ParcelFileDescriptor.open(cacheFile1, MODE_READ_ONLY), 0, -1);//getAssets().openFd("file:///android_asset/manifest.json");
            //String jsonTxt1 = IOUtils.toString(assetFileDescriptor1.createInputStream(), StandardCharsets.UTF_8);
            //System.out.println(jsonTxt1);
            //models.add(new Model(loadModelFromAssets("test.tflite"),"test",ManifestParser.loadManifestFromString(jsonTxt1)));
            final File cacheFile = new File(getCacheDir(), assetPath);
            cacheFile.getParentFile().mkdirs();
            copyToCacheFile(assetPath, cacheFile);
            AssetFileDescriptor assetFileDescriptor = new AssetFileDescriptor(ParcelFileDescriptor.open(cacheFile, MODE_READ_ONLY), 0, -1);//getAssets().openFd("file:///android_asset/manifest.json");
            String jsonTxt = IOUtils.toString(assetFileDescriptor.createInputStream(), StandardCharsets.UTF_8);
            defaultManifest = ManifestParser.loadManifestFromString(jsonTxt);
            if (!ManifestParser.validateManifest(defaultManifest))
                throw new RuntimeException("Manifest is invalid!");
            models.add(new Model(loadModelFromAssets("model_mobilenetv2_regularized.tflite"), "Mobilenet", defaultManifest));
            models.add(new Model(loadModelFromAssets("model_pca_based.tflite"), "PCA", defaultManifest));
            models.add(new Model(loadModelFromAssets("model_squeeze_excite_resnet.tflite"), "Resnet", defaultManifest));
        } catch (FileException ex) {
            ErrorFragment.setCurrentException(ex);
            ErrorFragment.notifyCriticalError();
            toError();
        } catch (ManifestException ex) {
            ex.printStackTrace();
            ErrorFragment.setCurrentException(ex);
            ErrorFragment.notifyCriticalError();
            toError();
        } catch (Exception ex) {
            ErrorFragment.setCurrentException(new FileException("There was an error opening asset file"));
            ErrorFragment.notifyCriticalError();
            toError();
        }

    }

    private MappedByteBuffer loadModelFromAssets(String fileName) throws FileException {
        try {
            AssetFileDescriptor fileDescriptor = getAssets().openFd(fileName);
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();

            FileChannel fileChannel = inputStream.getChannel();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (Exception ex) {
            throw new FileException("There was an error accessing model asset");
        }
    }

    private MappedByteBuffer loadLocalModelFile(String path) throws IOException {
        File file = new File(path);
        FileInputStream fis = null;
        fis = new FileInputStream(file);
        return fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }




    public void onCheckboxClicked(View view) {
        useDefaultManifest = !useDefaultManifest;
        ((Button) ((ViewGroup) view.getParent()).getChildAt(3)).setEnabled(!useDefaultManifest);
        ((Button) ((ViewGroup) view.getParent()).getChildAt(2)).setEnabled(useDefaultManifest);

    }

    public void setIndexOfCurrentModel(int i) {
        indexOfCurrentModel = i;
    }

    public String getModelName() {
        return models.get(indexOfCurrentModel).getModelName();
    }

}