package com.example.submanager.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.submanager.R;
import com.example.submanager.data.model.SuscripcionModel;
import com.example.submanager.databinding.FragmentHomeBinding;
import com.example.submanager.ui.adapter.UpcomingAdapter;
import com.example.submanager.ui.viewmodel.HomeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private UpcomingAdapter upcomingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupClickListeners();

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        observeViewModel();
    }

    private void setupRecyclerView() {
        upcomingAdapter = new UpcomingAdapter(this::onUpcomingItemClick);
        binding.rvUpcoming.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvUpcoming.setAdapter(upcomingAdapter);
        binding.rvUpcoming.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        // Settings icon — placeholder for SCR-09
        binding.btnSettings.setOnClickListener(v ->
                Toast.makeText(getContext(), "Ajustes (SCR-09 próximamente)", Toast.LENGTH_SHORT).show());

        // Add Subscription card — placeholder for SCR-03
        binding.cardAddSubscription.setOnClickListener(v ->
                Toast.makeText(getContext(), "Nueva Suscripción (SCR-03 próximamente)", Toast.LENGTH_SHORT).show());

        // Add Service card — placeholder for SCR-05
        binding.cardAddService.setOnClickListener(v ->
                Toast.makeText(getContext(), "Nuevo Servicio (SCR-05 próximamente)", Toast.LENGTH_SHORT).show());

        // See all — placeholder for SCR-08
        binding.tvSeeAll.setOnClickListener(v ->
                Toast.makeText(getContext(), "Ver todos los pagos (SCR-08 próximamente)", Toast.LENGTH_SHORT).show());
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case LOADING:
                    showLoading();
                    break;
                case SUCCESS:
                    showSuccess();
                    break;
                case EMPTY:
                    showEmpty();
                    break;
                case ERROR:
                    showError();
                    break;
            }
        });

        viewModel.getPendingAmount().observe(getViewLifecycleOwner(), amount ->
                binding.tvPendingAmount.setText(String.format("$%,.2f", amount)));

        viewModel.getPaidAmount().observe(getViewLifecycleOwner(), amount ->
                binding.tvPaidAmount.setText(String.format("$%,.2f", amount)));

        viewModel.getTotalAmount().observe(getViewLifecycleOwner(), amount ->
                binding.tvTotalAmount.setText(String.format("$%,.2f", amount)));

        viewModel.getUpcomingSubscriptions().observe(getViewLifecycleOwner(), this::updateUpcomingList);

        // Observe active subscriptions to trigger ViewModel side effects (amounts, state)
        viewModel.getActiveSubscriptions().observe(getViewLifecycleOwner(), list -> {
            // Side effects are handled inside ViewModel via MediatorLiveData
        });
    }

    private void showLoading() {
        binding.shimmerCards.setVisibility(View.VISIBLE);
        binding.shimmerCards.startShimmer();
        binding.hsvCards.setVisibility(View.GONE);
        binding.sectionUpcoming.setVisibility(View.GONE);
        binding.layoutEmptyUpcoming.setVisibility(View.GONE);
    }

    private void showSuccess() {
        binding.shimmerCards.stopShimmer();
        binding.shimmerCards.setVisibility(View.GONE);
        binding.hsvCards.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        binding.shimmerCards.stopShimmer();
        binding.shimmerCards.setVisibility(View.GONE);
        binding.hsvCards.setVisibility(View.VISIBLE);
        binding.tvPendingAmount.setText("$0.00");
        binding.tvPaidAmount.setText("$0.00");
        binding.tvTotalAmount.setText("$0.00");
        binding.sectionUpcoming.setVisibility(View.GONE);
        binding.layoutEmptyUpcoming.setVisibility(View.VISIBLE);
    }

    private void showError() {
        binding.shimmerCards.stopShimmer();
        binding.shimmerCards.setVisibility(View.GONE);
        binding.hsvCards.setVisibility(View.VISIBLE);
        binding.tvPendingAmount.setText("—");
        binding.tvPaidAmount.setText("—");
        binding.tvTotalAmount.setText("—");
        binding.sectionUpcoming.setVisibility(View.GONE);
        binding.layoutEmptyUpcoming.setVisibility(View.GONE);

        Snackbar.make(binding.getRoot(),
                        getString(R.string.dashboard_error),
                        Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.dashboard_retry), v -> viewModel.retry())
                .show();
    }

    private void updateUpcomingList(List<SuscripcionModel> list) {
        if (list == null || list.isEmpty()) {
            binding.sectionUpcoming.setVisibility(View.GONE);
            // Show empty state only when not in loading/error
            HomeViewModel.UiState currentState = viewModel.getUiState().getValue();
            if (currentState == HomeViewModel.UiState.EMPTY || currentState == HomeViewModel.UiState.SUCCESS) {
                binding.layoutEmptyUpcoming.setVisibility(View.VISIBLE);
            }
        } else {
            upcomingAdapter.submitList(list);
            binding.sectionUpcoming.setVisibility(View.VISIBLE);
            binding.layoutEmptyUpcoming.setVisibility(View.GONE);
        }
    }

    private void onUpcomingItemClick(SuscripcionModel suscripcion) {
        // Placeholder for SCR-03/SCR-05 edit mode
        Toast.makeText(getContext(),
                "Editar: " + suscripcion.getNombre() + " (SCR-03/SCR-05 próximamente)",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Show the BottomSheet for FAB (+) button.
     * Can be called from MainActivity or other components.
     */
    public void showAddBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_add, null);

        sheetView.findViewById(R.id.cardNewSubscription).setOnClickListener(v -> {
            dialog.dismiss();
            // Placeholder for SCR-03
            Toast.makeText(getContext(), "Nueva Suscripción (SCR-03 próximamente)", Toast.LENGTH_SHORT).show();
        });

        sheetView.findViewById(R.id.cardNewService).setOnClickListener(v -> {
            dialog.dismiss();
            // Placeholder for SCR-05
            Toast.makeText(getContext(), "Nuevo Servicio (SCR-05 próximamente)", Toast.LENGTH_SHORT).show();
        });

        dialog.setContentView(sheetView);
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
