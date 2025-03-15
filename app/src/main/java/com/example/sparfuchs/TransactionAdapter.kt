package com.example.sparfuchs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sparfuchs.backend.TransactionEntity

class TransactionAdapter(
    private val transactions: List<TransactionEntity>,
    private val listener: OnTransactionClickListener // <-- Listener hinzufügen
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    interface OnTransactionClickListener {
        fun onTransactionClick(transaction: TransactionEntity)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.transaction_date)
        val description: TextView = view.findViewById(R.id.transaction_description)
        val amount: TextView = view.findViewById(R.id.transaction_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.date.text = transaction.date
        holder.description.text = transaction.description
        holder.amount.text = "${transaction.amount} €"

        holder.itemView.setOnClickListener {
            listener.onTransactionClick(transaction)
        }
    }

    override fun getItemCount() = transactions.size
}
