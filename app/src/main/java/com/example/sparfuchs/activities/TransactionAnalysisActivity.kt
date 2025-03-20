package com.example.sparfuchs.activities

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.sparfuchs.R
import com.example.sparfuchs.backend.AppDatabase
import com.example.sparfuchs.backend.TransactionEntity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry

import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TransactionAnalysisActivity : ComponentActivity() {
    private lateinit var db: AppDatabase
    private lateinit var barChart: BarChart
    private lateinit var categorySpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var pieChart: PieChart
    private lateinit var spinnerType: Spinner

    private lateinit var spinnerYear: Spinner
    private var selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    private var selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) // 0 = Januar
    private var selectedType: String = "Ausgaben"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_analysis)

        db = AppDatabase.getInstance(this)
        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)
        categorySpinner = findViewById(R.id.spinner_category)
        spinnerYear = findViewById(R.id.spinner_year)
        monthSpinner = findViewById(R.id.spinner_month)
        spinnerType = findViewById(R.id.spinner_type)

        loadAvailableYears()
        setupMonthSpinner()
        setupTypeSpinner()
        loadCategories()

        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedYear = parent?.getItemAtPosition(position).toString().toInt()
                loadTransactions(
                    categorySpinner.selectedItem?.toString() ?: "Lebensmittel"
                ) // Balkendiagramm aktualisieren
                loadPieChartData() // Kreisdiagramm aktualisieren
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // 📌 1️⃣ Kategorien in den Spinner laden
    private fun loadCategories() {
        db.categoryDao().getAllCategories().observe(this) { categories ->
            val categoryNames = categories.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter

            categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedCategory = categoryNames[position]
                    loadTransactions(selectedCategory)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun loadTransactions(category: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val transactions = db.transactionDao().getTransactionsByCategory(category)

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
            val calendar = Calendar.getInstance()

            // 📌 1️⃣ Erstellt eine Map für die 12 Monate mit 0€ als Standardwert
            val monthlySums = (0..11).associateWith { 0f }.toMutableMap()

            for (transaction in transactions) {
                val date = dateFormat.parse(transaction.date) ?: continue
                calendar.time = date
                val transactionYear = calendar.get(Calendar.YEAR)

                if (transactionYear == selectedYear) {
                    val monthIndex = calendar.get(Calendar.MONTH)  // 0 = Januar, 1 = Februar, ...
                    monthlySums[monthIndex] = (monthlySums[monthIndex] ?: 0f) + transaction.amount.toFloat()
                }
            }

            // 📌 2️⃣ Monate nach Index (0-11) sortieren
            val sortedEntries = monthlySums.entries.sortedBy { it.key }

            withContext(Dispatchers.Main) {
                setupBarChart(sortedEntries)
            }
        }
    }


    // 📌 3️⃣ Balkendiagramm mit den monatlichen Summen aktualisieren
    private fun setupBarChart(monthlySums: List<Map.Entry<Int, Float>>) {
        val monthNames = listOf(
            "Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"
        )

        val entries = monthlySums.map { BarEntry(it.key.toFloat(), it.value) }
        val dataSet = BarDataSet(entries, "Monatliche Ausgaben")
        val data = BarData(dataSet)

        barChart.data = data
        barChart.xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() in 0..11) monthNames[value.toInt()] else ""
            }
        }
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.invalidate()
    }


    private fun loadAvailableYears() {
        lifecycleScope.launch(Dispatchers.IO) {
            val transactions = db.transactionDao().getAllTransactions()
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)

            val years = transactions.mapNotNull {
                try { dateFormat.parse(it.date)?.let { d -> Calendar.getInstance().apply { time = d }.get(Calendar.YEAR) } }
                catch (e: Exception) { null }
            }.toSet().sortedDescending()  // Sortiert nach Jahr (neueste zuerst)

            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@TransactionAnalysisActivity, android.R.layout.simple_spinner_item, years)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerYear.adapter = adapter

                // Standardmäßig aktuelles Jahr setzen
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val defaultPosition = years.indexOf(currentYear)
                if (defaultPosition != -1) spinnerYear.setSelection(defaultPosition)
            }
        }
    }

    private fun setupMonthSpinner() {
        val months = listOf(
            "Januar", "Februar", "März", "April", "Mai", "Juni",
            "Juli", "August", "September", "Oktober", "November", "Dezember"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner.adapter = adapter
        monthSpinner.setSelection(selectedMonth)

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedMonth = position
                loadPieChartData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadPieChartData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val transactions = db.transactionDao().getAllTransactions()
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
            val categorySums = mutableMapOf<String, Float>()

            for (transaction in transactions) {
                val date = dateFormat.parse(transaction.date) ?: continue
                val calendar = Calendar.getInstance()
                calendar.time = date
                val transactionYear = calendar.get(Calendar.YEAR)
                val transactionMonth = calendar.get(Calendar.MONTH)

                if (transactionYear == selectedYear && transactionMonth == selectedMonth) {
                    val amount = transaction.amount.toFloat()

                    // Nur Einnahmen oder nur Ausgaben berücksichtigen
                    if ((selectedType == "Einnahmen" && amount > 0) || (selectedType == "Ausgaben" && amount < 0)) {
                        categorySums[transaction.category] = (categorySums[transaction.category] ?: 0f) + amount
                    }
                }
            }

            val entries = categorySums.map { PieEntry(kotlin.math.abs(it.value), it.key) }

            if (entries.isEmpty()) return@launch // Kein Update, wenn keine Daten

            val dataSet = PieDataSet(entries, "Ausgaben nach Kategorie")
            dataSet.setColors(
                Color.rgb(244, 67, 54),  // Rot
                Color.rgb(33, 150, 243), // Blau
                Color.rgb(76, 175, 80),  // Grün
                Color.rgb(255, 193, 7),  // Gelb
                Color.rgb(156, 39, 176), // Lila
                Color.rgb(0, 188, 212),  // Cyan
                Color.rgb(255, 87, 34),  // Orange
                Color.rgb(63, 81, 181),  // Dunkelblau
                Color.rgb(139, 195, 74), // Hellgrün
                Color.rgb(233, 30, 99),  // Pink
                Color.rgb(121, 85, 72),  // Braun
                Color.rgb(0, 150, 136)   // Türkis
            )
            dataSet.valueTextSize = 0f // Kein Text im Diagramm anzeigen
            dataSet.setDrawValues(false) // Keine Werte im Diagramm anzeigen

            val data = PieData(dataSet)
            dataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "" // Keine Textanzeige
                }
            }

            val legendEntries = entries.map { entry ->
                LegendEntry("${entry.label}: ${"%.2f".format(entry.value)} €", Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null, Color.BLACK)
            }

            withContext(Dispatchers.Main) {
                pieChart.data = data
                pieChart.description.isEnabled = false
                pieChart.setUsePercentValues(false)

                // Dynamische Höhe der Legende, um Platz für mehr Kategorien zu schaffen
                val legendHeight = (entries.size * 20).toFloat() // Berechne die Höhe für die Legende
                val extraOffsetBottom = legendHeight + 20f

                pieChart.setExtraOffsets(5f, 10f, 5f, extraOffsetBottom) // Weniger Platz für die Legende

                // Die Legende wird hier durch eine horizontale Scrollansicht ersetzt
                // Scrollbare Legende anzeigen
                val scrollableLegendLayout = findViewById<LinearLayout>(R.id.scrollableLegendLayout) // Hier holst du das LinearLayout
                val scrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView) // Hier holst du den HorizontalScrollView
                scrollableLegendLayout.removeAllViews() // Entferne alle vorherigen Einträge

                // Erstelle dynamisch die Legende in einem horizontal scrollbaren Layout
                val numberOfItemsPerRow = 5 // Anzahl der Kategorien pro Ansicht
                val totalRows = (legendEntries.size + numberOfItemsPerRow - 1) / numberOfItemsPerRow // Berechne die Anzahl der Zeilen

                // Erstelle und füge die legend entries als Button oder TextView in das Layout ein
                for (i in 0 until totalRows) {
                    val rowLayout = LinearLayout(this@TransactionAnalysisActivity)
                    rowLayout.orientation = LinearLayout.HORIZONTAL
                    rowLayout.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    for (j in 0 until numberOfItemsPerRow) {
                        val index = i * numberOfItemsPerRow + j
                        if (index >= legendEntries.size) break

                        val entry = legendEntries[index]

                        // Füge hier deine Logik zum Erstellen von TextViews oder Buttons hinzu
                        val textView = TextView(this@TransactionAnalysisActivity).apply {
                            text = entry.label
                            setTextColor(entry.formColor)
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            gravity = Gravity.CENTER
                        }

                        rowLayout.addView(textView)
                    }

                    scrollableLegendLayout.addView(rowLayout) // Füge die Zeile zur scrollbaren Legende hinzu
                }

                // Aktualisiere das PieChart
                pieChart.invalidate()
            }
        }
    }




    private fun setupTypeSpinner() {
        val types = listOf("Ausgaben", "Einnahmen")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter
        spinnerType.setSelection(0) // Standard: "Ausgaben"

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedType = types[position]
                loadPieChartData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }



}

