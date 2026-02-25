package com.example.submanager.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.PopularService;

import java.util.ArrayList;
import java.util.List;

public class PopularServiceAdapter extends RecyclerView.Adapter<PopularServiceAdapter.ViewHolder> {

    private List<PopularService> services = new ArrayList<>();
    private OnServiceClickListener listener;
    private int selectedPosition = -1;

    public interface OnServiceClickListener {
        void onServiceClick(PopularService service);
    }

    public PopularServiceAdapter(List<PopularService> services) {
        this.services = services;
    }

    public void setOnServiceClickListener(OnServiceClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_popular_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PopularService service = services.get(position);
        holder.bind(service, position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            if (previousSelected != -1) {
                notifyItemChanged(previousSelected);
            }
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onServiceClick(service);
            }
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivServiceIcon;
        private final ImageView ivSelected;
        private final TextView tvServiceName;
        private final View iconBackground;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            ivSelected = itemView.findViewById(R.id.ivSelected);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            iconBackground = itemView.findViewById(R.id.iconBackground);
        }

        public void bind(PopularService service, boolean isSelected) {
            tvServiceName.setText(service.getName());
            ivServiceIcon.setImageResource(service.getIconRes());

            // Set background color
            iconBackground.setBackgroundTintList(
                itemView.getContext().getColorStateList(service.getColorRes())
            );

            // Show/hide selection indicator
            ivSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        }
    }
}

