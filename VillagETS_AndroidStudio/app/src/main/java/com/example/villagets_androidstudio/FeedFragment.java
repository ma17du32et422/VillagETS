package com.example.villagets_androidstudio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Données de test
        List<Post> posts = new ArrayList<>();
        posts.add(new Post(1, 101, "Premier Post", "Ceci est le contenu du premier post.", null, "2023-10-01", null));
        posts.add(new Post(2, 102, "Deuxième Post", "Un autre exemple de post dans le RecyclerView.", null, "2023-10-02", null));
        posts.add(new Post(3, 103, "Troisième Post", "Le défilement est maintenant géré par le RecyclerView.", null, "2023-10-03", null));

        PostAdapter adapter = new PostAdapter(posts);
        recyclerView.setAdapter(adapter);

        return view;
    }
}