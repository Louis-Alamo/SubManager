package com.example.submanager.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.SuscripcionModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingAdapter extends RecyclerView.Adapter<UpcomingAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SuscripcionModel suscripcion);
    }

    private List<SuscripcionModel> items = new ArrayList<>();
    private OnItemClickListener listener;

    public UpcomingAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SuscripcionModel> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SuscripcionModel item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final View viewColorDot;
        private final TextView tvServiceName;
        private final TextView tvDaysRemaining;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColorDot = itemView.findViewById(R.id.viewColorDot);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDaysRemaining = itemView.findViewById(R.id.tvDaysRemaining);
        }

        void bind(SuscripcionModel item, OnItemClickListener listener) {
            tvServiceName.setText(item.getNombre());

            // Set color dot
            try {
                viewColorDot.setBackgroundColor(Color.parseColor(item.getColor()));
            } catch (IllegalArgumentException e) {
                viewColorDot.setBackgroundResource(R.color.primary);
            }

            // Calculate days remaining
            int daysRemaining = calculateDaysRemaining(item.getFechaProximoCobro());
            String daysText;
            int textColor;

            if (daysRemaining == 0) {
                daysText = itemView.getContext().getString(R.string.dashboard_upcoming_today);
                textColor = itemView.getContext().getColor(R.color.error);
            } else if (daysRemaining == 1) {
                daysText = itemView.getContext().getString(R.string.dashboard_upcoming_tomorrow);
                textColor = itemView.getContext().getColor(R.color.error);
            } else {
                daysText = itemView.getContext().getString(R.string.dashboard_upcoming_days, daysRemaining);
                textColor = itemView.getContext().getColor(R.color.text_muted);
            }

            tvDaysRemaining.setText(daysText);
            tvDaysRemaining.setTextColor(textColor);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(item);
                }
            });
        }

        private int calculateDaysRemaining(String fechaProximoCobro) {
            if (fechaProximoCobro == null || fechaProximoCobro.isEmpty()) {
                return 0;
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date dueDate = sdf.parse(fechaProximoCobro);
                if (dueDate == null) return 0;

                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                Calendar due = Calendar.getInstance();
                due.setTime(dueDate);
                due.set(Calendar.HOUR_OF_DAY, 0);
                due.set(Calendar.MINUTE, 0);
                due.set(Calendar.SECOND, 0);
                due.set(Calendar.MILLISECOND, 0);

                long diffMs = due.getTimeInMillis() - today.getTimeInMillis();
                long diffDays = diffMs / (1000 * 60 * 60 * 24);
                return (int) Math.max(0, diffDays);
            } catch (ParseException e) {
                return 0;
            }
        }
    }
}
