package com.example.tpn;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tpn.databinding.CameraFragmentBinding;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {

    private CameraFragmentBinding binding;
    private Executor cameraExecutor;
    private ImageCapture imageCapture;
    private Preview preview;
    private Camera camera;
    private Bitmap bitmap;
    private boolean isBinded = false;


    public CameraFragment() {
        super(R.layout.camera_fragment);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = CameraFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!isBinded) {
            binding.viewCamera.bindToLifecycle(CameraFragment.this);
            binding.cameraCaptureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    {
                        takePhoto();
                        NavHostFragment.findNavController(CameraFragment.this)
                                .navigate(R.id.action_take_photo);
                    }
                }
            });
            cameraExecutor = Executors.newSingleThreadExecutor();
            isBinded = true;
            startCamera();
        }
    }

    public void startCamera(){
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    preview = new Preview.Builder().build();

                    imageCapture = new ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setTargetResolution(new Size(224, 224)) // Margaret set target resolution to 512x512
                            .build();

                    CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

                    cameraProvider.unbindAll();

                    camera = cameraProvider.bindToLifecycle(CameraFragment.this, cameraSelector, preview, imageCapture);

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(requireContext()));

    }

    public void takePhoto(){
       imageCapture.takePicture(cameraExecutor,
               new androidx.camera.core.ImageCapture.OnImageCapturedCallback(){
                    @Override
                    public void onCaptureSuccess(ImageProxy image){
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        buffer.rewind();
                        byte[] bytes = new byte[]{(byte) buffer.remaining()};
                        buffer.get(bytes);
                        ((MainActivity)getActivity()).setCurrentBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null));
                        NavHostFragment.findNavController(CameraFragment.this)
                                .navigate(R.id.action_take_photo);
                    }
                   @Override
                   public void onError(ImageCaptureException exception) {}
               });
    }
}
