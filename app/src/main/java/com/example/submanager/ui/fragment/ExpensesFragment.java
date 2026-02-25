package com.example.submanager.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.submanager.R;
import com.example.submanager.data.model.Expense;
import com.example.submanager.ui.adapter.ExpenseAdapter;
import com.example.submanager.ui.viewmodel.ExpenseViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class ExpensesFragment extends Fragment {

    private ExpenseViewModel viewModel;
    private RecyclerView rvExpenses;
    private ExpenseAdapter adapter;
    private View emptyState;
    private FloatingActionButton fab;
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupFilters(view);
        setupSwipeToDelete();
        observeData();
    }

    private void initViews(View view) {
        rvExpenses = view.findViewById(R.id.rvExpenses);
        emptyState = view.findViewById(R.id.emptyState);
        fab = view.findViewById(R.id.fab);

        fab.setOnClickListener(v -> navigateToAddExpense());
        view.findViewById(R.id.btnAddFirst).setOnClickListener(v -> navigateToAddExpense());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new ExpenseAdapter();
        rvExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExpenses.setAdapter(adapter);

        adapter.setOnItemClickListener(expense -> {
            // Navigate to edit expense
        });
    }

    private void setupFilters(View view) {
        Chip chipAll = view.findViewById(R.id.chipAll);
        Chip chipSubscriptions = view.findViewById(R.id.chipSubscriptions);
        Chip chipServices = view.findViewById(R.id.chipServices);
        Chip chipPending = view.findViewById(R.id.chipPending);
        Chip chipPaid = view.findViewById(R.id.chipPaid);

        chipAll.setOnClickListener(v -> {
            currentFilter = "all";
            observeData();
        });

        chipSubscriptions.setOnClickListener(v -> {
            currentFilter = "streaming";
            observeData();
        });

        chipServices.setOnClickListener(v -> {
            currentFilter = "home";
            observeData();
        });

        chipPending.setOnClickListener(v -> {
            currentFilter = "pending";
            observeData();
        });

        chipPaid.setOnClickListener(v -> {
            currentFilter = "paid";
            observeData();
        });
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Expense expense = adapter.getExpenseAt(position);

                if (direction == ItemTouchHelper.RIGHT) {
                    // Mark as paid
                    expense.setPaid(true);
                    viewModel.update(expense, success -> {
                        if (success) {
                            Snackbar.make(rvExpenses, R.string.marked_as_paid, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Delete
                    viewModel.delete(expense, success -> {
                        if (success) {
                            Snackbar.make(rvExpenses, R.string.expense_deleted, Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, v -> {
                                    viewModel.insert(expense, null);
                                })
                                .show();
                        }
                    });
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(rvExpenses);
    }

    private void observeData() {
        if ("all".equals(currentFilter)) {
            viewModel.getAllExpenses().observe(getViewLifecycleOwner(), this::updateUI);
        } else if ("pending".equals(currentFilter)) {
            viewModel.getPendingExpenses().observe(getViewLifecycleOwner(), this::updateUI);
        } else if ("paid".equals(currentFilter)) {
            viewModel.getAllExpenses().observe(getViewLifecycleOwner(), expenses -> {
                if (expenses != null) {
                    expenses.removeIf(expense -> !expense.isPaid());
                }
                updateUI(expenses);
            });
        } else {
            viewModel.getExpensesByCategory(currentFilter).observe(getViewLifecycleOwner(), this::updateUI);
        }
    }

    private void updateUI(java.util.List<Expense> expenses) {
        if (expenses == null || expenses.isEmpty()) {
            rvExpenses.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvExpenses.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            adapter.setExpenses(expenses);
        }
    }

    private void navigateToAddExpense() {
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragmentContainer, new AddExpenseFragment())
            .addToBackStack(null)
            .commit();
    }
}

