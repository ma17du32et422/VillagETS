package com.example.villagets_androidstudio.View.Search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View.Feed.PostAdapter;
import com.example.villagets_androidstudio.View_Model.PostViewModel;
import com.example.villagets_androidstudio.View_Model.UserViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

public class SearchFragment extends Fragment {

    private PostViewModel postViewModel;
    private UserViewModel userViewModel;
    private PostAdapter postAdapter;
    private UserSearchAdapter userAdapter;
    private TextInputEditText etSearch;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;

    public SearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etSearch);
        tabLayout = view.findViewById(R.id.tabLayoutSearch);
        recyclerView = view.findViewById(R.id.recyclerViewResults);

        postAdapter = new PostAdapter();
        userAdapter = new UserSearchAdapter();
        
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        // Par défaut on affiche l'adaptateur de posts
        recyclerView.setAdapter(postAdapter);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        postViewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            if (tabLayout.getSelectedTabPosition() == 0) {
                postAdapter.setPosts(posts);
            }
        });

        userViewModel.getSearchResultsLiveData().observe(getViewLifecycleOwner(), users -> {
            if (tabLayout.getSelectedTabPosition() == 1) {
                userAdapter.setUsers(users);
            }
        });

        // Gestion de la pagination (Infinite Scroll) pour les Posts
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // On ne gère le scroll infini que pour les posts (onglet 0)
                // car l'API User ne semble pas encore supporter la pagination
                if (dy > 0 && tabLayout.getSelectedTabPosition() == 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        postViewModel.chargerPageSuivante();
                    }
                }
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    recyclerView.setAdapter(postAdapter);
                } else {
                    recyclerView.setAdapter(userAdapter);
                }
                performSearch(etSearch.getText().toString().trim());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Recherche initiale
        performSearch("");
    }

    private void performSearch(String query) {
        if (tabLayout.getSelectedTabPosition() == 0) {
            postViewModel.rechercherPosts(query, null, false, null, null, 0, "DESC", false);
        } else {
            userViewModel.searchUsers(query);
        }
    }
}
