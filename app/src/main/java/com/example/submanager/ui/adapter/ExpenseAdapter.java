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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

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

    public Expense getExpenseAt(int position) {
        return expenses.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
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
        private final ImageView ivIcon;
        private final ImageView ivPaidIndicator;
        private final TextView tvName;
        private final TextView tvDueDate;
        private final TextView tvAmount;
        private final TextView currencyBadge;
        private final View iconBackground;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivPaidIndicator = itemView.findViewById(R.id.ivPaidIndicator);
            tvName = itemView.findViewById(R.id.tvName);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            currencyBadge = itemView.findViewById(R.id.currencyBadge);
            iconBackground = itemView.findViewById(R.id.iconBackground);
        }

        public void bind(Expense expense, OnItemClickListener listener) {
            tvName.setText(expense.getName());
            currencyBadge.setText(expense.getCurrency());

            // Format amount
            tvAmount.setText(String.format(Locale.getDefault(), "$%.2f", expense.getAmount()));

            // Calculate days until due
            long currentTime = System.currentTimeMillis();
            long daysUntil = TimeUnit.MILLISECONDS.toDays(expense.getBillingDate() - currentTime);

            String dueDateText;
            if (daysUntil < 0) {
                dueDateText = itemView.getContext().getString(R.string.overdue);
            } else if (daysUntil == 0) {
                dueDateText = itemView.getContext().getString(R.string.expires_in, itemView.getContext().getString(R.string.today));
            } else if (daysUntil == 1) {
                dueDateText = itemView.getContext().getString(R.string.expires_in, itemView.getContext().getString(R.string.tomorrow));
            } else {
                dueDateText = itemView.getContext().getString(R.string.expires_in,
                    itemView.getContext().getString(R.string.days_until, (int) daysUntil));
            }

            tvDueDate.setText(dueDateText);

            // Show/hide paid indicator
            if (expense.isPaid()) {
                ivPaidIndicator.setVisibility(View.VISIBLE);
                tvAmount.setVisibility(View.GONE);
                currencyBadge.setVisibility(View.GONE);
            } else {
                ivPaidIndicator.setVisibility(View.GONE);
                tvAmount.setVisibility(View.VISIBLE);
                currencyBadge.setVisibility(View.VISIBLE);
            }

            // Set background color
            if (expense.getColorRes() != 0) {
                iconBackground.setBackgroundTintList(
                    itemView.getContext().getColorStateList(expense.getColorRes())
                );
            }

            // Set icon
            ivIcon.setImageResource(R.drawable.ic_star);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(expense);
                }
            });
        }
    }
}

