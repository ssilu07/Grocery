package com.study.grocery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.study.grocery.R;
import com.study.grocery.models.ModelCartitem;
import com.study.grocery.models.ModelOrderUser;
import com.study.grocery.models.ModelOrdereditem;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem> {

    private Context context;
    private ArrayList<ModelOrdereditem> ordereditemArrayList;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrdereditem> ordereditemArrayList) {
        this.context = context;
        this.ordereditemArrayList = ordereditemArrayList;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordereditem,parent,false);
        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {

        //get data at position
        ModelOrdereditem modelOrdereditem = ordereditemArrayList.get(position);
        String getpId = modelOrdereditem.getpId();
        String name = modelOrdereditem.getName();
        String cost = modelOrdereditem.getCost();
        String price  = modelOrdereditem.getPrice();
        String quantity = modelOrdereditem.getQuantity();

        //set data
        holder.itemTitleTv.setText(name);
        holder.itemPriceEachTv.setText("$"+price);
        holder.itemPriceTv.setText("$"+cost);
        holder.itemQuantityTv.setText("["+ quantity +"]");

    }

    @Override
    public int getItemCount() {
        return ordereditemArrayList.size(); //return list size
    }

    //view holder class
    class HolderOrderedItem extends RecyclerView.ViewHolder{

        //views of row_ordereditem.xml
        private TextView itemTitleTv,itemPriceTv,itemPriceEachTv,itemQuantityTv;

        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);

            //init views
            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);

        }
    }
}

