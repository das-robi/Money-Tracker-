package com.devrobin.moneytracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devrobin.moneytracker.MVVM.Model.CategoryModel;
import com.devrobin.moneytracker.R;
import com.devrobin.moneytracker.databinding.CategoryListItemsBinding;
import com.devrobin.moneytracker.databinding.CategorySettingLayouttBinding;

import java.util.ArrayList;

public class CategorySettingsAdapter extends RecyclerView.Adapter<CategorySettingsAdapter.CategoryViewHolder> {

    private Context context;
    private ArrayList<CategoryModel> categoryList;

    private onCategoryItemClickListener itemClickListener;
    private onEditClickListener editClickListener;
    private onDeleteClickListener deleteClickListener;

    public CategorySettingsAdapter(Context context, ArrayList<CategoryModel> categoryList, onCategoryItemClickListener itemClickListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.itemClickListener = itemClickListener;
    }

    public void setEditClickListener(onEditClickListener editClickListener) {
        this.editClickListener = editClickListener;
    }

    public void setDeleteClickListener(onDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CategorySettingLayouttBinding categoryBinding = CategorySettingLayouttBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(categoryBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel categoryModel = categoryList.get(position);

        String name = categoryModel != null ? categoryModel.getCategoryName() : null;
        holder.categoryBinding.categoryName.setText(name == null ? "" : name);

        // Set category icon
        setCategoryIcon(holder, categoryModel.getIconId());

        // Set button visibility based on whether it's a default category
        if (categoryModel.isDefault()) {
            // Default categories: only show delete button, hide edit button
            holder.categoryBinding.editBTN.setVisibility(View.GONE);
            holder.categoryBinding.deleteBTN.setVisibility(View.VISIBLE);
        } else {
            // User-created categories: show both edit and delete buttons
            holder.categoryBinding.editBTN.setVisibility(View.VISIBLE);
            holder.categoryBinding.deleteBTN.setVisibility(View.VISIBLE);
        }
        // Category item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (itemClickListener != null){
                    itemClickListener.categoryItemClick(categoryModel);
                }
            }
        });

        // Category item click
        holder.categoryBinding.editBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editClickListener != null){
                    editClickListener.onEditClick(categoryModel);
                }
            }
        });

        // Delete button click
        holder.categoryBinding.deleteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(categoryModel);
                }

            }
        });


    }

    private void setCategoryIcon(CategoryViewHolder holder, int iconId) {
        // Use the iconId directly as it's now the actual R.drawable resource ID
        try {
            holder.categoryBinding.categoryIcons.setImageResource(iconId);
        } catch (Exception e) {
            // Fallback to default food icon if icon ID is invalid
            holder.categoryBinding.categoryIcons.setImageResource(R.drawable.others);
        }
    }


    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        CategorySettingLayouttBinding categoryBinding;

        public CategoryViewHolder(@NonNull CategorySettingLayouttBinding categoryBinding) {
            super(categoryBinding.getRoot());
            this.categoryBinding = categoryBinding;
        }
    }


    public interface onCategoryItemClickListener {
        void categoryItemClick(CategoryModel categoryModel);
    }

    public interface onEditClickListener {
        void onEditClick(CategoryModel categoryModel);
    }

    public interface onDeleteClickListener {
        void onDeleteClick(CategoryModel categoryModel);
    }

    public void setCategoryList (ArrayList < CategoryModel > categoryList) {
        this.categoryList = categoryList;
        notifyDataSetChanged();
    }

}