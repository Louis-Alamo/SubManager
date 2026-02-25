package com.example.submanager.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.Expense;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UpcomingPaymentAdapter extends RecyclerView.Adapter<UpcomingPaymentAdapter.ViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upcoming_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense, listener);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivServiceIcon;
        private final TextView tvServiceName;
        private final TextView tvDueDate;
        private final TextView tvAmount;
        private final View iconBackground;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            iconBackground = itemView.findViewById(R.id.iconBackground);
        }

        public void bind(Expense expense, OnItemClickListener listener) {
            tvServiceName.setText(expense.getName());

            // Format amount
            NumberFormat currencyFormat;
            if ("USD".equals(expense.getCurrency())) {
                currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            } else {
                currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
            }
            tvAmount.setText(currencyFormat.format(expense.getAmount()));

            // Calculate days until due
            long currentTime = System.currentTimeMillis();
            long daysUntil = TimeUnit.MILLISECONDS.toDays(expense.getBillingDate() - currentTime);

            String dueDateText;
            int dueDateColor;

            if (daysUntil < 0) {
                dueDateText = itemView.getContext().getString(R.string.overdue);
                dueDateColor = itemView.getContext().getColor(R.color.error);
            } else if (daysUntil == 0) {
                dueDateText = itemView.getContext().getString(R.string.today);
                dueDateColor = itemView.getContext().getColor(R.color.error);
            } else if (daysUntil == 1) {
                dueDateText = itemView.getContext().getString(R.string.tomorrow);
                dueDateColor = itemView.getContext().getColor(R.color.warning);
            } else {
                dueDateText = itemView.getContext().getString(R.string.days_until, (int) daysUntil);
                dueDateColor = itemView.getContext().getColor(R.color.text_secondary);
            }

            tvDueDate.setText(dueDateText);
            tvDueDate.setTextColor(dueDateColor);

            // Set background color
            if (expense.getColorRes() != 0) {
                iconBackground.setBackgroundTintList(
                    itemView.getContext().getColorStateList(expense.getColorRes())
                );
            }

            // Set icon (you can use Glide here if you have URLs)
            ivServiceIcon.setImageResource(R.drawable.ic_star);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(expense);
                }
            });
        }
    }
}

