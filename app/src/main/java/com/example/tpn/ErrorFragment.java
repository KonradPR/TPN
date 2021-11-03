package com.example.tpn;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tpn.databinding.ErrorFragmentBinding;


public class ErrorFragment extends Fragment {
    private static Exception currentException = null;
    private static boolean isCritical = false;
    private ErrorFragmentBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = ErrorFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.textView.setText(currentException.getMessage());
        if (isCritical) {
            binding.textView2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isCritical) {
            PackageManager packageManager = getContext().getPackageManager();
            Intent intent = packageManager.getLaunchIntentForPackage(getContext().getPackageName());
            ComponentName componentName = intent.getComponent();
            Intent mainIntent = Intent.makeRestartActivityTask(componentName);
            getContext().startActivity(mainIntent);
            Runtime.getRuntime().exit(0);
        }
    }

    public static void notifyCriticalError() {
        isCritical = true;
    }

    public static void setCurrentException(Exception ex) {
        currentException = ex;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCritical) {
            binding.textView2.setVisibility(View.VISIBLE);
        }
    }
}
