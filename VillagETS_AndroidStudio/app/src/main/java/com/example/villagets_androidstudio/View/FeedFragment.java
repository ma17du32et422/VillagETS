package com.example.villagets_androidstudio.View;

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

import com.example.villagets_androidstudio.R;
import com.example.villagets_androidstudio.View_Model.PostViewModel;

public class FeedFragment extends Fragment {

    private PostViewModel viewModel;
    private PostAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new PostAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(PostViewModel.class);

        // Observer les changements de données
        viewModel.getPostsLiveData().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                adapter.setPosts(posts);
            }
        });

        // Observer les messages d'erreur
        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les posts à chaque fois que le fragment devient visible
        // (Utile après un login ou un retour sur l'app)
        if (viewModel != null) {
            viewModel.chargerPosts();
        }
    }
}
