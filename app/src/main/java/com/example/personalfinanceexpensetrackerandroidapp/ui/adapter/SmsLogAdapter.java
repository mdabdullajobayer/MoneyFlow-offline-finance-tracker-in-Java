package com.example.personalfinanceexpensetrackerandroidapp.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalfinanceexpensetrackerandroidapp.R;
import com.example.personalfinanceexpensetrackerandroidapp.data.model.SmsLog;

import java.util.List;
import java.util.function.Consumer;

public class SmsLogAdapter extends RecyclerView.Adapter<SmsLogAdapter.ViewHolder> {
    private List<SmsLog> logs;
    private final Consumer<SmsLog> onApproveClick;
    private final Consumer<SmsLog> onEditClick;
    private final Consumer<SmsLog> onRejectClick;

    public SmsLogAdapter(List<SmsLog> logs, Consumer<SmsLog> onApproveClick, Consumer<SmsLog> onEditClick, Consumer<SmsLog> onRejectClick) {
        this.logs = logs;
        this.onApproveClick = onApproveClick;
        this.onEditClick = onEditClick;
        this.onRejectClick = onRejectClick;
    }

    public void updateData(List<SmsLog> newLogs) {
        this.logs = newLogs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sms_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SmsLog log = logs.get(position);
        holder.tvSender.setText("Sender: " + log.getSender() + " (" + log.getReceivedDate() + ")");
        holder.tvMessage.setText(log.getMessageBody());
        holder.tvDetails.setText("Type: " + log.getTransactionType() + " | Amount: " + log.getParsedAmount() + " | Ref: " + log.getReference() + " | Status: " + log.getStatus());

        boolean isPending = "PENDING".equals(log.getStatus());
        holder.btnApprove.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.btnEdit.setVisibility(isPending ? View.VISIBLE : View.GONE);
        holder.btnReject.setVisibility(isPending ? View.VISIBLE : View.GONE);

        holder.btnApprove.setOnClickListener(v -> onApproveClick.accept(log));
        holder.btnEdit.setOnClickListener(v -> onEditClick.accept(log));
        holder.btnReject.setOnClickListener(v -> onRejectClick.accept(log));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvSender;
        final TextView tvMessage;
        final TextView tvDetails;
        final Button btnApprove;
        final Button btnEdit;
        final Button btnReject;

        public ViewHolder(@NonNull View view) {
            super(view);
            tvSender = view.findViewById(R.id.tvSender);
            tvMessage = view.findViewById(R.id.tvMessage);
            tvDetails = view.findViewById(R.id.tvDetails);
            btnApprove = view.findViewById(R.id.btnApprove);
            btnEdit = view.findViewById(R.id.btnEdit);
            btnReject = view.findViewById(R.id.btnReject);
        }
    }
}
