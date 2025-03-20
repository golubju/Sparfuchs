package com.example.sparfuchs.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.sparfuchs.R
import com.example.sparfuchs.backend.AppDatabase
import com.example.sparfuchs.backend.CategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch(Dispatchers.IO) {
            val db: AppDatabase = AppDatabase.getInstance(applicationContext)
            val dao = db.categoryDao()
            val existingCategories = dao.getAllCategoriesList()

            if (existingCategories.isEmpty()) {
            dao.insert(CategoryEntity("nicht zugeordnete Transaktionen", "keine Keywords"))
            dao.insert(CategoryEntity("Lebensmittel", "Aldi, Lidl, Rewe, Netto, Edeka, Famila,Norma, Kaufland"))
            dao.insert(CategoryEntity("Transport", "Bahn, Bus, Auto, Benzin, DB"))
            dao.insert(CategoryEntity("Freizeit", "Kino, Sport, Events"))
            dao.insert(CategoryEntity("Miete", "Miete"))
            dao.insert(CategoryEntity("Gehalt", "Lohn, Gehalt"))
            dao.insert(CategoryEntity("Drogerie", "Rossman, DM"))
                }
        }

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
