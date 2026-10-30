package com.example.personalfinanceexpensetrackerandroidapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.PaymentMethod;

import java.util.List;
import java.util.function.Consumer;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.ViewHolder> {
    private List<PaymentMethod> methods;
    private final Consumer<PaymentMethod> onEditClick;
    private final Consumer<PaymentMethod> onDeleteClick;

    public PaymentMethodAdapter(List<PaymentMethod> methods, Consumer<PaymentMethod> onEditClick, Consumer<PaymentMethod> onDeleteClick) {
        this.methods = methods;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    public void updateData(List<PaymentMethod> newMethods) {
        this.methods = newMethods;
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
        PaymentMethod method = methods.get(position);
        holder.tvTitle.setText(method.getName());
        holder.tvSubtitle.setText("Payment Method" + (method.isActive() ? "" : " (Inactive)"));
        holder.ivIcon.setImageResource(android.R.drawable.ic_menu_send);

        holder.btnEdit.setOnClickListener(v -> onEditClick.accept(method));
        holder.btnDelete.setOnClickListener(v -> onDeleteClick.accept(method));
    }

    @Override
    public int getItemCount() {
        return methods.size();
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
