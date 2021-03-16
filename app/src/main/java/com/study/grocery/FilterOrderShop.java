package com.study.grocery;

import android.widget.Filter;

import com.study.grocery.adapter.AdapterOrderShop;
import com.study.grocery.adapter.AdapterProductSeller;
import com.study.grocery.models.ModelOrderShop;
import com.study.grocery.models.ModelProduct;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {

    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterList;


    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if (constraint != null && constraint.length()>0){
            //search filed not empty, searching something ,perform search

            //change to uppercase,to make sentence insensitive
            constraint = constraint.toString().toUpperCase();
            //store our filled list
            ArrayList<ModelOrderShop> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //check search by title and category
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(constraint)){
                    //add filtered data to list
                    filteredModels.add(filterList.get(i));

                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            //search filed  empty,not searching ,return original/all/complete list

            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.orderShopArrayList = (ArrayList<ModelOrderShop>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();

    }
}
