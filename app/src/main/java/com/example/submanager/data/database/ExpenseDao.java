package com.example.submanager.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.submanager.data.model.Expense;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    long insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("SELECT * FROM expenses ORDER BY billingDate ASC")
    LiveData<List<Expense>> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE isPaid = 0 ORDER BY billingDate ASC")
    LiveData<List<Expense>> getPendingExpenses();

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY billingDate ASC")
    LiveData<List<Expense>> getExpensesByCategory(String category);

    @Query("SELECT * FROM expenses WHERE billingDate BETWEEN :startDate AND :endDate ORDER BY billingDate ASC")
    LiveData<List<Expense>> getExpensesByDateRange(long startDate, long endDate);

    @Query("SELECT SUM(amount) FROM expenses WHERE currency = :currency AND isPaid = 0")
    LiveData<Double> getTotalPendingByCurrency(String currency);

    @Query("SELECT * FROM expenses WHERE billingDate BETWEEN :startDate AND :endDate AND isPaid = 0 ORDER BY billingDate ASC LIMIT :limit")
    LiveData<List<Expense>> getUpcomingExpenses(long startDate, long endDate, int limit);
}

