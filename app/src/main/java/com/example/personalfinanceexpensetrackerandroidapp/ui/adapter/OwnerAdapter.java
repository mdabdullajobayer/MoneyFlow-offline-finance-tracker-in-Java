package com.example.personalfinanceexpensetrackerandroidapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Owner;

import java.util.List;
import java.util.function.Consumer;

public class OwnerAdapter extends RecyclerView.Adapter<OwnerAdapter.ViewHolder> {
    private List<Owner> owners;
    private final Consumer<Owner> onEditClick;
    private final Consumer<Owner> onDeleteClick;

    public OwnerAdapter(List<Owner> owners, Consumer<Owner> onEditClick, Consumer<Owner> onDeleteClick) {
        this.owners = owners;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    public void updateData(List<Owner> newOwners) {
        this.owners = newOwners;
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
        Owner owner = owners.get(position);
        holder.tvTitle.setText(owner.getName());
        holder.tvSubtitle.setText("Type: " + owner.getType() + " • " + (owner.isDefault() ? "Default" : "") + (owner.isActive() ? "" : " (Inactive)"));
        holder.ivIcon.setImageResource(android.R.drawable.ic_menu_myplaces);

        holder.btnEdit.setOnClickListener(v -> onEditClick.accept(owner));
        holder.btnDelete.setOnClickListener(v -> onDeleteClick.accept(owner));
    }

    @Override
    public int getItemCount() {
        return owners.size();
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
