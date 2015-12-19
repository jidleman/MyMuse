package com.exploremuse.brain;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.exploremuse.brain.adapter.MuseDeviceAdapter;
import com.exploremuse.brain.fragments.LandingAccelFragment;
import com.exploremuse.brain.fragments.LandingMirrorFragment;
import com.exploremuse.brain.views.DepthPageTransformer;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseFileFactory;
import com.interaxon.libmuse.MuseFileWriter;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class LandingActivity extends AppCompatActivity implements View.OnClickListener {
    public ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    public int NUM_PAGES = 2;
    private TextView connectivityStatusText;
    private ListView devicesList;
    public Muse selectedMuse;
    private ConnectionListener connectionListener;
    private DataListener dataListener;
    public MuseFileWriter fileWriter;

    public LandingActivity() {
        WeakReference<Activity> weakActivity = new WeakReference<Activity>(this);
        connectionListener = new ConnectionListener(weakActivity);
        dataListener = new DataListener(weakActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_activity);
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        connectivityStatusText = (TextView)findViewById(R.id.device_status_text);
        devicesList = (ListView)findViewById(R.id.devices_list);

        mPager = (ViewPager) findViewById(R.id.landing_pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new DepthPageTransformer());

        File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        fileWriter = MuseFileFactory.getMuseFileWriter(new File(dir, "new_muse_file.muse"));
        fileWriter.addAnnotationString(1, "LandingActivity onCreate");
        dataListener.setFileWriter(fileWriter);

        loadMuseList();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(selectedMuse != null && (isConnected(selectedMuse))) {
            selectedMuse.enableDataTransmission(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(selectedMuse != null) {
            selectedMuse.enableDataTransmission(false);
        }
    }

    private void loadMuseList() {
        MuseManager.refreshPairedMuses();
        List<Muse> paired = MuseManager.getPairedMuses();
        if (paired.size() < 1) {
            connectivityStatusText.setText(getString(R.string.status_no_devices));
            devicesList.setVisibility(View.GONE);
        }
        else {
            devicesList.setAdapter(new MuseDeviceAdapter(this, paired));
            devicesList.setVisibility(View.VISIBLE);

            if(hasConnection(paired)) {
                connectivityStatusText.setText(getString(R.string.status_connected)+ " " +selectedMuse.getName());
            }
            else {
                connectivityStatusText.setText(getString(R.string.status_select));
            }
        }
    }

    private boolean hasConnection(List<Muse> paired) {
        boolean connected = false;
        for(Muse muse : paired) {
            if(isConnected(muse)) {
                connected = true;
                this.selectedMuse = muse;
                break;
            }
        }

        return connected;
    }
    private boolean isConnected(Muse muse) {
        return ConnectionState.CONNECTED==muse.getConnectionState() || ConnectionState.CONNECTING==muse.getConnectionState();
    }

    public void refreshDevices(View v) {
        loadMuseList();
    }

    private void connect(Muse muse) {
        selectedMuse = muse;

        if (isConnected(muse)) {
            return;
        }
        initialize();
        fileWriter.open();
        fileWriter.addAnnotationString(1, "Connect clicked");

        try {
            muse.runAsynchronously();
        } catch (Exception e) {
            Log.e("Muse Headband", e.toString());
        }
    }

    private void disconnect(Muse muse) {
        if (muse != null) {
            muse.disconnect(true);
            fileWriter.addAnnotationString(1, "Disconnect clicked");
            fileWriter.flush();
            fileWriter.close();
        }

        selectedMuse = null;
    }

    private void initialize() {
        selectedMuse.registerConnectionListener(connectionListener);
        //selectedMuse.registerDataListener(dataListener, MuseDataPacketType.ACCELEROMETER);
        selectedMuse.registerDataListener(dataListener, MuseDataPacketType.ARTIFACTS);
        selectedMuse.setPreset(MusePreset.PRESET_14);
        selectedMuse.enableDataTransmission(true);
    }

    @Override
    public void onClick(View v) {
        Muse muse = (Muse)v.getTag();
        if(isConnected(muse)) {
            disconnect(muse);
        }
        else {
            connect(muse);
        }
    }

    /**
     * Handle connection events related to the Muse device.
     */
    class ConnectionListener extends MuseConnectionListener {
        final WeakReference<Activity> activityRef;
        public ConnectionListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            final String status = p.getSource().getMacAddress() + ": " + current;
            Log.i("Muse Headband", "Muse " + p.getSource().getMacAddress() + " " + status);
            final Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ConnectionState.CONNECTED == current) {
                            ((MuseDeviceAdapter) ((ListView)findViewById(R.id.devices_list)).getAdapter()).notifyDataSetChanged();
                            findViewById(R.id.eyes_image).setVisibility(View.VISIBLE);
                            findViewById(R.id.mouth_image).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.eyes_image).setVisibility(View.GONE);
                            findViewById(R.id.mouth_image).setVisibility(View.GONE);

                            if (ConnectionState.DISCONNECTED == current) {
                                ((MuseDeviceAdapter) ((ListView)findViewById(R.id.devices_list)).getAdapter()).notifyDataSetChanged();
                            }
                        }

                        ((TextView)findViewById(R.id.device_status_text)).setText(status);
                    }
                });
            }
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private String[] title = {
                "MIRROR",
                "MUSE 2"
        };

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new LandingMirrorFragment();
                case 1:
                    return new LandingAccelFragment();

            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

    class DataListener extends MuseDataListener {
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
                        LandingMirrorFragment.eyeBlink.stop(); //reset any previous
                        LandingMirrorFragment.eyeBlink.selectDrawable(0); //reset any previous
                        LandingMirrorFragment.eyeBlink.start();
                    }
                });

                Log.i("Artifacts", "blink");
            }
            if (p.getHeadbandOn() && p.getJawClench()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LandingMirrorFragment.mouthMotion.stop(); //reset any previous
                        LandingMirrorFragment.mouthMotion.selectDrawable(0); //reset any previous
                        LandingMirrorFragment.mouthMotion.start();
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
}
