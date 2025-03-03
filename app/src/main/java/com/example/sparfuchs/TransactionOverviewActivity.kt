package com.example.sparfuchs

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sparfuchs.backend.AppDatabase
import com.example.sparfuchs.backend.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransactionOverviewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_overview)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = AppDatabase.getInstance(this)

        lifecycleScope.launch(Dispatchers.IO) {
            // Test-Transaktionen einfügen
            db.transactionDao().deleteAll() // Löscht alte Daten
            val testTransactions = listOf(
                TransactionEntity("01.01.2025", "Gehalt", 2500.00),
                TransactionEntity("10.01.2025", "Steuerrückzahlung", 300.00),
                TransactionEntity("15.01.2025", "Geschenk von Familie", 100.00),
                TransactionEntity("20.01.2025", "Nebenjob", 500.00),

                // Lebensmittel (Ausgaben)
                TransactionEntity("01.01.2025", "Einkauf Supermarkt", -45.0),
                TransactionEntity("05.01.2025", "Bäckerei", -5.50),
                TransactionEntity("07.01.2025", "Wochenmarkt", -22.30),
                TransactionEntity("10.01.2025", "Getränkemarkt", -18.75),

                // Freizeit (Ausgaben)
                TransactionEntity("02.01.2025", "Netflix Abo", -12.99),
                TransactionEntity("04.01.2025", "Restaurantbesuch", -30.0),
                TransactionEntity("08.01.2025", "Kinotickets", -25.00),
                TransactionEntity("12.01.2025", "Fitnessstudio", -39.99),

                // Transport (Ausgaben)
                TransactionEntity("03.01.2025", "Tankfüllung", -65.0),
                TransactionEntity("06.01.2025", "ÖPNV Ticket", -3.20),
                TransactionEntity("09.01.2025", "Parkgebühren", -4.50),
                TransactionEntity("14.01.2025", "Fahrradreparatur", -15.00),

                // Gesundheit (Ausgaben)
                TransactionEntity("05.01.2025", "Apotheken-Einkauf", -12.30),
                TransactionEntity("10.01.2025", "Zahnarztbesuch", -90.00),
                TransactionEntity("15.01.2025", "Medikamente", -25.75),
                TransactionEntity("20.01.2025", "Physiotherapie", -50.00),

                // Wohnen (Ausgaben)
                TransactionEntity("01.01.2025", "Miete", -800.00),
                TransactionEntity("05.01.2025", "Stromrechnung", -60.00),
                TransactionEntity("12.01.2025", "Internet & Telefon", -40.00),
                TransactionEntity("18.01.2025", "Wasserrechnung", -30.00),

                // Sonstiges (Ausgaben)
                TransactionEntity("03.01.2025", "Amazon Bestellung", -120.00),
                TransactionEntity("07.01.2025", "Geburtstagsgeschenk", -35.00),
                TransactionEntity("11.01.2025", "Buchkauf", -15.90),
                TransactionEntity("16.01.2025", "Spende", -20.00)
            )
            db.transactionDao().insertAll(testTransactions)

            // Transaktionen abrufen und gruppieren
            val transactions = db.transactionDao().getAllTransactions()
            val groupedTransactions = transactions.groupBy { it.category }

            Log.d("TransactionOverview", "Fetched Transactions: $transactions")

            withContext(Dispatchers.Main) {
                recyclerView.adapter = ExpandableTransactionAdapter(groupedTransactions)
            }
        }
    }
}
