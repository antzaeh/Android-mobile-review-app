package com.example.finaaliprojekti;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherDataAdapter extends RecyclerView.Adapter<WeatherDataAdapter.ViewHolder> {

    private final List<Pair<String, String>> weatherDataList;

    public WeatherDataAdapter(List<Pair<String, String>> weatherDataList) {
        this.weatherDataList = weatherDataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Pair<String, String> data = weatherDataList.get(position);
        holder.tvTime.setText(data.first);
        holder.tvTemperature.setText(data.second);
    }

    @Override
    public int getItemCount() {
        return weatherDataList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemperature;

        ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
        }
    }
}