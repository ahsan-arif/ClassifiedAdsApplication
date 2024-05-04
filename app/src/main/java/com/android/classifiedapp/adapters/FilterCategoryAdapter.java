package com.android.classifiedapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.classifiedapp.R;
import com.android.classifiedapp.models.Category;

import java.util.ArrayList;

public class FilterCategoryAdapter extends RecyclerView.Adapter<FilterCategoryAdapter.ViewHolder> {
Context context;
ArrayList<Category> categories;
FilterCategoriesAdapterListener listener;
int selectedPos;
    public FilterCategoryAdapter(Context context, ArrayList<Category> categories, FilterCategoriesAdapterListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_category,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvCategory.setText(category.getName());

        if (category.isSelected()){
            holder.vgParent.setBackgroundResource(R.drawable.bg_selected_item);
        }else{
            holder.vgParent.setBackgroundResource(R.drawable.btn_sign_in_opts);
        }
        int p = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categories.get(selectedPos).setSelected(false);
                categories.get(p).setSelected(true);
                listener.onCategorySelected(category);
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
TextView tvCategory;
RelativeLayout vgParent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_category);
            vgParent = itemView.findViewById(R.id.vg_parent);
        }
    }

    public interface FilterCategoriesAdapterListener{
        void onCategorySelected(Category category);
    }
}
