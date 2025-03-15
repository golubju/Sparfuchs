package com.example.sparfuchs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sparfuchs.backend.TransactionEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class ExpandableTransactionAdapter extends RecyclerView.Adapter<ExpandableTransactionAdapter.CategoryViewHolder> {
    private final Map<String, List<TransactionEntity>> groupedTransactions;
    private final Set<String> expandedCategories = new HashSet<>();
    private final TransactionAdapter.OnTransactionClickListener onTransactionClickListener;  // Use TransactionAdapter's listener

    public interface OnTransactionClickListener extends TransactionAdapter.OnTransactionClickListener {
    }

    public ExpandableTransactionAdapter(Map<String, List<TransactionEntity>> groupedTransactions, OnTransactionClickListener listener) {
        this.groupedTransactions = groupedTransactions;
        this.onTransactionClickListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = (String) groupedTransactions.keySet().toArray()[position];
        List<TransactionEntity> transactions = groupedTransactions.get(category);

        holder.categoryTitle.setText(category);
        holder.transactionsList.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));

        // Pass the onTransactionClickListener to TransactionAdapter
        TransactionAdapter transactionAdapter = new TransactionAdapter(transactions, onTransactionClickListener);
        holder.transactionsList.setAdapter(transactionAdapter);

        holder.transactionsList.setVisibility(expandedCategories.contains(category) ? View.VISIBLE : View.GONE);

        holder.categoryTitle.setOnClickListener(v -> {
            if (expandedCategories.contains(category)) {
                expandedCategories.remove(category);
            } else {
                expandedCategories.add(category);
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return groupedTransactions.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        RecyclerView transactionsList;

        public CategoryViewHolder(View view) {
            super(view);
            categoryTitle = view.findViewById(R.id.category_title);
            transactionsList = view.findViewById(R.id.transactions_list);
        }
    }
}

