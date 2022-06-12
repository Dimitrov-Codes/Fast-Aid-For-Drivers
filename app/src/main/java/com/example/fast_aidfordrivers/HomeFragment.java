package com.example.fast_aidfordrivers;

import android.location.Address;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.fast_aidfordrivers.R;

import org.jetbrains.annotations.NotNull;

public class HomeFragment extends Fragment {
    TextView tvAddress;
    Button btnBook;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvAddress = view.findViewById(R.id.tvAddress);

    }



    void setAddress(Address address) {

        tvAddress.setText(address.getAddressLine(0));
    }
}