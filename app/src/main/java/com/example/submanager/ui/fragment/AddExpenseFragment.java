package com.example.submanager.ui.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.Expense;
import com.example.submanager.data.model.PopularService;
import com.example.submanager.ui.adapter.PopularServiceAdapter;
import com.example.submanager.ui.viewmodel.ExpenseViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseFragment extends Fragment {

    private ExpenseViewModel viewModel;
    private RecyclerView rvPopularServices;
    private PopularServiceAdapter serviceAdapter;
    private TextInputEditText etServiceName, etAmount, etBillingDate, etNotes;
    private AutoCompleteTextView actvCategory;
    private MaterialButtonToggleGroup toggleCurrency;
    private TextView tvExchangeRate;
    private MaterialButton btnSave, btnCancel;

    private String selectedCurrency = "MXN";
    private Calendar selectedDate = Calendar.getInstance();
    private PopularService selectedService = null;
    private final double USD_TO_MXN_RATE = 19.05; // You can make this dynamic

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_expense, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupPopularServices();
        setupCategoryDropdown();
        setupDatePicker();
        setupCurrencyToggle();
        setupAmountWatcher();
        setupButtons();
    }

    private void initViews(View view) {
        rvPopularServices = view.findViewById(R.id.rvPopularServices);
        etServiceName = view.findViewById(R.id.etServiceName);
        etAmount = view.findViewById(R.id.etAmount);
        etBillingDate = view.findViewById(R.id.etBillingDate);
        etNotes = view.findViewById(R.id.etNotes);
        actvCategory = view.findViewById(R.id.actvCategory);
        toggleCurrency = view.findViewById(R.id.toggleCurrency);
        tvExchangeRate = view.findViewById(R.id.tvExchangeRate);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
    }

    private void setupPopularServices() {
        List<PopularService> services = getPopularServices();
        serviceAdapter = new PopularServiceAdapter(services);
        rvPopularServices.setLayoutManager(new GridLayoutManager(getContext(), 4));
        rvPopularServices.setAdapter(serviceAdapter);

        serviceAdapter.setOnServiceClickListener(service -> {
            selectedService = service;
            etServiceName.setText(service.getName());
            actvCategory.setText(service.getCategory(), false);
        });
    }

    private List<PopularService> getPopularServices() {
        List<PopularService> services = new ArrayList<>();
        services.add(new PopularService("Netflix", "streaming", R.drawable.ic_star, R.color.category_streaming));
        services.add(new PopularService("Spotify", "streaming", R.drawable.ic_star, R.color.category_streaming));
        services.add(new PopularService("Disney+", "streaming", R.drawable.ic_star, R.color.category_streaming));
        services.add(new PopularService("HBO Max", "streaming", R.drawable.ic_star, R.color.category_streaming));
        services.add(new PopularService("CFE", "home", R.drawable.ic_home, R.color.category_home));
        services.add(new PopularService("Agua", "home", R.drawable.ic_home, R.color.category_home));
        services.add(new PopularService("Internet", "home", R.drawable.ic_home, R.color.category_home));
        services.add(new PopularService("Gas", "home", R.drawable.ic_home, R.color.category_home));
        return services;
    }

    private void setupCategoryDropdown() {
        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);
    }

    private void setupDatePicker() {
        // Set current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etBillingDate.setText(dateFormat.format(selectedDate.getTime()));

        etBillingDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    etBillingDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void setupCurrencyToggle() {
        toggleCurrency.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnUSD) {
                    selectedCurrency = "USD";
                    updateExchangeRateInfo();
                } else {
                    selectedCurrency = "MXN";
                    tvExchangeRate.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupAmountWatcher() {
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ("USD".equals(selectedCurrency)) {
                    updateExchangeRateInfo();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateExchangeRateInfo() {
        String amountStr = etAmount.getText().toString();
        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                double mxnAmount = amount * USD_TO_MXN_RATE;
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
                tvExchangeRate.setText(getString(R.string.approximately, currencyFormat.format(mxnAmount)));
                tvExchangeRate.setVisibility(View.VISIBLE);
            } catch (NumberFormatException e) {
                tvExchangeRate.setVisibility(View.GONE);
            }
        } else {
            tvExchangeRate.setVisibility(View.GONE);
        }
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> saveExpense());
        btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void saveExpense() {
        String name = etServiceName.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etServiceName.setError(getString(R.string.service_name));
            return;
        }

        if (amountStr.isEmpty()) {
            etAmount.setError(getString(R.string.amount));
            return;
        }

        if (category.isEmpty()) {
            actvCategory.setError(getString(R.string.category));
            return;
        }

        double amount = Double.parseDouble(amountStr);

        Expense expense = new Expense(
            name,
            amount,
            selectedCurrency,
            category,
            selectedDate.getTimeInMillis(),
            notes,
            false,
            "",
            selectedService != null ? selectedService.getColorRes() : R.color.category_other
        );

        viewModel.insert(expense, success -> {
            if (success) {
                requireActivity().runOnUiThread(() -> {
                    Snackbar.make(requireView(), R.string.expense_added, Snackbar.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            }
        });
    }
}

