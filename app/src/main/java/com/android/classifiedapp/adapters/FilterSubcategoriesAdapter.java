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
import com.android.classifiedapp.models.SubCategory;
import com.blankj.utilcode.util.LogUtils;

import java.util.List;

public class FilterSubcategoriesAdapter extends RecyclerView.Adapter<FilterSubcategoriesAdapter.ViewHolder> {
    Context context;
    List<SubCategory> subCategories;
    FilterSubcategoriesAdapterListener listener;
    int selectedPos;

    public FilterSubcategoriesAdapter(Context context, List<SubCategory> subCategories, FilterSubcategoriesAdapterListener listener) {
        this.context = context;
        this.subCategories = subCategories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_category,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SubCategory subCategory = subCategories.get(position);
        holder.tvCategory.setText(subCategory.getName());
        LogUtils.e(subCategory.getName());

        if (subCategory.isSelected()){
            holder.vgParent.setBackgroundResource(R.drawable.bg_selected_item);
        }else{
            holder.vgParent.setBackgroundResource(R.drawable.btn_sign_in_opts);
        }

        int p = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subCategories.get(selectedPos).setSelected(false);
                subCategories.get(p).setSelected(true);
                holder.vgParent.setBackgroundResource(R.drawable.bg_selected_item);
                listener.onSubcategorySelected(subCategory);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subCategories.size();
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
    public interface FilterSubcategoriesAdapterListener{
        void onSubcategorySelected(SubCategory subCategory);
    }
}
