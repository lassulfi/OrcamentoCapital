package com.assulfisoft.oramentodecapital.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.assulfisoft.oramentodecapital.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TIRFragment extends Fragment {


    public TIRFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tir, container, false);
        return rootView;
    }

}
