package com.example.villagets_androidstudio.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.villagets_androidstudio.R;

public class MarketPlaceManager extends BaseAdapter {

    private Context context;
    private String[] names;
    private String[] prices;

    public MarketPlaceManager(Context context, String[] names, String[] prices) {
        this.context = context;
        this.names = names;
        this.prices = prices;
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return names[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_marketplace, parent, false);
        }

        TextView name = convertView.findViewById(R.id.itemName);
        TextView price = convertView.findViewById(R.id.itemPrice);

        name.setText(names[position]);
        price.setText(prices[position]);

        return convertView;
    }
}