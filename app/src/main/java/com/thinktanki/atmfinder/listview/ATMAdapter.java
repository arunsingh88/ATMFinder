package com.thinktanki.atmfinder.listview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thinktanki.atmfinder.R;
import com.thinktanki.atmfinder.atm.ATM;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aruns512 on 12/12/2016.
 */

public class ATMAdapter  extends RecyclerView.Adapter<ATMAdapter.MyViewHolder> {
    private List<ATM> atmList;

    public ATMAdapter(List<ATM> atmList) {
        this.atmList = atmList;
    }

    public void setFilter(List<ATM> atmLists) {
        atmList = new ArrayList<>();
        atmList.addAll(atmLists);
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.atm_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ATM atmObj = atmList.get(position);
        holder.atmName.setText(atmObj.getAtmName());
        holder.distance.setText(atmObj.getDistance());
        holder.atmAddress.setText(atmObj.getAtmAddress());
    }

    @Override
    public int getItemCount() {
        return atmList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView atmName, distance, atmAddress;

        public MyViewHolder(View view) {
            super(view);
            atmName = (TextView) view.findViewById(R.id.atmName);
            distance = (TextView) view.findViewById(R.id.distance);
            atmAddress = (TextView) view.findViewById(R.id.atmAddress);
        }
    }
}
