package com.example.sparfuchs.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.sparfuchs.R


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val buttonPdf: Button = findViewById(R.id.button_pdf)
        val buttonTransactions: Button = findViewById(R.id.button_transactions)
        val buttonCategories: Button = findViewById(R.id.button_categories)
        val buttonAnalyser: Button = findViewById(R.id.button_analyser)
        buttonPdf.setOnClickListener {
            val intent = Intent(this, PdfUploadActivity::class.java)
            startActivity(intent)
        }
        buttonTransactions.setOnClickListener {
            val intent = Intent(this, TransactionOverviewActivity::class.java)
            startActivity(intent)
        }
        buttonCategories.setOnClickListener {
            val intent = Intent(this, CategoryOverviewActivity::class.java)
            startActivity(intent)
        }
        buttonAnalyser.setOnClickListener {
            val intent = Intent(this, TransactionAnalysisActivity::class.java)
            startActivity(intent)
        }
    }
}
