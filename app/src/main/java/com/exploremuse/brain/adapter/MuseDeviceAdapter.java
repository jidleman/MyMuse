package com.exploremuse.brain.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.exploremuse.brain.LandingActivity;
import com.exploremuse.brain.R;
import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Muse;

import java.util.List;

/**
 * Created by jidleman on 12/18/2015.
 */
public class MuseDeviceAdapter extends ArrayAdapter<Muse> {
    private LayoutInflater mInflater;

    public MuseDeviceAdapter(Context context, List<Muse> content){
        super(context, android.R.layout.simple_list_item_1, content);
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeviceViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_device, null);

            holder = new DeviceViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (DeviceViewHolder)convertView.getTag();
        }

        Muse device = getItem(position);

        holder.deviceName.setText(device.getName());
        holder.connectivityButton.setText(
                (ConnectionState.CONNECTED == device.getConnectionState() || ConnectionState.CONNECTING==device.getConnectionState()) ?
                        getContext().getString(R.string.disconnect) : getContext().getString(R.string.connect)
        );
        holder.connectivityButton.setTag(device);
        holder.connectivityButton.setOnClickListener((LandingActivity)getContext());

        return convertView;
    }

    public static class DeviceViewHolder {
        public TextView deviceName;
        public Button connectivityButton;

        public DeviceViewHolder(View v) {
            deviceName = (TextView)v.findViewById(R.id.device_name_label);
            connectivityButton = (Button)v.findViewById(R.id.device_connectivity_button);
        }
    }
}