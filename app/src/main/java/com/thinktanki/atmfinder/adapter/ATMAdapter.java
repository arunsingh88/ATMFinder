package com.thinktanki.atmfinder.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.thinktanki.atmfinder.atm.DetailActivity;
import com.thinktanki.atmfinder.R;
import com.thinktanki.atmfinder.atm.ATM;

import java.util.List;

/**
 * Created by aruns512 on 12/12/2016.
 */

public class ATMAdapter extends RecyclerView.Adapter<ATMAdapter.MyViewHolder> {
    private List<ATM> atmList;
    private Context context;
    private String type;

    public ATMAdapter(List<ATM> atmList, Context context, String type) {
        this.atmList = atmList;
        this.context = context;
        this.type = type;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (type.equalsIgnoreCase("atm")) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_atm_object, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bank_object, parent, false);

        }

        return new MyViewHolder(itemView, context);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ATM atmObj = atmList.get(position);
        holder.atmName.setText(atmObj.getAtmName());
        holder.distance.setText(atmObj.getDistance().toString() + " Kms");
        holder.atmAddress.setText(atmObj.getAtmAddress());
    }

    @Override
    public int getItemCount() {
        return atmList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView atmName, distance, atmAddress;
        private Intent intent;
        private Context context;

        public MyViewHolder(View view, Context context) {
            super(view);
            atmName = (TextView) view.findViewById(R.id.atmName);
            distance = (TextView) view.findViewById(R.id.distance);
            atmAddress = (TextView) view.findViewById(R.id.atmAddress);
            this.context = context;
            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            intent = new Intent(context, DetailActivity.class);

            intent.putExtra("ATM_NAME", atmList.get(getLayoutPosition()).getAtmName());
            intent.putExtra("ATM_ADDRESS", atmList.get(getLayoutPosition()).getAtmAddress());
            intent.putExtra("LATITUDE", atmList.get(getLayoutPosition()).getLatitude());
            intent.putExtra("LONGITUDE", atmList.get(getLayoutPosition()).getLongitude());
            intent.putExtra("DISTANCE", atmList.get(getLayoutPosition()).getDistance().toString());
            if(type.equalsIgnoreCase("atm")){
                intent.putExtra("TYPE","atm");
            }
            else
            {
                intent.putExtra("TYPE","bank");
            }
            context.startActivity(intent);


        }
    }
}
