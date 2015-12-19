package com.exploremuse.brain;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.exploremuse.brain.adapter.MuseDeviceAdapter;
import com.exploremuse.brain.listeners.ConnectionListener;
import com.exploremuse.brain.listeners.DataListener;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseFileFactory;
import com.interaxon.libmuse.MuseFileWriter;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class LandingActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView connectivityStatusText;
    private ListView devicesList;
    private Muse selectedMuse;
    private ConnectionListener connectionListener;
    private DataListener dataListener;
    private MuseFileWriter fileWriter;
    public AnimationDrawable eyeBlink, mouthMotion;

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
        eyeBlink = (AnimationDrawable) ((ImageView)findViewById(R.id.eyes_image)).getBackground();
        mouthMotion = (AnimationDrawable) ((ImageView)findViewById(R.id.mouth_image)).getBackground();

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
        /**
         * In most cases libmuse native library takes care about
         * exceptions and recovery mechanism, but native code still
         * may throw in some unexpected situations (like bad bluetooth
         * connection). Print all exceptions here.
         */
        try {
            muse.runAsynchronously();
        } catch (Exception e) {
            Log.e("Muse Headband", e.toString());
        }
    }

    private void disconnect(Muse muse) {
        if (muse != null) {
            /**
             * true flag will force libmuse to unregister all listeners,
             * BUT AFTER disconnecting and sending disconnection event.
             * If you don't want to receive disconnection event (for ex.
             * you call disconnect when application is closed), then
             * unregister listeners first and then call disconnect:
             * muse.unregisterAllListeners();
             * muse.disconnect(false);
             */
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
}
