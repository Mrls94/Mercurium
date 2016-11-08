package com.example.mbalza.mercurium;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by mbalza on 10/31/16.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    ArrayList<String> mDataSet;

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_layout, parent, false);

        ViewHolder vh = new ViewHolder((CardView) v);
        return vh;


    }

    public MyAdapter (ArrayList<String> data)
    {
        mDataSet = data;
        System.out.println(mDataSet.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public CardView mcardview;
        public TextView textview;
        public ViewHolder(View v) {
            super(v);
            textview = (TextView) v.findViewById(R.id.cardText);

        }
    }

    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {

        holder.textview.setText(mDataSet.get(position));
        System.out.println(mDataSet.get(position));

    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

}
