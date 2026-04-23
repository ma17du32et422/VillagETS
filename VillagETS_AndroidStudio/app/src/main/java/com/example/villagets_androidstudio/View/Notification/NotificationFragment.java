package com.example.villagets_androidstudio.View.Notification;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.villagets_androidstudio.R;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;

    public NotificationFragment() {
        super(R.layout.fragment_notification);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewNotifications);

        // placeholders
        String[] titles = {
                "Nouvel article",
                "Message reçu",
                "Vendu !",
                "Alerte prix",
                "Mise à jour",
                "Rappel",
                "Offre spéciale",
                "Bienvenue"
        };
        
        String[] descriptions = {
                "Un nouvel article correspondant à vos recherches est disponible.",
                "Vous avez reçu un nouveau message de Thomas.",
                "Votre Calculatrice TI-Nspire a été vendue.",
                "Le prix de 'Livre Physique' a baissé de 10%.",
                "Une nouvelle version de l'application est disponible.",
                "N'oubliez pas de noter votre dernier vendeur.",
                "Profitez de -15% sur les fournitures ce weekend.",
                "Bienvenue sur VillagETS ! Complétez votre profil."
        };
        
        String[] times = {
                "Il y a 5 min",
                "Il y a 1 heure",
                "Il y a 3 heures",
                "Hier",
                "Il y a 2 jours",
                "Il y a 3 jours",
                "La semaine dernière",
                "Il y a 2 semaines"
        };

        NotificationAdapter adapter = new NotificationAdapter(titles, descriptions, times);
        recyclerView.setAdapter(adapter);
    }
}
