package com.example.villagets_androidstudio.View.Feed;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.PostViewModel;
import com.google.android.material.chip.ChipGroup;

public class MarketPlaceFragment extends Fragment {

    private RecyclerView recyclerView;
    private MarketPlaceAdapter adapter;
    private PostViewModel viewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText etMinPrice;
    private EditText etMaxPrice;
    private Button btnApplyFilters;
    private ChipGroup chipGroupSort;

    public MarketPlaceFragment() {
        super(R.layout.fragment_marketplace);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshMarketplace);
        recyclerView = view.findViewById(R.id.recyclerViewMarketplace);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new MarketPlaceAdapter();
        recyclerView.setAdapter(adapter);

        etMinPrice = view.findViewById(R.id.etMinPrice);
        etMaxPrice = view.findViewById(R.id.etMaxPrice);
        btnApplyFilters = view.findViewById(R.id.btnApplyFilters);
        chipGroupSort = view.findViewById(R.id.chipGroupSort);

        viewModel = new ViewModelProvider(this).get(PostViewModel.class);

        viewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (posts != null) {
                adapter.setPosts(posts);
            }
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), msg -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (msg != null) {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.red_primary);
        swipeRefreshLayout.setOnRefreshListener(this::refreshMarketplace);

        btnApplyFilters.setOnClickListener(v -> applyFilters());
        chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());
    }

    private void applyFilters() {
        if (viewModel == null) return;

        String minStr = etMinPrice.getText().toString().trim();
        String maxStr = etMaxPrice.getText().toString().trim();
        
        Double minPrice = null;
        Double maxPrice = null;
        
        try {
            if (!minStr.isEmpty()) minPrice = Double.parseDouble(minStr);
            if (!maxStr.isEmpty()) maxPrice = Double.parseDouble(maxStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Format de prix invalide", Toast.LENGTH_SHORT).show();
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        String sortMode = "DESC";
        int checkedId = chipGroupSort.getCheckedChipId();
        if (checkedId == R.id.chipOldest) {
            sortMode = "ASC";
        } else if (checkedId == R.id.chipTop) {
            sortMode = "TOP";
        }

        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        viewModel.rechercherPosts(null, null, true, minPrice, maxPrice, 0, sortMode, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshMarketplace();
    }

    private void refreshMarketplace() {
        if (viewModel == null) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }
        applyFilters();
    }
}
