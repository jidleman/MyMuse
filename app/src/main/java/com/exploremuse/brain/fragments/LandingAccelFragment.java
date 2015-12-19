package com.exploremuse.brain.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exploremuse.brain.R;

/**
 * Created by jidleman on 12/19/2015.
 */
public class LandingAccelFragment extends Fragment {

    public LandingAccelFragment() {
        this.setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_landing_accel, container, false);


        return rootView;
    }
}