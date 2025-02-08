package com.example.researchproject.ui.trip;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.researchproject.databinding.FragmentTripBinding;

public class TripFragment extends Fragment {

    private FragmentTripBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TripViewModel notificationsViewModel =
                new ViewModelProvider(this).get(TripViewModel.class);

        binding = FragmentTripBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textTrip;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}