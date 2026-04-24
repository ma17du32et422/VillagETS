package com.example.villagets_androidstudio.View.Feed;

import android.os.Bundle;
import android.view.View;
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

public class MarketPlaceFragment extends Fragment {

    private RecyclerView recyclerView;
    private MarketPlaceAdapter adapter;
    private PostViewModel viewModel;
    private SwipeRefreshLayout swipeRefreshLayout;

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

        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        viewModel.chargerPosts(true);
    }
}
