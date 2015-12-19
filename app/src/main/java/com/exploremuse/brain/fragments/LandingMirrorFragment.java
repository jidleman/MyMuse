package com.exploremuse.brain.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.exploremuse.brain.R;

/**
 * Created by jidleman on 12/19/2015.
 */
public class LandingMirrorFragment extends Fragment {
    public static AnimationDrawable eyeBlink, mouthMotion;

    public LandingMirrorFragment() {
        this.setArguments(new Bundle());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_landing_mirror, container, false);

        eyeBlink = (AnimationDrawable) ((ImageView)rootView.findViewById(R.id.eyes_image)).getBackground();
        mouthMotion = (AnimationDrawable) ((ImageView)rootView.findViewById(R.id.mouth_image)).getBackground();

        return rootView;
    }
}
