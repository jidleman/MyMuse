package com.exploremuse.brain.listeners;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.exploremuse.brain.R;
import com.exploremuse.brain.adapter.MuseDeviceAdapter;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import java.lang.ref.WeakReference;

/**
 * Connection listener updates UI with new connection status and logs it.
 * @author Muse w/ updates by jidleman
 */
public class ConnectionListener extends MuseConnectionListener {

    final WeakReference<Activity> activityRef;

    public ConnectionListener(final WeakReference<Activity> activityRef) {
        this.activityRef = activityRef;
    }

    @Override
    public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
        final ConnectionState current = p.getCurrentConnectionState();
        final String status = p.getSource().getMacAddress()+ ": " +current;
        Log.i("Muse Headband", "Muse " +p.getSource().getMacAddress()+ " " +status);
        final Activity activity = activityRef.get();
        // UI thread is used here only because we need to update
        // TextView values. You don't have to use another thread, unless
        // you want to run disconnect() or connect() from connection packet
        // handler. In this case creating another thread is required.
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Activity contextActivity = activityRef.get();

                    if(ConnectionState.CONNECTED==current) {
                        ((MuseDeviceAdapter)((ListView)contextActivity.findViewById(R.id.devices_list)).getAdapter()).notifyDataSetChanged();
                        contextActivity.findViewById(R.id.eyes_image).setVisibility(View.VISIBLE);
                        contextActivity.findViewById(R.id.mouth_image).setVisibility(View.VISIBLE);
                    }
                    else {
                        contextActivity.findViewById(R.id.eyes_image).setVisibility(View.GONE);
                        contextActivity.findViewById(R.id.mouth_image).setVisibility(View.GONE);

                        if(ConnectionState.DISCONNECTED==current) {
                            ((MuseDeviceAdapter) ((ListView) contextActivity.findViewById(R.id.devices_list)).getAdapter()).notifyDataSetChanged();
                        }
                    }

                    ((TextView)contextActivity.findViewById(R.id.device_status_text)).setText(status);
                }
            });
        }
    }
}
