package com.example.tpn;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tpn.databinding.FragmentFirstBinding;
import com.google.android.material.snackbar.Snackbar;
import com.nbsp.materialfilepicker.MaterialFilePicker;

import java.util.regex.Pattern;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private static boolean wasAlreadyVisited = false;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkPermissions() {

        boolean permissionGrantedCamera = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;

        if (permissionGrantedCamera) {
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
            }
        }

        boolean permissionGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;

        if (permissionGranted) {

        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }
        boolean permissionGranted1 = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;

        if (permissionGranted1) {
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }

        boolean permissionGranted2 = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;

        if (permissionGranted2) {
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
            }
        }


        if(Integer.valueOf(android.os.Build.VERSION.SDK)>=30){
            if (Environment.isExternalStorageManager()){

            }else{
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getActivity().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        checkPermissions();
        super.onViewCreated(view, savedInstanceState);
        if(!wasAlreadyVisited){
            wasAlreadyVisited = true;
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage(R.string.menu);
            alertDialogBuilder.setNegativeButton("ok", new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        binding.currentModel.setText("Currently used model: " + ((MainActivity) getActivity()).getModelName());
        binding.buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_to_CameraFragment);
            }
        });

        binding.buttonSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_to_SelectFragment);
            }
        });

        binding.buttonModelLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_to_LoadModelFragment);
            }
        });
        binding.buttonFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).getPhotoFromGallery();
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}