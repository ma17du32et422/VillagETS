package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagets_androidstudio.R;

public class MarketPlaceFragment extends Fragment {

    private RecyclerView recyclerView;

    public MarketPlaceFragment() {
        super(R.layout.fragment_marketplace);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewMarketplace);

        // placeholders
        String[] itemNames = {"Camera", "Livre Physique", "MacNEO", "Cat for ram", "Billet de 20$", "Les secrets d'ETS", "Cleaning crew de Bonaventure", "Coupons d'A&W"};
        String[] itemPrices = {"1000$", "67$", "500$", "2000$", "20$", "314159$", "50$", "0$"};

        MarketPlaceManager adapter = new MarketPlaceManager(itemNames, itemPrices);
        recyclerView.setAdapter(adapter);
    }
}
