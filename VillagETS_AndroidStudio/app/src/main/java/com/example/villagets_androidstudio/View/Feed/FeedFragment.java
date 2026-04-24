package com.example.villagets_androidstudio.View.Feed;

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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.PostViewModel;

public class FeedFragment extends Fragment {

    private PostViewModel viewModel;
    private PostAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshFeed);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        
        adapter = new PostAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Observer les changements de données
        viewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (posts != null) {
                adapter.setPosts(posts);
            }
        });

        // Observer les messages d'erreur
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        // Gestion de la pagination (Infinite Scroll)
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) { // On scroll vers le bas
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        viewModel.chargerPageSuivante();
                    }
                }
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.red_primary);
        swipeRefreshLayout.setOnRefreshListener(this::refreshFeed);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les posts à chaque fois que le fragment devient visible
        refreshFeed();
    }

    private void refreshFeed() {
        if (viewModel == null) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        if (swipeRefreshLayout != null && !swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(true);
        }
        viewModel.chargerPosts(false);
    }
}
