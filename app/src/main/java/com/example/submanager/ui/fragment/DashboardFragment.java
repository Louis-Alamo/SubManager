package com.example.submanager.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.submanager.R;
import com.facebook.shimmer.ShimmerFrameLayout;

public class DashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Simulamos que los datos cargaron después de 1,5 s para ver el diseño real
        ShimmerFrameLayout shimmer = view.findViewById(R.id.shimmerDashboard);
        View scroll = view.findViewById(R.id.scrollDashboard);

        view.postDelayed(() -> {
            shimmer.stopShimmer();
            shimmer.setVisibility(View.GONE);
            scroll.setVisibility(View.VISIBLE);
        }, 1500);
    }
}
