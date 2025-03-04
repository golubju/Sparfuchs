package com.example.sparfuchs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val buttonPdf: Button = findViewById(R.id.button_pdf)
        val buttonTransactions: Button = findViewById(R.id.button_transactions)
        val buttonCategories: Button = findViewById(R.id.button_categories)

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
    }


}
