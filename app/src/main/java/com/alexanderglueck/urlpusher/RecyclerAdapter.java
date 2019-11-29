package com.alexanderglueck.urlpusher;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyviewHolder> {
    List<Device> deviceList;

    public RecyclerAdapter(List<Device> movieList) {
        this.deviceList = movieList;
    }

    public void setDeviceList(List<Device> deviceList) {
        this.deviceList = deviceList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerAdapter.MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_device_item, parent, false);

        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.MyviewHolder holder, int position) {
        holder.setItem(deviceList.get(position));
    }

    @Override
    public int getItemCount() {
        if (deviceList != null) {
            return deviceList.size();
        }
        return 0;
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvMovieName;
        private Device mDevice;

        public MyviewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvMovieName = (TextView) itemView.findViewById(R.id.tvName);
        }

        public void setItem(Device device) {
            mDevice = device;

            tvMovieName.setText(device.getName());
        }

        @Override
        public void onClick(View v) {
            Log.d("tag", "onClick " + getAdapterPosition() + " " + mDevice.getId() + " " + mDevice.getToken());

            // set device token for this login
            // close device chooser
            SharedPreferences sharedPreferences = v.getContext().getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, MODE_PRIVATE);

            sharedPreferences.edit().putInt(Constants.LAST_SIGNED_IN_DEVICE_ID, mDevice.getId()).commit();
        }
    }
}
