package com.android.classifiedapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.R;
import com.android.classifiedapp.models.SubCategory;

import java.util.List;

public class SubcategoriesRecyclerAdapter extends RecyclerView.Adapter<SubcategoriesRecyclerAdapter.SubcategoriesViewHolder> {
    List<SubCategory> subCategories;
    Context context;
    SubcategorySelectionListener subcategorySelectionListener;

    public SubcategoriesRecyclerAdapter(List<SubCategory> subCategories, Context context, SubcategorySelectionListener subcategorySelectionListener) {

        this.subCategories = subCategories;
        this.context = context;
        this.subcategorySelectionListener = subcategorySelectionListener;
    }

    @NonNull
    @Override
    public SubcategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SubcategoriesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_select,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull SubcategoriesViewHolder holder, int position) {
        SubCategory subCategory = subCategories.get(position);
        holder.tvCategory.setText(subCategory.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subcategorySelectionListener.onSubcategorySelected(subCategory);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subCategories.size();
    }

    class SubcategoriesViewHolder extends RecyclerView.ViewHolder{
        TextView tvCategory;
        public SubcategoriesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
        }
    }

    public interface SubcategorySelectionListener{
        public void onSubcategorySelected(SubCategory subCategory);
    }
}
