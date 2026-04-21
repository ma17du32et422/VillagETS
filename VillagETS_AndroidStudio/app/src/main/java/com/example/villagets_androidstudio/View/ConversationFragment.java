package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagets_androidstudio.R;

public class ConversationFragment extends Fragment {

    private RecyclerView recyclerView;

    public ConversationFragment() {
        super(R.layout.fragment_conversation);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewConversations);


    }
}
