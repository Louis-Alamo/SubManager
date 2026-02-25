package com.example.submanager.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.submanager.data.database.AppDatabase;
import com.example.submanager.data.database.ExpenseDao;
import com.example.submanager.data.model.Expense;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {

    private ExpenseDao expenseDao;
    private ExecutorService executorService;

    public ExpenseRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        expenseDao = database.expenseDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Expense expense, OnCompleteListener listener) {
        executorService.execute(() -> {
            long id = expenseDao.insert(expense);
            if (listener != null) {
                listener.onComplete(id > 0);
            }
        });
    }

    public void update(Expense expense, OnCompleteListener listener) {
        executorService.execute(() -> {
            expenseDao.update(expense);
            if (listener != null) {
                listener.onComplete(true);
            }
        });
    }

    public void delete(Expense expense, OnCompleteListener listener) {
        executorService.execute(() -> {
            expenseDao.delete(expense);
            if (listener != null) {
                listener.onComplete(true);
            }
        });
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return expenseDao.getAllExpenses();
    }

    public LiveData<List<Expense>> getPendingExpenses() {
        return expenseDao.getPendingExpenses();
    }

    public LiveData<List<Expense>> getExpensesByCategory(String category) {
        return expenseDao.getExpensesByCategory(category);
    }

    public LiveData<List<Expense>> getExpensesByDateRange(long startDate, long endDate) {
        return expenseDao.getExpensesByDateRange(startDate, endDate);
    }

    public LiveData<Double> getTotalPendingByCurrency(String currency) {
        return expenseDao.getTotalPendingByCurrency(currency);
    }

    public LiveData<List<Expense>> getUpcomingExpenses(long startDate, long endDate, int limit) {
        return expenseDao.getUpcomingExpenses(startDate, endDate, limit);
    }

    public interface OnCompleteListener {
        void onComplete(boolean success);
    }
}

