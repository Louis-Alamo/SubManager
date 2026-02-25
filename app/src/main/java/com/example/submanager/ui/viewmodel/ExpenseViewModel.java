package com.example.submanager.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.submanager.data.model.Expense;
import com.example.submanager.data.repository.ExpenseRepository;

import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private ExpenseRepository repository;
    private LiveData<List<Expense>> allExpenses;
    private LiveData<List<Expense>> pendingExpenses;

    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        allExpenses = repository.getAllExpenses();
        pendingExpenses = repository.getPendingExpenses();
    }

    public void insert(Expense expense, ExpenseRepository.OnCompleteListener listener) {
        repository.insert(expense, listener);
    }

    public void update(Expense expense, ExpenseRepository.OnCompleteListener listener) {
        repository.update(expense, listener);
    }

    public void delete(Expense expense, ExpenseRepository.OnCompleteListener listener) {
        repository.delete(expense, listener);
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return allExpenses;
    }

    public LiveData<List<Expense>> getPendingExpenses() {
        return pendingExpenses;
    }

    public LiveData<List<Expense>> getExpensesByCategory(String category) {
        return repository.getExpensesByCategory(category);
    }

    public LiveData<List<Expense>> getExpensesByDateRange(long startDate, long endDate) {
        return repository.getExpensesByDateRange(startDate, endDate);
    }

    public LiveData<Double> getTotalPendingByCurrency(String currency) {
        return repository.getTotalPendingByCurrency(currency);
    }

    public LiveData<List<Expense>> getUpcomingExpenses(long startDate, long endDate, int limit) {
        return repository.getUpcomingExpenses(startDate, endDate, limit);
    }
}

