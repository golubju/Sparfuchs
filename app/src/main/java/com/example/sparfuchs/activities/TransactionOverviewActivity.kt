package com.example.sparfuchs.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sparfuchs.ExpandableTransactionAdapter
import com.example.sparfuchs.R
import com.example.sparfuchs.backend.AppDatabase
import com.example.sparfuchs.backend.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionOverviewActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpandableTransactionAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_overview)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db = AppDatabase.getInstance(this)

        refreshTransactions()

        // Button zum Löschen aller Transaktionen
        findViewById<Button>(R.id.btn_delete_all).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                db.transactionDao().deleteAll()
                withContext(Dispatchers.Main) {
                    refreshTransactions()
                }
            }
        }

        // Button zum Hinzufügen einer neuen Transaktion
        findViewById<Button>(R.id.btn_add_transaction).setOnClickListener {
            // Pass null for transaction to indicate it's a new transaction
            showTransactionDialog(null)
        }
    }

    private fun refreshTransactions() {
        lifecycleScope.launch(Dispatchers.IO) {
            val transactions = db.transactionDao().getAllTransactions()
            val groupedTransactions = transactions.groupBy { it.category }

            withContext(Dispatchers.Main) {
                adapter = ExpandableTransactionAdapter(groupedTransactions) { transaction ->
                    showTransactionDialog(transaction)
                }
                recyclerView.adapter = adapter
            }
        }
    }


    private fun showTransactionDialog(transaction: TransactionEntity?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_transaction, null)
        val editAmount = dialogView.findViewById<EditText>(R.id.editAmount)
        val editDescription = dialogView.findViewById<EditText>(R.id.editDescription)
        val editDate = dialogView.findViewById<EditText>(R.id.editDate)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        // Observe categories to populate the spinner
        db.categoryDao().getAllCategories().observe(this) { categories ->
            val categoryNames = categories.map { it.name }
            val categoryAdapter = ArrayAdapter(this@TransactionOverviewActivity, android.R.layout.simple_spinner_item, categoryNames)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = categoryAdapter

            // If it's an existing transaction, set the current category
            transaction?.let {
                editAmount.setText(it.amount.toString())
                editDescription.setText(it.description)
                editDate.setText(it.date)  // Set the current date of the transaction
                val categoryPosition = categoryNames.indexOf(it.category)
                spinnerCategory.setSelection(categoryPosition)
            }

            // Show the dialog
            AlertDialog.Builder(this)
                .setTitle(if (transaction == null) "Neue Transaktion hinzufügen" else "Transaktion bearbeiten")
                .setView(dialogView)
                .setPositiveButton("Speichern") { _, _ ->
                    val newAmount = editAmount.text.toString().toDoubleOrNull()
                    val newDescription = editDescription.text.toString()
                    val newCategory = spinnerCategory.selectedItem.toString()
                    val newDate = editDate.text.toString()

                    if (newAmount != null && newDate.isNotEmpty()) {
                        if (transaction == null) {
                            // Create a new transaction
                            val newTransaction = TransactionEntity()
                            newTransaction.date = newDate
                            newTransaction.description = newDescription
                            newTransaction.amount = newAmount
                            newTransaction.category = newCategory
                            addNewTransaction(newTransaction)
                        } else {
                            // Edit existing transaction
                            transaction.amount = newAmount
                            transaction.description = newDescription
                            transaction.category = newCategory
                            transaction.date = newDate
                            updateTransaction(transaction) // Update transaction in DB
                        }
                    } else {
                        Toast.makeText(this@TransactionOverviewActivity, "Ungültiger Betrag oder Datum", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Abbrechen", null)
                .show()
        }
    }


    // Add new transaction to the database
    private fun addNewTransaction(transaction: TransactionEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.transactionDao().insert(transaction)  // Insert the new transaction
            withContext(Dispatchers.Main) {
                refreshTransactions()  // Refresh the list of transactions
            }
        }
    }

    // Update an existing transaction
    private fun updateTransaction(transaction: TransactionEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.transactionDao().update(transaction)
            withContext(Dispatchers.Main) {
                refreshTransactions()
            }
        }
    }
}

