package com.example.personalfinanceexpensetrackerandroidapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Account;

import java.util.List;
import java.util.function.Consumer;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {
    private List<Account> accounts;
    private final Consumer<Account> onEditClick;
    private final Consumer<Account> onDeleteClick;

    public AccountAdapter(List<Account> accounts, Consumer<Account> onEditClick, Consumer<Account> onDeleteClick) {
        this.accounts = accounts;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    public void updateData(List<Account> newAccounts) {
        this.accounts = newAccounts;
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
        Account account = accounts.get(position);
        holder.tvTitle.setText(account.getName());
        holder.tvSubtitle.setText(account.getType() + " • Balance: " + account.getCurrency() + " " + account.getBalance() + (account.isActive() ? "" : " (Inactive)"));
        holder.ivIcon.setImageResource(android.R.drawable.ic_menu_agenda);

        holder.btnEdit.setOnClickListener(v -> onEditClick.accept(account));
        holder.btnDelete.setOnClickListener(v -> onDeleteClick.accept(account));
    }

    @Override
    public int getItemCount() {
        return accounts.size();
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
