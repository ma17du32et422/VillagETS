package com.example.villagets_androidstudio;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyViewPagerAdapter extends FragmentStateAdapter {
    public MyViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new FeedFragment();
            case 1:
                return new MarketPlaceFragment();
        }
        return new FeedFragment();
    }

    @Override
    public int getItemCount() {
        return 5; // Nombre total de pages correspondant à tes 5 boutons du bas
    }
}