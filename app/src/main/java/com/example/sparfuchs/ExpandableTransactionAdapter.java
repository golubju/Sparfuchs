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

    public ExpandableTransactionAdapter(Map<String, List<TransactionEntity>> groupedTransactions) {
        this.groupedTransactions = groupedTransactions;
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
        holder.transactionsList.setAdapter(new TransactionAdapter(transactions));

        // Sichtbarkeit setzen
        if (expandedCategories.contains(category)) {
            holder.transactionsList.setVisibility(View.VISIBLE);
        } else {
            holder.transactionsList.setVisibility(View.GONE);
        }

        // Klick-Listener für das Ein-/Ausklappen
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
