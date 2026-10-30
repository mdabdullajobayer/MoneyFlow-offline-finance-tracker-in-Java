package com.example.personalfinanceexpensetrackerandroidapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.SmsSender;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SmsSenderAdapter extends RecyclerView.Adapter<SmsSenderAdapter.ViewHolder> {
    private List<SmsSender> senders;
    private final BiConsumer<SmsSender, Boolean> onToggleActive;
    private final Consumer<SmsSender> onEditClick;
    private final Consumer<SmsSender> onDeleteClick;
    private final Consumer<SmsSender> onAddAsAccountClick;

    public SmsSenderAdapter(List<SmsSender> senders,
                            BiConsumer<SmsSender, Boolean> onToggleActive,
                            Consumer<SmsSender> onEditClick,
                            Consumer<SmsSender> onDeleteClick,
                            Consumer<SmsSender> onAddAsAccountClick) {
        this.senders = senders;
        this.onToggleActive = onToggleActive;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
        this.onAddAsAccountClick = onAddAsAccountClick;
    }

    public void updateData(List<SmsSender> newSenders) {
        this.senders = newSenders;
        notifyDataSetChanged();
    }

    public List<SmsSender> getSenders() {
        return senders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sms_sender, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SmsSender sender = senders.get(position);
        holder.tvTitle.setText(sender.getSenderName());
        holder.tvSubtitle.setText("Keyword: " + (sender.getKeywordFilter() != null && !sender.getKeywordFilter().isEmpty() ? sender.getKeywordFilter() : "None"));
        String accountName = sender.getAccountName();
        if (accountName == null || accountName.isEmpty()) {
            accountName = "None (ID " + sender.getAccountId() + ")";
        }
        holder.tvAccount.setText("Linked Account: " + accountName);
        holder.ivIcon.setImageResource(android.R.drawable.ic_dialog_email);

        holder.switchActive.setOnCheckedChangeListener(null);
        holder.switchActive.setChecked(sender.isActive());
        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked != sender.isActive()) {
                onToggleActive.accept(sender, isChecked);
            }
        });

        holder.btnAddAsAccount.setOnClickListener(v -> onAddAsAccountClick.accept(sender));
        holder.btnEdit.setOnClickListener(v -> onEditClick.accept(sender));
        holder.btnDelete.setOnClickListener(v -> onDeleteClick.accept(sender));
    }

    @Override
    public int getItemCount() {
        return senders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvSubtitle;
        final TextView tvAccount;
        final SwitchMaterial switchActive;
        final View btnAddAsAccount;
        final View btnEdit;
        final View btnDelete;

        public ViewHolder(@NonNull View view) {
            super(view);
            ivIcon = view.findViewById(R.id.ivIcon);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvSubtitle = view.findViewById(R.id.tvSubtitle);
            tvAccount = view.findViewById(R.id.tvAccount);
            switchActive = view.findViewById(R.id.switchActive);
            btnAddAsAccount = view.findViewById(R.id.btnAddAsAccount);
            btnEdit = view.findViewById(R.id.btnEdit);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
}
