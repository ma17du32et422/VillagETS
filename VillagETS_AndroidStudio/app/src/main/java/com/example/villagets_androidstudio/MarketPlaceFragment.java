package com.example.villagets_androidstudio;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
public class MarketPlaceFragment extends Fragment {

    private GridView gridView;

    public MarketPlaceFragment() {
        super(R.layout.fragment_marketplace);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gridView = view.findViewById(R.id.gridView);

        // placeholders
        String[] itemNames = {"Camera", "Livre Physique", "MacNEO", "Cat for ram", "Billet de 20$", "Les secrets d'ETS", "Cleaning crew de Bonaventure", "Coupons d'A&W"};
        String[] itemPrices = {"1000$", "67$", "500$", "2000$", "20$", "314159$", "50$", "0$"};


        MarketPlaceManager adapter = new MarketPlaceManager(getContext(), itemNames, itemPrices);
        gridView.setAdapter(adapter);
    }
}
