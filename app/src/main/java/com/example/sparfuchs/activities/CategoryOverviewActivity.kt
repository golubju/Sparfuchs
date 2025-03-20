package com.example.sparfuchs.activities

import com.example.sparfuchs.CategoryAdapter
import CategoryViewModel
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sparfuchs.R
import com.example.sparfuchs.backend.CategoryEntity



class CategoryOverviewActivity : ComponentActivity() {

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private var selectedCategory: CategoryEntity? = null  // Store the selected category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_overview)
        recyclerView = findViewById(R.id.recycler_view_categories)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CategoryAdapter { category -> selectCategory(category) }
        recyclerView.adapter = adapter
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        categoryViewModel.allCategories.observe(this) { categories ->
            categories?.let { adapter.submitList(it) }
        }
        findViewById<Button>(R.id.add_category_button).setOnClickListener {
            showCategoryDialog(null)
        }
    }
    private fun selectCategory(category: CategoryEntity) {
        selectedCategory = category
        showCategoryDialog(category)
    }
    private fun showCategoryDialog(category: CategoryEntity?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_category, null)
        val editCategoryName = dialogView.findViewById<EditText>(R.id.edit_category_name)
        val editKeywords = dialogView.findViewById<EditText>(R.id.edit_category_keywords)
        if (category != null) {
            editCategoryName.setText(category.name)
            editKeywords.setText(category.keywords)
        }
        AlertDialog.Builder(this)
            .setTitle(if (category == null) "Neue Kategorie hinzufügen" else "Kategorie bearbeiten")
            .setView(dialogView)
            .setPositiveButton("Speichern") { _, _ ->
                val newName = editCategoryName.text.toString()
                val newKeywords = editKeywords.text.toString()
                if (newName.isNotBlank()) {
                    if (category == null) {
                        val newCategory = CategoryEntity(newName, newKeywords)
                        categoryViewModel.insert(newCategory)
                        Toast.makeText(this, "Kategorie hinzugefügt", Toast.LENGTH_SHORT).show()
                    } else {
                        val updatedCategory = CategoryEntity(newName, newKeywords).apply {
                            id = category.id
                        }
                        categoryViewModel.update(updatedCategory)
                        Toast.makeText(this, "Kategorie aktualisiert", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Kategorie Name darf nicht leer sein", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Abbrechen", null)
            .setNeutralButton("Löschen") { _, _ ->
                category?.let { deleteCategory(it) }
            }
            .show()
    }
    private fun deleteCategory(category: CategoryEntity) {
        categoryViewModel.delete(category)
        Toast.makeText(this, "Kategorie gelöscht", Toast.LENGTH_SHORT).show()
    }
}

