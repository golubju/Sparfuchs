package com.example.sparfuchs.activities

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sparfuchs.ExpandableTransactionAdapter
import com.example.sparfuchs.R
import com.example.sparfuchs.backend.*
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
        findViewById<Button>(R.id.btn_delete_all).setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                db.transactionDao().deleteAll()
                withContext(Dispatchers.Main) {
                    refreshTransactions()
                }
            }
        }
        findViewById<Button>(R.id.btn_add_transaction).setOnClickListener {
            showTransactionDialog(null)
        }
        findViewById<Button>(R.id.check_others).setOnClickListener {
            println("Prüfen")
            checkTransactions()
        }
    }

    private fun checkTransactions() {

        val db = AppDatabase.getInstance(this)
        val transactionDao = db.transactionDao()
        val categoryDao = db.categoryDao()
        val categoriesLiveData = getDefaultCategories(categoryDao)
        val categoriesList = mutableListOf<Category>()

        categoriesLiveData.observeForever { categories ->
            categoriesList.clear()
            categoriesList.addAll(categories)
            val transactionAnalyser = TransactionAnalyser(categoriesList)
            println(categoriesList.size)

            lifecycleScope.launch(Dispatchers.IO) {
                val existingTransactions = transactionDao.getAllTransactions()

                val sonstiTransactions = existingTransactions.filter { it.category == "Sonstiges" }

                sonstiTransactions.forEach { transaction ->
                    val newCategory = transactionAnalyser.getCategory(transaction)
                    if (transaction.category != newCategory.name) {
                        transaction.category = newCategory.name
                        println(transaction.toString())
                        println(newCategory.name)

                        transactionDao.update(transaction)
                    }
                }
            }
        }
        refreshTransactions()
    }

    private fun getDefaultCategories(categoryDao: CategoryDao): LiveData<List<Category>> {
        val liveData = MediatorLiveData<List<Category>>()
        liveData.addSource(categoryDao.getAllCategories()) { categoryEntities ->
            liveData.value = categoryEntities.map { entity ->
                Category(
                    entity.name,
                    entity.keywords.split(",").map { it.trim() }
                )
            }
        }
        liveData.observeForever { categories ->
            categories?.let {
                val categoryNames = it.joinToString(", ") { category -> category.name }
                println("Categories fetched: $categoryNames")
            } ?: println("No categories found.")
        }
        return liveData
    }

    private fun refreshTransactions() {
        lifecycleScope.launch(Dispatchers.IO) {
            val transactions = db.transactionDao().getAllTransactions()
            val groupedTransactions = transactions.groupBy { it.category }
            val categorySums = groupedTransactions.mapValues { entry ->
                entry.value.sumOf { it.amount }
            }

            withContext(Dispatchers.Main) {
                adapter = ExpandableTransactionAdapter(groupedTransactions, categorySums) { transaction ->
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
        db.categoryDao().getAllCategories().observe(this) { categories ->
            val categoryNames = categories.map { it.name }
            val categoryAdapter = ArrayAdapter(this@TransactionOverviewActivity, android.R.layout.simple_spinner_item, categoryNames)
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = categoryAdapter
            transaction?.let { it ->
                editAmount.setText(it.amount.toString())
                editDescription.setText(it.description)
                editDate.setText(it.date)  // Set the current date of the transaction
                val categoryPosition = categoryNames.indexOf(it.category)
                spinnerCategory.setSelection(categoryPosition)
            }

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
                            val newTransaction = TransactionEntity()
                            newTransaction.date = newDate
                            newTransaction.description = newDescription
                            newTransaction.amount = newAmount
                            newTransaction.category = newCategory
                            addNewTransaction(newTransaction)
                        } else {
                            transaction.amount = newAmount
                            transaction.description = newDescription
                            transaction.category = newCategory
                            transaction.date = newDate
                            updateTransaction(transaction)
                        }
                    } else {
                        Toast.makeText(this@TransactionOverviewActivity, "Ungültiger Betrag oder Datum", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Abbrechen", null)
                .show()
        }
    }
    private fun addNewTransaction(transaction: TransactionEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.transactionDao().insert(transaction)
            withContext(Dispatchers.Main) {
                refreshTransactions()
            }
        }
    }

    private fun updateTransaction(transaction: TransactionEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.transactionDao().update(transaction)
            withContext(Dispatchers.Main) {
                refreshTransactions()
            }
        }
    }
}

