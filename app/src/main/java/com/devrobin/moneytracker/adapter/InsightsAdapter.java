package com.devrobin.moneytracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devrobin.moneytracker.MVVM.Model.InsightData;
import com.devrobin.moneytracker.R;

import java.util.List;

public class InsightsAdapter extends RecyclerView.Adapter<InsightsAdapter.InsightsViewHolder> {
    private Context context;
    private List<InsightData> insightsList;

    public InsightsAdapter(Context context, List<InsightData> insightsList) {
        this.context = context;
        this.insightsList = insightsList;
    }

    @NonNull
    @Override
    public InsightsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_insight, parent, false);
        return new InsightsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InsightsViewHolder holder, int position) {
        InsightData insightData = insightsList.get(position);
        holder.bind(insightData);
    }

    @Override
    public int getItemCount() {
        return insightsList.size();
    }

    public void updateData(List<InsightData> newData) {
        this.insightsList = newData;
        notifyDataSetChanged();
    }

    class InsightsViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivInsightIcon;
        private TextView tvInsightText;
        private View viewAlertLevel;

        public InsightsViewHolder(@NonNull View itemView) {
            super(itemView);

            ivInsightIcon = itemView.findViewById(R.id.ivInsightIcon);
            tvInsightText = itemView.findViewById(R.id.tvInsightText);
            viewAlertLevel = itemView.findViewById(R.id.viewAlertLevel);
        }

        public void bind(InsightData insightData) {
            // Set insight text safely
            String text = insightData != null ? insightData.getText() : null;
            tvInsightText.setText(text == null ? "" : text);

            // Set icon and alert level based on insight type
            String type = insightData.getType();

            switch (type) {
                case "warning":
                    ivInsightIcon.setImageResource(R.drawable.outline_brightness_empty_24);
                    ivInsightIcon.setColorFilter(context.getResources().getColor(R.color.yellow));
                    viewAlertLevel.setBackgroundTintList(context.getResources().getColorStateList(R.color.yellow));
                    break;

                case "success":
                    ivInsightIcon.setImageResource(R.drawable.outline_brightness_empty_24);
                    ivInsightIcon.setColorFilter(context.getResources().getColor(R.color.green));
                    viewAlertLevel.setBackgroundTintList(context.getResources().getColorStateList(R.color.green));
                    break;

                case "info":
                default:
                    ivInsightIcon.setImageResource(R.drawable.outline_brightness_empty_24);
                    ivInsightIcon.setColorFilter(context.getResources().getColor(R.color.primary));
                    viewAlertLevel.setBackgroundTintList(context.getResources().getColorStateList(R.color.primary));
                    break;
            }

            // Set text color based on type
            if (type.equals("warning")) {
                tvInsightText.setTextColor(context.getResources().getColor(R.color.yellow));
            } else if (type.equals("success")) {
                tvInsightText.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                tvInsightText.setTextColor(context.getResources().getColor(R.color.text_primary));
            }
        }
    }
}