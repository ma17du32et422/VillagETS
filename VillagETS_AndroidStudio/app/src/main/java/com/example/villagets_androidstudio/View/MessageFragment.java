package com.example.villagets_androidstudio.View;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagets_androidstudio.R;

public class MessageFragment extends Fragment {

    private RecyclerView recyclerView;

    public MessageFragment() {
        super(R.layout.fragment_message);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewConversations);

        // placeholders
        String[] userNames = {"Thomas", "Sarah", "Marc-Antoine", "Julie", "Kevin"};
        String[] lastMessages = {
                "C'est bon pour la calculatrice !",
                "Est-ce que le livre est toujours dispo ?",
                "Je peux passer demain à 14h.",
                "Merci beaucoup !",
                "Salut, tu fais un prix pour le lot ?"
        };
        String[] times = {"14:30", "Hier", "Hier", "Lun.", "2 janv."};
        Integer[] avatarResIds = {
                R.drawable.silicate,
                R.drawable.silicate,
                R.drawable.silicate,
                R.drawable.silicate,
                R.drawable.silicate
        };

        MessageAdapter adapter = new MessageAdapter(userNames, lastMessages, times, avatarResIds);
        recyclerView.setAdapter(adapter);
    }
}
