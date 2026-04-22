package com.example.villagets_androidstudio;

import android.app.Application;
import com.giphy.sdk.ui.Giphy;

public class VillagETSApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialisation de Giphy avec la clé API fournie
        Giphy.INSTANCE.configure(this, "VcAVY8pswkRIjczWLQEypf99BXBlzZAw");
    }
}
