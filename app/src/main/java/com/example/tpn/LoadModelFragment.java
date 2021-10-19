package com.example.tpn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.tpn.databinding.FragmentFirstBinding;
import com.example.tpn.databinding.LoadModelFragmentBinding;

public class LoadModelFragment extends Fragment {

    private LoadModelFragmentBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = LoadModelFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public LoadModelFragment() {
        super(R.layout.load_model_fragment);
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).modelName = String.valueOf(binding.editTextModelName.getText());
                ((MainActivity)getActivity()).pick();
                NavHostFragment.findNavController(LoadModelFragment.this)
                        .navigate(R.id.action_to_menu);
            }
        });;
    }

}
