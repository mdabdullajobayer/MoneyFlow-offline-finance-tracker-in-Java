package com.example.personalfinanceexpensetrackerandroidapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Category;

import java.util.List;
import java.util.function.Consumer;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<Category> categories;
    private final Consumer<Category> onEditClick;
    private final Consumer<Category> onDeleteClick;

    public CategoryAdapter(List<Category> categories, Consumer<Category> onEditClick, Consumer<Category> onDeleteClick) {
        this.categories = categories;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    public void updateData(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_management_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.tvTitle.setText(category.getName());
        String ownerText = category.getOwnerId() <= 0 ? "Global" : "Owner ID: " + category.getOwnerId();
        holder.tvSubtitle.setText("Type: " + category.getType() + " • " + ownerText + (category.isActive() ? "" : " (Inactive)"));
        holder.ivIcon.setImageResource(android.R.drawable.ic_menu_sort_by_size);

        holder.btnEdit.setOnClickListener(v -> onEditClick.accept(category));
        holder.btnDelete.setOnClickListener(v -> onDeleteClick.accept(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvSubtitle;
        final View btnEdit;
        final View btnDelete;

        public ViewHolder(@NonNull View view) {
            super(view);
            ivIcon = view.findViewById(R.id.ivIcon);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvSubtitle = view.findViewById(R.id.tvSubtitle);
            btnEdit = view.findViewById(R.id.btnEdit);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
}
