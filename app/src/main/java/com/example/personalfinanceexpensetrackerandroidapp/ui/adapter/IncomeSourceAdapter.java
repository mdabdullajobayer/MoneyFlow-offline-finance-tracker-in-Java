package com.example.personalfinanceexpensetrackerandroidapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.IncomeSource;

import java.util.List;
import java.util.function.Consumer;

public class IncomeSourceAdapter extends RecyclerView.Adapter<IncomeSourceAdapter.ViewHolder> {
    private List<IncomeSource> sources;
    private final Consumer<IncomeSource> onEditClick;
    private final Consumer<IncomeSource> onDeleteClick;

    public IncomeSourceAdapter(List<IncomeSource> sources, Consumer<IncomeSource> onEditClick, Consumer<IncomeSource> onDeleteClick) {
        this.sources = sources;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    public void updateData(List<IncomeSource> newSources) {
        this.sources = newSources;
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
        IncomeSource source = sources.get(position);
        holder.tvTitle.setText(source.getName());
        String ownerText = source.getOwnerId() <= 0 ? "Global" : "Owner ID: " + source.getOwnerId();
        holder.tvSubtitle.setText(ownerText + (source.isActive() ? "" : " (Inactive)"));
        holder.ivIcon.setImageResource(android.R.drawable.ic_menu_upload);

        holder.btnEdit.setOnClickListener(v -> onEditClick.accept(source));
        holder.btnDelete.setOnClickListener(v -> onDeleteClick.accept(source));
    }

    @Override
    public int getItemCount() {
        return sources.size();
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
