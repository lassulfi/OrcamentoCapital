package com.assulfisoft.oramentodecapital.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.assulfisoft.oramentodecapital.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class OutrosFragment extends Fragment {


    public OutrosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_outros,container,false);
        return rootView;
    }

}
