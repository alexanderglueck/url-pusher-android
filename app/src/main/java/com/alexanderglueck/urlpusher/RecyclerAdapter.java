package com.alexanderglueck.urlpusher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyviewHolder> {
    Context context;
    List<Device> movieList;

    public RecyclerAdapter(Context context, List<Device> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    public void setMovieList(List<Device> movieList) {
        this.movieList = movieList;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerAdapter.MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_device_item, parent, false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.MyviewHolder holder, int position) {
        holder.tvMovieName.setText(movieList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        if (movieList != null) {
            return movieList.size();
        }
        return 0;

    }

    public class MyviewHolder extends RecyclerView.ViewHolder {
        TextView tvMovieName;

        public MyviewHolder(View itemView) {
            super(itemView);
            tvMovieName = (TextView) itemView.findViewById(R.id.tvName);
        }
    }
}
