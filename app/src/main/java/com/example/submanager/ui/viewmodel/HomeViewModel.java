package com.example.submanager.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.data.repository.SuscripcionRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeViewModel extends AndroidViewModel {

    public enum UiState { LOADING, SUCCESS, EMPTY, ERROR }

    private final SuscripcionRepository repository;

    private final MutableLiveData<UiState> uiState = new MutableLiveData<>(UiState.LOADING);
    private final MutableLiveData<Double> pendingAmount = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> paidAmount = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> totalAmount = new MutableLiveData<>(0.0);

    // MediatorLiveData to observe active subscriptions without leaking observers
    private final MediatorLiveData<List<SuscripcionModel>> activeSubscriptions = new MediatorLiveData<>();

    private final LiveData<List<SuscripcionModel>> upcomingSubscriptions;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new SuscripcionRepository(application);

        // Compute date range: today .. today+7
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.DAY_OF_YEAR, 7);
        String startDate = sdf.format(start.getTime());
        String endDate = sdf.format(end.getTime());

        upcomingSubscriptions = repository.getSuscripcionesProximasAVencer(startDate, endDate);

        // Use MediatorLiveData to avoid observeForever leaks
        activeSubscriptions.addSource(repository.getSuscripcionesActivas(), list -> {
            activeSubscriptions.setValue(list);
            if (list == null) {
                uiState.setValue(UiState.ERROR);
                return;
            }
            if (list.isEmpty()) {
                pendingAmount.setValue(0.0);
                paidAmount.setValue(0.0);
                totalAmount.setValue(0.0);
                uiState.setValue(UiState.EMPTY);
                return;
            }

            double total = 0.0;
            for (SuscripcionModel s : list) {
                total += s.getMonto();
            }
            // Without explicit paid registry, treat all active subscriptions as pending.
            // Paid amount defaults to 0 until payment records are integrated.
            totalAmount.setValue(total);
            pendingAmount.setValue(total);
            paidAmount.setValue(0.0);
            uiState.setValue(UiState.SUCCESS);
        });
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public LiveData<Double> getPendingAmount() {
        return pendingAmount;
    }

    public LiveData<Double> getPaidAmount() {
        return paidAmount;
    }

    public LiveData<Double> getTotalAmount() {
        return totalAmount;
    }

    public LiveData<List<SuscripcionModel>> getUpcomingSubscriptions() {
        return upcomingSubscriptions;
    }

    public LiveData<List<SuscripcionModel>> getActiveSubscriptions() {
        return activeSubscriptions;
    }

    public void retry() {
        uiState.setValue(UiState.LOADING);
    }
}
