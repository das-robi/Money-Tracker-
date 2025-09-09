package com.devrobin.moneytracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devrobin.moneytracker.MVVM.Model.CategoryModel;

import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.databinding.ActivityCategorySettingsBinding;
import com.devrobin.moneytracker.databinding.CategoryListItemsBinding;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private onItemClickListener listener;
    private ArrayList<CategoryModel> categoryList;
    private Context context;

    public CategoryAdapter(Context context, ArrayList<CategoryModel> categoryList, onItemClickListener listener) {
        this.listener = listener;
        this.categoryList = categoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            CategoryListItemsBinding listItemsBinding = CategoryListItemsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CategoryViewHolder(listItemsBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {

        CategoryModel cgtoryModel = categoryList.get(position);

        String name = cgtoryModel != null ? cgtoryModel.getCategoryName() : null;
        holder.listItemsBinding.categoryName.setText(name == null ? "" : name);

        // Handle icon ID safely with fallback
        int iconId = cgtoryModel.getIconId();
        try {
            holder.listItemsBinding.categoryIcons.setImageResource(iconId);
        } catch (Exception e) {
            // Fallback to default food icon if icon ID is invalid
            holder.listItemsBinding.categoryIcons.setImageResource(R.drawable.others);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(cgtoryModel);
                holder.listItemsBinding.categoryIcons.setBackgroundTintList(context.getColorStateList(R.color.black));
            }
        });

    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder{

        private CategoryListItemsBinding listItemsBinding;

        public CategoryViewHolder(@NonNull CategoryListItemsBinding listItemsBinding) {
            super(listItemsBinding.getRoot());

            this.listItemsBinding = listItemsBinding;

            listItemsBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int clickPosition = getAdapterPosition();

                    if (listener != null && clickPosition != RecyclerView.NO_POSITION){
                        listener.onItemClick(categoryList.get(clickPosition));
                    }

                }
            });

        }
    }



    public interface onItemClickListener{

        void onItemClick(CategoryModel category);

    }

    public void setListener(onItemClickListener listener) {
        this.listener = listener;
    }

    public void setCategoryList(ArrayList<CategoryModel> categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

}
