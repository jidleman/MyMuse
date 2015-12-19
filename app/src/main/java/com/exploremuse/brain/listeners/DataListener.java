package com.exploremuse.brain.listeners;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.exploremuse.brain.LandingActivity;
import com.exploremuse.brain.R;
import com.interaxon.libmuse.Accelerometer;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseFileWriter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @author Muse w/ updates by jidleman
 */
public class DataListener extends MuseDataListener {

    final WeakReference<Activity> activityRef;
    private MuseFileWriter fileWriter;

    public DataListener(final WeakReference<Activity> activityRef) {
        this.activityRef = activityRef;
    }

    @Override
    public void receiveMuseDataPacket(MuseDataPacket p) {
        switch (p.getPacketType()) {
            case ACCELEROMETER:
                //updateAccelerometer(p.getValues());
                break;
            default:
                break;
        }
    }

    @Override
    public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
        Activity activity = activityRef.get();

        if (p.getHeadbandOn() && p.getBlink()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LandingActivity activity = (LandingActivity)activityRef.get();
                    /*activity.eyeBlink.stop(); //reset any previous
                    activity.eyeBlink.selectDrawable(0); //reset any previous
                    activity.eyeBlink.start();*/
                }
            });

            Log.i("Artifacts", "blink");
        }
        if (p.getHeadbandOn() && p.getJawClench()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LandingActivity activity = (LandingActivity)activityRef.get();
                    /*activity.mouthMotion.stop(); //reset any previous
                    activity.mouthMotion.selectDrawable(0); //reset any previous
                    activity.mouthMotion.start();*/
                }
            });

            Log.i("Artifacts", "jaw");
        }
    }

    /*
    private void updateAccelerometer(final ArrayList<Double> data) {
        Activity activity = activityRef.get();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Activity activity = activityRef.get();
                    TextView acc_x = (TextView)activity.findViewById(R.id.acc_x);
                    TextView acc_y = (TextView)activity.findViewById(R.id.acc_y);
                    TextView acc_z = (TextView)activity.findViewById(R.id.acc_z);
                    acc_x.setText(String.format("%6.2f", data.get(Accelerometer.FORWARD_BACKWARD.ordinal())));
                    acc_y.setText(String.format("%6.2f", data.get(Accelerometer.UP_DOWN.ordinal())));
                    acc_z.setText(String.format("%6.2f", data.get(Accelerometer.LEFT_RIGHT.ordinal())));
                }
            });
        }
    }
    */

    public void setFileWriter(MuseFileWriter fileWriter) {
        this.fileWriter  = fileWriter;
    }
}