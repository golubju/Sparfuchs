package com.example.sparfuchs

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.sparfuchs.backend.AppDatabase
import com.example.sparfuchs.backend.PdfParser
import com.example.sparfuchs.backend.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PdfUploadActivity : AppCompatActivity() {

    private val txtFileName: TextView by lazy { findViewById(R.id.txtFileName) }

    private val getPdfLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { processPdfUri(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdfupload)

        val btnUpload: Button = findViewById(R.id.btnUpload)

        btnUpload.setOnClickListener {
            getPdfLauncher.launch("application/pdf")
        }
    }

    private fun processPdfUri(uri: Uri) {
        val fileName = getFileNameFromUri(uri)
        txtFileName.text = "Ausgewählte Datei: $fileName"

        parsePdf(uri, this)
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = cursor.getColumnIndexOrThrow("_display_name")
            if (cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return null
    }

    private fun parsePdf(uri: Uri, context: Context) {
        try {
            val transactions: List<TransactionEntity> = PdfParser.extractTransactionsFromPDF(context, uri)

            val db = AppDatabase.getInstance(context)
            val transactionDao = db.transactionDao()

            CoroutineScope(Dispatchers.IO).launch {
                transactionDao.insertAll(transactions)
            }

            runOnUiThread {
                Toast.makeText(this, "Transaktionen gespeichert!", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Fehler beim Parsen des PDFs.", Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        }
    }
    }
