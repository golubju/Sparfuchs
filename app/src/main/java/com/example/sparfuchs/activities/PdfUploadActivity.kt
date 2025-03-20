package com.example.sparfuchs.activities

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.sparfuchs.R
import com.example.sparfuchs.backend.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch




class PdfUploadActivity : AppCompatActivity() {

    private lateinit var txtFileName: TextView
    private lateinit var bankSpinner: Spinner
    private lateinit var btnUpload: Button
    private lateinit var transactionAnalyser: TransactionAnalyser

    private val getPdfLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { processPdfUri(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdfupload)

        txtFileName = findViewById(R.id.txtFileName)
        bankSpinner = findViewById(R.id.spinnerBanks)
        btnUpload = findViewById(R.id.btnUpload)



        val bankList = listOf("Bitte wählen", "Deutsche Bank", "Sparkasse", "Commerzbank", "ING", "DKB")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bankList)
        bankSpinner.adapter = adapter

        btnUpload.setOnClickListener {
            val selectedBank = bankSpinner.selectedItem.toString()
            if (selectedBank == "Bitte wählen") {
                Toast.makeText(this, "Bitte eine Bank auswählen!", Toast.LENGTH_SHORT).show()
            } else {
                getPdfLauncher.launch("application/pdf")
            }
        }
    }

    private fun processPdfUri(uri: Uri) {
        val fileName = getFileNameFromUri(uri)
        txtFileName.text = "Ausgewählte Datei: $fileName"

        val selectedBank = bankSpinner.selectedItem.toString()
        if (selectedBank == "Bitte wählen") {
            Toast.makeText(this, "Bitte eine Bank auswählen!", Toast.LENGTH_SHORT).show()
            return
        }

        parsePdf(uri, this, selectedBank)
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("_display_name")
            if (cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return "Unbekannte Datei"
    }

    private fun parsePdf(uri: Uri, context: Context, bank: String) {
        try {
            val transactions: List<TransactionEntity> = PdfParser.extractTransactionsFromPDF(context, uri, bank)

            if (transactions.isEmpty()) {
                Toast.makeText(this, "Keine Transaktionen gefunden.", Toast.LENGTH_SHORT).show()
                return
            }

            val db = AppDatabase.getInstance(context)
            val transactionDao = db.transactionDao()
            val categoryDao = db.categoryDao()
            val categoriesLiveData = getDefaultCategories(categoryDao)
            val categoriesList = mutableListOf<Category>()

            categoriesLiveData.observeForever { categories ->
                categoriesList.clear()
                categoriesList.addAll(categories)
                transactionAnalyser = TransactionAnalyser(categoriesList)

                lifecycleScope.launch(Dispatchers.IO) {
                    val existingTransactions = transactionDao.getAllTransactions()
                    val newTransactions = transactions.filter { newTransaction ->
                        existingTransactions.none { existingTransaction ->
                            existingTransaction.date == newTransaction.date &&
                                    existingTransaction.amount == newTransaction.amount &&
                                    existingTransaction.description == newTransaction.description
                        }
                    }

                    newTransactions.forEach { transaction ->
                        val category = transactionAnalyser.getCategory(transaction)
                        transaction.category = category.name
                    }

                    if (newTransactions.isNotEmpty()) {
                        transactionDao.insertAll(newTransactions)
                        runOnUiThread {
                            Toast.makeText(this@PdfUploadActivity, "${newTransactions.size} neue Transaktionen für $bank gespeichert!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@PdfUploadActivity, "Keine neuen Transaktionen zum Speichern.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Fehler beim Parsen des PDFs.", Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        }
    }


    private fun getDefaultCategories(categoryDao: CategoryDao): LiveData<List<Category>> {
        val liveData = MediatorLiveData<List<Category>>()

        // Beobachte die Kategorienquelle und aktualisiere liveData
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


}
