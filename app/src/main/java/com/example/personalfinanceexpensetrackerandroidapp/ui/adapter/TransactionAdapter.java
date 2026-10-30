package com.example.personalfinanceexpensetrackerandroidapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.Transaction;
import com.example.personalfinanceexpensetrackerandroidapp.util.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private List<Transaction> transactions;
    private final Consumer<Transaction> onItemClick;
    private final Consumer<Transaction> onDeleteClick;

    public TransactionAdapter(List<Transaction> transactions, Consumer<Transaction> onItemClick, Consumer<Transaction> onDeleteClick) {
        this.transactions = transactions;
        this.onItemClick = onItemClick;
        this.onDeleteClick = onDeleteClick;
    }

    public void updateData(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = transactions.get(position);
        String formattedAmount = CurrencyUtils.formatCents(holder.itemView.getContext(), tx.getAmount());
        String typeName = tx.getTransactionTypeName();
        if ("IN".equals(typeName)) {
            holder.tvAmount.setText("+" + formattedAmount);
            holder.tvAmount.setTextColor(0xFF2E7D32);
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_add);
        } else if ("OUT".equals(typeName)) {
            holder.tvAmount.setText("-" + formattedAmount);
            holder.tvAmount.setTextColor(0xFFC62828);
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_edit);
        } else {
            holder.tvAmount.setText(formattedAmount);
            holder.tvAmount.setTextColor(0xFF1565C0);
            holder.ivCategoryIcon.setImageResource(R.drawable.ic_report);
        }

        String title = tx.getCategoryName();
        if (title == null) {
            title = typeName != null ? typeName : "Transaction";
        }
        holder.tvTitle.setText(title);

        List<String> details = new ArrayList<>();
        if (tx.getAccountName() != null) {
            details.add("Acct: " + tx.getAccountName());
        }
        if (tx.getDestinationAccountName() != null) {
            details.add("To: " + tx.getDestinationAccountName());
        }
        if (tx.getOwnerName() != null) {
            details.add("Owner: " + tx.getOwnerName());
        }
        if (tx.getPayeePayer() != null) {
            details.add("Payer/Payee: " + tx.getPayeePayer());
        }
        if (tx.getNote() != null && !tx.getNote().isEmpty()) {
            details.add("Note: " + tx.getNote());
        }

        StringBuilder sbDetails = new StringBuilder();
        for (int i = 0; i < details.size(); i++) {
            if (i > 0) sbDetails.append(" • ");
            sbDetails.append(details.get(i));
        }
        holder.tvSubtitle.setText(sbDetails.toString());
        holder.tvDate.setText(tx.getDate());

        holder.itemView.setOnClickListener(v -> onItemClick.accept(tx));
        holder.btnDelete.setOnClickListener(v -> onDeleteClick.accept(tx));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvSubtitle;
        final TextView tvAmount;
        final TextView tvDate;
        final ImageView ivCategoryIcon;
        final View btnDelete;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvSubtitle = view.findViewById(R.id.tvSubtitle);
            tvAmount = view.findViewById(R.id.tvAmount);
            tvDate = view.findViewById(R.id.tvDate);
            ivCategoryIcon = view.findViewById(R.id.ivCategoryIcon);
            btnDelete = view.findViewById(R.id.btnDelete);
        }
    }
}
