package com.example.villagets_androidstudio.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.villagets_androidstudio.View.Feed.FeedFragment;
import com.example.villagets_androidstudio.View.Feed.MarketPlaceFragment;
import com.example.villagets_androidstudio.View.Message.ConversationFragment;
import com.example.villagets_androidstudio.View.Search.SearchFragment;

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
            case 2:
                return new SearchFragment();
            case 3:
                return new ConversationFragment();
        }
        return new FeedFragment();
    }

    @Override
    public int getItemCount() {
        return 4; // Nombre total de pages correspondant à tes 4 boutons du bas
    }
}