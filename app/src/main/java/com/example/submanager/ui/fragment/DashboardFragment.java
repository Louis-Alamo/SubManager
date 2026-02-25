package com.example.submanager.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.Expense;
import com.example.submanager.ui.adapter.UpcomingPaymentAdapter;
import com.example.submanager.ui.viewmodel.ExpenseViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.widget.TextView;

public class DashboardFragment extends Fragment {

    private ExpenseViewModel viewModel;
    private TextView tvTotalAmount, tvRemainingAmount;
    private RecyclerView rvUpcomingPayments;
    private PieChart pieChart;
    private ShimmerFrameLayout shimmerUpcoming, shimmerChart;
    private UpcomingPaymentAdapter upcomingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupChart();
        observeData();
    }

    private void initViews(View view) {
        tvTotalAmount = view.findViewById(R.id.tvTotalAmount);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);
        rvUpcomingPayments = view.findViewById(R.id.rvUpcomingPayments);
        pieChart = view.findViewById(R.id.pieChart);
        shimmerUpcoming = view.findViewById(R.id.shimmerUpcoming);
        shimmerChart = view.findViewById(R.id.shimmerChart);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
    }

    private void setupRecyclerView() {
        upcomingAdapter = new UpcomingPaymentAdapter();
        rvUpcomingPayments.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvUpcomingPayments.setAdapter(upcomingAdapter);
    }

    private void setupChart() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setDrawCenterText(true);
        pieChart.setCenterTextSize(18f);
        pieChart.setRotationEnabled(true);
        pieChart.getLegend().setEnabled(false);
    }

    private void observeData() {
        // Show shimmer
        shimmerUpcoming.setVisibility(View.VISIBLE);
        shimmerUpcoming.startShimmer();
        shimmerChart.setVisibility(View.VISIBLE);
        shimmerChart.startShimmer();

        // Observe upcoming payments (next 7 days)
        Calendar calendar = Calendar.getInstance();
        long startDate = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        long endDate = calendar.getTimeInMillis();

        viewModel.getUpcomingExpenses(startDate, endDate, 5).observe(getViewLifecycleOwner(), expenses -> {
            shimmerUpcoming.stopShimmer();
            shimmerUpcoming.setVisibility(View.GONE);
            rvUpcomingPayments.setVisibility(View.VISIBLE);
            upcomingAdapter.setExpenses(expenses);
        });

        // Observe total amount
        viewModel.getTotalPendingByCurrency("MXN").observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
                tvTotalAmount.setText(currencyFormat.format(total));
                // Assuming a budget of 5000 MXN
                double remaining = 5000 - total;
                tvRemainingAmount.setText(getString(R.string.hero_card_remaining, currencyFormat.format(remaining)));
            }
        });

        // Observe all expenses for chart
        viewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses -> {
            shimmerChart.stopShimmer();
            shimmerChart.setVisibility(View.GONE);
            updateChart(expenses);
        });
    }

    private void updateChart(List<Expense> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            return;
        }

        pieChart.setVisibility(View.VISIBLE);

        // Group expenses by category
        Map<String, Float> categoryTotals = new HashMap<>();
        for (Expense expense : expenses) {
            String category = expense.getCategory();
            float amount = (float) expense.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + amount);
        }

        // Create pie entries
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(getCategoryColors());
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();

        // Calculate total
        float total = 0;
        for (Float value : categoryTotals.values()) {
            total += value;
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        pieChart.setCenterText(currencyFormat.format(total));
    }

    private int[] getCategoryColors() {
        return new int[] {
            getResources().getColor(R.color.category_streaming, null),
            getResources().getColor(R.color.category_home, null),
            getResources().getColor(R.color.category_software, null),
            getResources().getColor(R.color.category_fitness, null),
            getResources().getColor(R.color.category_education, null),
            getResources().getColor(R.color.category_food, null),
            getResources().getColor(R.color.category_other, null)
        };
    }
}

