package com.example.tpn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.ViewPager;

import com.example.tpn.databinding.CameraFragmentBinding;
import com.example.tpn.databinding.ResultFragmentBinding;


public class ResultFragment extends Fragment {

    private ResultFragmentBinding binding;
    ViewPager viewPager;
    CustomSwipeAdapter adapter;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    )  {
        super.onCreateView(inflater,container,savedInstanceState);
        binding = ResultFragmentBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        adapter = new CustomSwipeAdapter(this.getActivity());
        viewPager.setAdapter(adapter);
        return view;
    }



    public void show(){
        binding.progressbar.setVisibility(View.GONE);
        binding.check.setVisibility(View.GONE);
        binding.viewPager.setVisibility(View.VISIBLE);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((MainActivity)getActivity()).makePrediction();
        show();
        super.onViewCreated(view, savedInstanceState);
        binding.check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { {
                show();
            }}
        });


    }

    public ResultFragment() {
        super(R.layout.result_fragment);
    }

}
